package org.javastorm.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.javastorm.network.commands.*;
import org.javastorm.util.MyByteBuffer;
import org.javastorm.util.MyDataInputStream;
import org.javastorm.util.MyOutputStream;

//Handy functions to receive/send packets in NS-speek.
public class Connection
{
	private boolean debug;
	private PrintStream debugStream;

	static
	{
		Connection.masterBwMonitor = new BandwidthMonitor();
	}

	public Connection()
	{
		this.cmds = new LinkedBlockingQueue<NSCommand>();
		this.bwMonitor = new BandwidthMonitor();
		this.listener = new CommandListener();
	}

	public void setMinorVersion(int minorVersion)
	{
		this.minorVersion = minorVersion;
	}

	public int getMinorVersion()
	{
		return this.minorVersion;
	}

	public void enableDebug(PrintStream debugStream)
	{
		this.debug = true;
		this.debugStream = debugStream;
	}

	// Turns on packet logging.
	public void setPacketLogging(boolean log)
	{
		/*
		this.logPackets = log;
		try
		{
			this.logFile = new PrintWriter(new File("nspackets.log"));
		} catch(Throwable t) { }*/
	}

	private void logPacket(MyByteBuffer cmd)
	{/*
			cmd.setPosition(0);
			for(int i = 0; i < cmd.size(); i++)
			{
				this.logFile.print(cmd.get());
				
				if(i > 0)
				{
					if((i % 8) == 0)
						this.logFile.print("  ");
					else if((i % 16) == 0)
						this.logFile.println();
					else
						this.logFile.print(" ");
				}
			}
			this.logFile.println();
			this.logFile.println();
			cmd.setPosition(0);*/
	}

	// Connects to specified server.
	public boolean connect(String hostname, int port, int timeout)
	{
		return this.connect(hostname, port, timeout, false);
	}

	public boolean connect(String hostname, int port, int timeout, boolean forceOffProxy)
	{
		Socket socket = this.createSocket(hostname, port, timeout, forceOffProxy);
		if (socket == null)
			return false;

		return this.init(socket);
	}

	// Initializes the NSProtocol with an existing connection.
	public boolean init(Socket socket)
	{
		this.socket = socket;

		try
		{
			this.in = new MyDataInputStream(socket.getInputStream());
			this.out = new MyOutputStream(socket.getOutputStream());
		}
		catch (IOException ioe)
		{
			return false;
		}

		this.bwMonitor.start();
		if (!Connection.masterBwMonitor.started())
			Connection.masterBwMonitor.start();
		this.listener.start();

		return true;
	}

	public BandwidthMonitor getBandwidthMonitor()
	{
		return this.bwMonitor;
	}

	public void disconnect()
	{
		try
		{
			if (this.socket != null)
				this.socket.close();
		}
		catch (IOException ioe)
		{
		}
		this.socket = null;

		// We're not gonna return from here 'till the listener thread has died.
		while (this.listener.thread != null)
			Thread.yield();
	}

	public boolean connected()
	{
		if (this.socket == null)
			return false;
		return this.socket.isConnected() && !this.socket.isClosed();
	}

	public static void useProxy(String proxy)
	{
		Connection.proxy = proxy;
	}

	public static String getProxy()
	{
		return Connection.proxy;
	}

	// Sends a single NSPacket command in a packet.
	public void sendCommandRaw(NSCommand command)
	{
		this.sendCommand(command, true, true);
	}

	public void sendCommand(NSCommand command)
	{
		this.sendCommand(command, true, false);
	}

	public void sendCommand(NSCommand command, boolean packetHeader)
	{
		this.sendCommand(command, packetHeader, false);
	}

	public void sendCommand(NSCommand command, boolean packetHeader, boolean useRaw)
	{
		// Ensures we don't flood chat.
		if (command.getCommandID() == Connection.ZChatLine)
			this.PacketWait();

		this.sendCommands(new NSCommand[]
		{ command }, packetHeader, useRaw);
	}

	// Sends a an array of commands rolled into one packet.
	public void sendCommands(NSCommand[] commands)
	{
		this.sendCommands(commands, true, false);
	}

	public void sendCommands(NSCommand[] commands, boolean packetHeader, boolean useRaw)
	{
		if(this.debug)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(new Date()).append(": CLIENT >>> SERVER\n");
			
			for(NSCommand command : commands)
			{
				sb.append(command).append("\n");
				for(String line : command.outputDebug())
					sb.append("\t").append(line).append("\n");
			}
			
			this.debugStream.println(sb.toString());
			this.debugStream.flush();
		}
		
		int len = 4;
		MyByteBuffer packetData;

		if (this.out == null)
			return;

		for (int i = 0; i < commands.length; i++)
		{
			commands[i]._setMinorVersion(this.minorVersion);

			if (useRaw)
				packetData = commands[i].getCommandBuffer();
			else
			{
				packetData = commands[i].getCommandData();
				if (packetData.position() < packetData.size())
				{
					System.err.println(commands[i] + " WARNING! PACKET BUFFER UNDERFLOW! " + packetData.position() + "/" + packetData.size());
					throw new RuntimeException();
				}
			}

			len += packetData.size() + 4;
		}

		synchronized (this.out)
		{
			try
			{
				// Write the packets header.
				if (packetHeader)
				{
					this.out.write(255);
					this.out.write(4);
					this.out.writeShort(len);
				}

				for (int i = 0; i < commands.length; i++)
				{
					if (useRaw)
						packetData = commands[i].getCommandBuffer();
					else
						packetData = commands[i].getCommandData();

					// Write the command header.
					this.out.write(commands[i].getVersion());
					this.out.write(commands[i].getCommandID());
					this.out.writeShort(packetData.size() + 4);

					// Write the command.
					packetData.setPosition(0);
					for (int j = 0; j < packetData.size(); j++)
						this.out.write(packetData.get());

					if (this.logPackets)
						this.logPacket(packetData);
					this.commandsOut++;
				}

				this.out.flush();
				this.bwMonitor.trafficOut(len);
				Connection.masterBwMonitor.trafficOut(len);
			}
			// This happens when socket has been closed by other end. Even if this was triggered by something
			// else we probably don't want to press on with this connection anyway.
			catch (SocketException se)
			{
				try
				{
					this.socket.close();
				}
				catch (IOException ioe)
				{
				}
				catch (NullPointerException ne)
				{
				}
			}
			catch (IOException ioe)
			{
				this.out.clean();
				ioe.printStackTrace();
			}
			catch (NullPointerException npe)
			{
				this.out.clean();
			}
		}
	}

	private void PacketWait()
	{
		if (System.currentTimeMillis() - 700 <= lastMessageSent)
		{
			long waitTime = 700 - (System.currentTimeMillis() - lastMessageSent);
			waitMillis(waitTime);
		}

		lastMessageSent = System.currentTimeMillis();
	}

	// Reads a packet in from supplied stream.
	// A packet can contain many commands.
	// If block is true, a check won't be made for available() input, which will cause the calling thread
	// to block until we get something.
	private NSCommand[] readCommands(boolean block)
	{
		int packetHeader, packetLen, commandIDs[] = new int[1024], commandLen, packetTotalRead, commandCount = 0;
		NSCommand[] commands = null;

		// Temporary buffer for packets.
		MyByteBuffer commandBuffers[] = new MyByteBuffer[1024];
		MyByteBuffer command;

		try
		{
			synchronized (this.in)
			{
				try
				{
					// If we don't want to block, we'll just set a really quick timeout on the first byte.
					// Obviously if we receive 1 byte then the rest of the packet will here very shortly.
					// So even in nonblocking, we still block for the rest of the packet if we have at least
					// part of one.
					if (!block)
					{
						this.socket.setSoTimeout(1);
					}

					// Read the header.
					packetHeader = this.in.read();

					if (!block)
					{
						this.socket.setSoTimeout(0);
					}

					if (packetHeader == -1)
					{
						this.socket.close();
						return null;
					}

					if (packetHeader != 255)
					{
						int commandID = this.in.read();
						commandIDs[commandCount] = commandID;
						commandLen = this.in.readShort();

						this.bwMonitor.trafficIn(commandLen + 4);
						Connection.masterBwMonitor.trafficIn(commandLen + 4);

						commandBuffers[commandCount] = new MyByteBuffer();
						command = commandBuffers[commandCount];

						command.allocate(commandLen - 4);

						for (int i = 0; i < command.size(); i++)
						{
							command.put(this.in.read());
						}

						command.setPosition(0);
						commandCount++;
					}
					else
					{
						this.in.read();
						// Next is the length of this packet in total.
						packetLen = this.in.readShort();

						this.bwMonitor.trafficIn(packetLen + 4);
						Connection.masterBwMonitor.trafficIn(packetLen + 4);

						// Total bytes read in this packet.
						packetTotalRead = 4;

						while (packetTotalRead < packetLen)
						{
							this.in.read();
							// Read a command.
							int commandID = this.in.read();

							commandIDs[commandCount] = commandID;

							// The length of this command's parameters.
							commandLen = this.in.readShort();

							// A bit of a hack I suppose. If we get a notifyencode, just read ahead, set encode status
							// accordingly then continue to next packet.
							if (commandID == Connection.NotifyEncode)
							{
								this.encode = this.in.read() == 1 ? true : false;

								if (this.encode)
									this.in.setEncodeString("myfleahasdogs");
								else
									this.in.setEncodeString(null);

								packetTotalRead += 5;

								continue;
							}

							// Buffer the packet.
							commandBuffers[commandCount] = new MyByteBuffer();
							command = commandBuffers[commandCount];

							if (commandIDs[commandCount] == 0)
								command.allocate(packetLen - 8);
							else
								command.allocate(commandLen - 4);

							packetTotalRead += 4;

							for (int i = 0; i < command.size(); i++)
							{
								command.put(this.in.read());
								packetTotalRead++;
							}

							command.setPosition(0);
							commandCount++;

							this.commandsIn++;

							this.in.resetEncode();
						}
					}

					commands = new NSCommand[commandCount];
					for (int i = 0; i < commandCount; i++)
					{
						switch (commandIDs[i])
						{
							case ZSendBucks:
							{
								commands[i] = new ZSendBucks();
								break;
							}
							case ZDestroySquid:
							{
								commands[i] = new ZDestroySquid();
								break;
							}
							case ZChalLoginReply:
							{
								commands[i] = new ZChalLoginReply();
								break;
							}
							case ZRequestLogin:
							{
								commands[i] = new ZRequestLogin();
								break;
							}
							case ZChalLaunchServer:
							{
								commands[i] = new ZChalLaunchServer();
								break;
							}
							case ZChalPlayerAdd:
							{
								commands[i] = new ZChalPlayerAdd();
								break;
							}
							case ZChalPlayerAddress:
							{
								commands[i] = new ZChalPlayerAddress();
								break;
							}
							case ZChalPlayerDel:
							{
								commands[i] = new ZChalPlayerDel();
								break;
							}
							case ZChalPlayerUpdate:
							{
								commands[i] = new ZChalPlayerUpdate();
								break;
							}
							case ZChatLine:
							{
								commands[i] = new ZChatLine();
								break;
							}
							case ZChalLaunchClient:
							{
								commands[i] = new ZChalLaunchClient();
								break;
							}
							case ZChalBattleDel:
							{
								commands[i] = new ZChalBattleDel();
								break;
							}
							case ZChalBattleUpdate:
							{
								commands[i] = new ZChalBattleUpdate();
								break;
							}
							case ZChalRequestChangeStatus:
							{
								commands[i] = new ZChalRequestChangeStatus();
								break;
							}
							case ZChalBattleAdd:
							{
								commands[i] = new ZChalBattleAdd();
								break;
							}
							case ZBattleOptions:
							{
								commands[i] = new ZBattleOptions();
								break;
							}
							case ZLoginBegin:
							{
								commands[i] = new ZLoginBegin();
								break;
							}
							case ZPlayerData:
							{
								commands[i] = new ZPlayerData();
								break;
							}
							case ZLoadFort:
							{
								commands[i] = new ZLoadFort();
								break;
							}
							case ZLoginReply:
							{
								commands[i] = new ZLoginReply();
								break;
							}
							case ZInitialMoney:
							{
								commands[i] = new ZInitialMoney();
								break;
							}
							case ZTimeSync:
							{
								commands[i] = new ZTimeSync();
								break;
							}
							case ZKeepAlive:
							{
								commands[i] = new ZKeepAlive();
								break;
							}
							case ZDeclareDraw:
							{
								commands[i] = new ZDeclareDraw();
								break;
							}
							case ZRootReplyQuery:
							{
								commands[i] = new ZRootReplyQuery();
								break;
							}
							case ZChalServerInfo:
							{
								commands[i] = new ZChalServerInfo();
								break;
							}
							case ZChalRequestLogin:
							{
								commands[i] = new ZChalRequestLogin();
								break;
							}/*
														case ZReconnectState:
														{
															commands[i] = new ZReconnectState();
															break;
														}*/
							case ZFortDataPacket:
							{
								commands[i] = new ZFortDataPacket();
								break;
							}
							case ZReadyToRock:
							{
								commands[i] = new ZReadyToRock();
								break;
							}
							case ZRequestCleanShutdown:
							{
								commands[i] = new ZRequestCleanShutdown();
								break;
							}
							case ZCreateCanon:
							{
								commands[i] = new ZCreateCanon();
								break;
							}
							case ZPathProcess:
							{
								commands[i] = new ZPathProcess();
								break;
							}
							case ZCompressedUpdateSquid:
							{
								commands[i] = new ZCompressedUpdateSquid();
								break;
							}
							case ZRootQuery:
							{
								commands[i] = new ZRootQuery();
								break;
							}
							case ZTALKBACK:
							{
								commands[i] = new ZTALKBACK();
								break;
							}
							case ZPreconnect:
							{
								commands[i] = new ZPreconnect();
								break;
							}
							case ZChalRequestSailTo:
							{
								commands[i] = new ZChalRequestSailTo();
								break;
							}
							default:
							{
								commands[i] = new ZUnknown(commandIDs[i]);
								break;
								/*
								commands[i] = null;
								
								commandBuffers[i].position(0);
								unknownPacketCount++;
								
								java.lang.System.this.out.println("Packet command '" + commandIDs[i] + "' not recognized. Outputting to file 'packet" + unknownPacketCount + ".log'");
								
								FileWriter file_out = new FileWriter("packet" + unknownPacketCount + ".log");
								
								java.lang.System.this.out.println("Packet had a length of " + commandBuffers[i].size() + " bytes.");
								
								for(int j = 0; j < commandBuffers[j].size(); j++)
									file_this.out.write(commandBuffers[i].get());
								
								file_this.out.close();
								
								break;*/
							}
						}

						commands[i]._setMinorVersion(this.minorVersion);
						commands[i].setCommandBuffer(commandBuffers[i]);
						commands[i].readCommandData(commandBuffers[i]);
					}
				}
				catch (IOException ioe)
				{
					try
					{
						// Gotta make sure we retain state.
						this.socket.setSoTimeout(0);
					}
					catch (IOException ioe2)
					{
					}
					return null;
				}
			}
		}
		catch (NullPointerException npe)
		{
		}

		return commands;
	}

	// New and improved! NSProtocol now handles reading in all incoming commands, popping them onto a stack.
	// So now we can query for one command at a time.
	public NSCommand readCommand()
	{
		return this.readCommand(false);
	}

	public NSCommand readCommand(boolean block)
	{
		NSCommand command = null;

		// If we're blocking, save off the calling Thread if we need to interrupt later.
		// Then we go into a blocking take() call.
		if (block)
		{
			this.commandBlocker = Thread.currentThread();
			try
			{
				command = this.cmds.take();
			}
			catch (InterruptedException ie)
			{
				System.out.println("NSConnection.readCommand() interrupted.");
				command = null;
			}

			this.commandBlocker = null;
		}
		else
			command = this.cmds.poll();

		if(command != null && this.debug)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(new Date()).append(": CLIENT <<< SERVER\n");

			sb.append(command).append("\n");
			for(String line : command.outputDebug())
				sb.append("\t").append(line).append("\n");

			this.debugStream.println(sb.toString());
			this.debugStream.flush();
		}
		return command;
	}

	public NSCommand peekCommand()
	{
		return this.peekCommand(false);
	}

	public NSCommand peekCommand(boolean block)
	{
		NSCommand command = null;

		// If we're blocking, save off the calling Thread if we need to interrupt later.
		// Then we go into a blocking take() call.
		if (block)
		{
			this.commandBlocker = Thread.currentThread();

			while (command == null && !this.commandBlocker.isInterrupted())
			{
				command = this.cmds.peek();
				Thread.yield();
			}

			this.commandBlocker = null;
		}
		else
			command = this.cmds.peek();

		return command;
	}

	public static void waitMillis(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException ie)
		{
		}
	}

	// Creates a socket to the supplied hostname and port. Uses proxy if it's enabled.
	protected Socket createSocket(String hostname, int port, int timeout, boolean forceOffProxy)
	{
		try
		{
			return this.createSocket(InetAddress.getByName(hostname).getAddress(), port, timeout, forceOffProxy);
		}
		catch (UnknownHostException uhe)
		{
			return null;
		}
	}

	protected Socket createSocket(byte[] ip, int port, int timeout, boolean forceOffProxy)
	{
		try
		{
			Socket sock = new Socket();

			if ((Connection.proxy != null) && !forceOffProxy)
			{
				// The NSProtocolProxy always listens in on port 6668.
				sock.connect(new InetSocketAddress(Connection.proxy, 6668), timeout);
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.write(ip[0]);
				out.write(ip[1]);
				out.write(ip[2]);
				out.write(ip[3]);
				out.writeInt(port);
				out.writeInt(timeout);
				out.flush();

				while (sock.getInputStream().available() != 1)
				{
					Thread.yield();
				}
				if (sock.getInputStream().read() == 0)
				{
					sock.close();
					return null;
				}
			}
			else
				sock.connect(new InetSocketAddress(InetAddress.getByAddress(ip), port), timeout);

			return sock;
		}
		catch (IOException ioe)
		{
		}

		return null;
	}

	// This class will spin on a thread, pushing commands on a stack as they come in.
	private class CommandListener implements Runnable
	{
		public void start()
		{
			this.thread = new Thread(this, "NSProtocol Listener Thread");
			this.thread.start();
		}

		public void run()
		{
			while (connected())
			{
				// Check to see if there's any new packets on the wire.
				NSCommand[] commands = readCommands(true);
				NSCommand command;

				if (commands == null)
				{
					// Nothing yet.
					continue;
				}

				for (int i = 0; i < commands.length; i++)
				{
					command = commands[i];

					if (command == null)
						continue;

					try
					{
						cmds.put(command);
					}
					catch (InterruptedException ie)
					{
					}
				}
			}

			bwMonitor.end();
			new Thread(new Runnable()
			{
				public void run()
				{
					Connection.waitMillis(100);
					// Any commands that were in the queue should be dropped.
					cmds.clear();
					// Cut off blocking caller if needed.
					if (commandBlocker != null)
						commandBlocker.interrupt();
				}
			}).start();

			// Thread died, clean up.
			this.thread = null;
		}

		private Thread thread;
	}

	private static String proxy = null;

	private CommandListener listener;

	private LinkedBlockingQueue<NSCommand> cmds; // FIFO queue of incoming commands.

	private Thread commandBlocker; // When a client enters into readCommand, if it's a blocking call

	// we store the calling thread here. So if the connection is killed
	// we have a safe way of cutting the blocking caller off.

	private int minorVersion = 78;

	private BandwidthMonitor bwMonitor;

	private int commandsIn; // how many zackets we've received

	private int commandsOut; // and how many we've sent.

	private long lastMessageSent = 0;

	private Socket socket;

	private MyDataInputStream in;

	private MyOutputStream out;

	private boolean encode = false;

	private boolean logPackets;

	//private PrintWriter logFile;

	public static BandwidthMonitor masterBwMonitor;

	//private static int unknownPacketCount;

	// Quick references to zacket types.
	public static final int ZTimeSync = 5;

	public static final int NotifyEncode = 7;

	public static final int ZRootReplyQuery = 15;

	public static final int ZRootQuery = 22;

	public static final int ZChalLaunchServer = 26;

	public static final int ZChalLaunchClient = 27;

	public static final int ZChalRequestLogin = 28;

	public static final int ZChalLoginReply = 29;

	public static final int ZChalRequestSailTo = 30;

	public static final int ZChalRequestChangeStatus = 31;

	public static final int ZChalPlayerAdd = 32;

	public static final int ZChalPlayerDel = 33;

	public static final int ZChalPlayerUpdate = 34;

	public static final int ZChalBattleAdd = 35;

	public static final int ZChalBattleDel = 36;

	public static final int ZChalServerInfo = 38;

	public static final int ZRequestLogin = 39;

	public static final int ZLoginReply = 40;

	public static final int ZLoginBegin = 41;

	public static final int ZFortDataPacket = 42;

	public static final int ZPlayerData = 43;

	public static final int ZDestroySquid = 46;

	public static final int ZPathProcess = 48;

	public static final int ZCreateCanon = 63;

	public static final int ZChatLine = 65;

	public static final int ZRequestCleanShutdown = 82;

	public static final int ZKeepAlive = 100;

	public static final int ZBattleOptions = 103;

	public static final int ZSendBucks = 108;

	public static final int ZReadyToRock = 110;

	public static final int ZDeclareDraw = 111;

	public static final int ZChalBattleUpdate = 113;

	public static final int ZLoadFort = 114;

	public static final int ZCompressedUpdateSquid = 122;

	public static final int ZChalPlayerAddress = 123;

	public static final int ZPreconnect = 124;

	public static final int ZInitialMoney = 129;

	public static final int ZChalAdmin = 131;

	public static final int ZReconnectState = 132;

	public static final int ZTALKBACK = 136;

	public static final int ZSambroCommand = 138;
}