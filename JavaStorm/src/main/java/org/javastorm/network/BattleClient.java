package org.javastorm.network;

import java.io.File;
import java.io.PrintStream;

import org.javastorm.Version;
import org.javastorm.World;
import org.javastorm.battle.BattleOptions;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZoneRing;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZChatLine;
import org.javastorm.network.commands.ZCompressedUpdateSquid;
import org.javastorm.network.commands.ZCreateCanon;
import org.javastorm.network.commands.ZDeclareDraw;
import org.javastorm.network.commands.ZDestroySquid;
import org.javastorm.network.commands.ZFortDataPacket;
import org.javastorm.network.commands.ZInitialMoney;
import org.javastorm.network.commands.ZKeepAlive;
import org.javastorm.network.commands.ZLoginBegin;
import org.javastorm.network.commands.ZPathProcess;
import org.javastorm.network.commands.ZPlayerData;
import org.javastorm.network.commands.ZPreconnect;
import org.javastorm.network.commands.ZReadyToRock;
import org.javastorm.network.commands.ZRequestCleanShutdown;
import org.javastorm.network.commands.ZRequestLogin;
import org.javastorm.network.commands.ZSendBucks;
import org.javastorm.network.commands.ZTimeSync;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.territory.Territory;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.util.MyByteBuffer;

// This is the class that basically handles all things battle client related.
// Specific logic is implemented via the callback system.
public class BattleClient implements CommandListener
{
	public BattleClient()
	{
		this.listener = new CommandProcessor();
	}

	public void setCallback(BattleListener listener)
	{
		this.callback = listener;
	}

	// This is done in zones, we send a preconnection request to Battlemaster.
	public boolean preconnect(ZonePlayer me, ZoneRing ring)
	{
		this.me = me;

		ZonePlayer battlemaster = ring.getBattleMaster();

		if (battlemaster == null)
			return false;

		String ip = battlemaster.getIpString();

		try
		{
			if ((battlemaster.getIpString().equals("220.239.90.145") || battlemaster.getNickname().equalsIgnoreCase("sambro")) && Connection.getProxy() == null)
			{
				ip = "127.0.0.1";
			}
		}
		catch (Throwable t)
		{
		}

		boolean forceOffProxy = false;

		// Are we acting as a client for a serving bot?
		if (battlemaster.getSubscriberID() == this.me.getSubscriberID())
		{
			// This is really painful to think about, but we will connect to the bot that inited us, so then we can
			// be put in the connection pool system to be proxyed to the .exe.
			// Conceptually it makes sense I guess, it's better than some ugly hack. Also this means I can
			// consolidate alot of in-game tracking stuff here, instead of mirroring it in some sloppy setup in the NSBattleProxy.
			ip = "127.0.0.1";
		}

		// For development, my ISP doesn't let me connect to myself from internet ip.
		// fallback to a looopback =)
		if (ip.equals("127.0.0.1"))
		{
			forceOffProxy = true;
		}

		System.out.println("Connecting to ip " + ip);

		this.connection = new Connection();
		try
		{
			File log = new File("D:\\client.log");
			log.createNewFile();
			this.connection.enableDebug(new PrintStream(log));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if (!this.connection.connect(ip, 6799, 10000, forceOffProxy))
		{
			return false;
		}

		// Do the preconnect. I gather this is like a way of authenticating with battlemaster before game.
		ZPreconnect joinBattle = new ZPreconnect();
		joinBattle.setServerSubscriberID(battlemaster.getSubscriberID());
		joinBattle.setPlayerSubscriberID(this.me.getSubscriberID());
		joinBattle.setSlot(this.me.getSlot());
		joinBattle.setRing(this.me.getRing());
		joinBattle.setPlayerIndex(this.me.getPlayerIndex());
		this.connection.sendCommand(joinBattle);

		// We'll wait and see what the BM sayeth.
		ZPreconnect response = (ZPreconnect) this.connection.readCommand(true);
		if (response == null)
			return false;
		if (response.getCode() != 1)
			return false;

		this.listener.start(this.connection, this);

		return true;
	}

	// Lets us know the battle has started and that we should ZRequestLogin.
	public void startBattle(MyByteBuffer fortData)
	{
		System.out.println("Started.");

		this.fortData = fortData;

		this.battle = new World();

		Connection.waitMillis(500);

		ZRequestLogin auth = new ZRequestLogin();
		auth.setSubscriberID(this.me.getSubscriberID());
		auth.setNickname(this.me.getNickname());
		auth.setFortData(this.fortData);

		this.connection.sendCommand(auth);
	}

	public void disconnect()
	{
		if (this.connection != null)
			this.connection.disconnect();
		this.listener.stop();
	}

	public void draw()
	{
		ZDeclareDraw draw = new ZDeclareDraw();
		draw.setPlayerId(this.getMe().getPlayerIndex());
		draw.setState(1);
		this.connection.sendCommand(draw);
	}

	public void say(String sayText)
	{
		ZChatLine say = new ZChatLine();
		say.setSubscriberID(this.me.getSubscriberID());
		say.setMessage("032" + "~[I93.114]" + sayText);
		say.setNickname(this.me.getNickname());
		say.setMessageLineBreak(false);
		say.setMessageNullTerm(true);

		for (int i = 1; i < 9; i++)
		{
			if (this.battle.getPlayer(i) != null)
			{
				if (this.battle.getPlayer(i).getSubscriberID() != this.me.getSubscriberID())
					say.addDestinationSubscriberID(this.battle.getPlayer(i).getSubscriberID());
			}
		}

		say.addDestinationSubscriberID(this.me.getSubscriberID());

		this.connection.sendCommand(say);
	}

	//	Commands bot to speak.
	public void sayObserver(String sayText)
	{
		ZChatLine say = new ZChatLine();
		say.setSubscriberID(this.me.getSubscriberID());
		say.setMessage("128" + "~[I93.112]" + sayText);
		say.setNickname(this.me.getNickname());
		say.setMessageLineBreak(false);
		say.setMessageNullTerm(true);

		for (int i = 1; i < 9; i++)
		{
			if (this.battle.getPlayer(i) != null)
			{
				if (this.battle.getPlayer(i).getSubscriberID() != this.me.getSubscriberID())
					say.addDestinationSubscriberID(this.battle.getPlayer(i).getSubscriberID());
			}
		}

		say.addDestinationSubscriberID(this.me.getSubscriberID());

		this.connection.sendCommand(say);
	}

	public void whisper(String sayText, int subscriberID)
	{
		ZChatLine say = new ZChatLine();
		say.setSubscriberID(this.me.getSubscriberID());
		say.setMessage(sayText);
		say.setNickname(this.me.getNickname());
		say.setMessageLineBreak(false);
		say.setMessageNullTerm(true);

		say.addDestinationSubscriberID(subscriberID);

		this.connection.sendCommand(say);
	}

	// Sends a popup message to specific player.
	public void popupTo(String sayText, int subID)
	{
		ZChatLine say = new ZChatLine();
		say.setMessage(sayText);
		say.setEncoding(ZChatLine.CHAT_ASCII | ZChatLine.CHAT_POPUP);
		say.setSubscriberID(0);
		say.setNickname("");
		say.addDestinationSubscriberID(subID);
		this.connection.sendCommand(say);
	}

	public void onConnectionDead()
	{
		// TODO stuff here?
	}

	public void onException(Throwable t)
	{
		t.printStackTrace();
	}

	public void onCommand(NSCommand command)
	{
		System.err.println("NSBattleClient: " + command);

		switch (command.getCommandID())
		{
			case Connection.ZSendBucks:
			{
				ZSendBucks sendBucks = (ZSendBucks) command;

				BattlePlayer target = this.battle.getPlayer(sendBucks.getTargetPlayerId());
				target.incrementCurrentMoney(sendBucks.getAmount());

				if (!sendBucks.isRefund())
				{
					BattlePlayer source = this.battle.getPlayer(sendBucks.getSourcePlayerId());
					source.decrementCurrentMoney(sendBucks.getAmount());
				}

				break;
			}
			case Connection.ZLoginBegin:
			{
				ZLoginBegin loginBegin = (ZLoginBegin) command;
				//System.out.println("ZLoginBegin: timer: " + loginBegin.getAscendancyTimer() + ". inAscendancy: " + loginBegin.getInAscendancy() + ". ascRand: " + loginBegin.getAscendancyRand() + ". libStormPower: " + loginBegin.getLiberatedStormPower());
				//this.myPlayerId = loginBegin.getPlayerID();
				Territory terr = loginBegin.getTerrData();
				System.err.println("NSBattleClient: ZLoginBegin. playerId=" + loginBegin.getPlayerID());
				System.out.println(terr);

				this.callback.battleLoggedIn();

				break;
			}

			case Connection.ZReadyToRock:
			{
				ZReadyToRock rtr = (ZReadyToRock) command;

				//System.out.println("Ready to rock. " + rtr.getReady());

				if (!this.firstRTR)
				{
					this.firstRTR = true;
					this.connection.sendCommand(rtr);
				}

				// We'll figure out priest sids here. Most cohesive way I can think of is to first
				// sort the players into an array based on login order. Then we just loop and increment
				// priest sid counter if they're NOT a watcher.
				BattlePlayer[] loginOrders = new BattlePlayer[8];
				for (int j = 1; j <= 8; j++)
				{
					if (this.battle.getPlayer(j) != null)
						loginOrders[this.battle.getPlayer(j).getLoginOrder()] = this.battle.getPlayer(j);
				}

				for (int j = 0; j < 8; j++)
				{
					if (loginOrders[j] != null)
					{
						if (!loginOrders[j].isWatcher())
						{
							/*
							NSType priest = NSTypes.findByTypeName("priest");
							NSType residence = NSTypes.findByTypeName("residence");
							
							NSBaseSquid squid = this.battle.createSquid(priest.getTypeNum());
							loginOrders[j].setPriest(squid);
							squid = this.battle.createSquid(residence.getTypeNum());*/
						}
					}
				}

				ZTimeSync timeSync = new ZTimeSync();
				timeSync.setSentClient(System.currentTimeMillis());
				this.connection.sendCommand(timeSync);

				this.callback.battleStarted();
				break;
			}

			case Connection.ZChatLine:
			{
				ZChatLine chatLine = (ZChatLine) command;
				BattlePlayer player = this.battle.getPlayerBySubscriberID(chatLine.getSubscriberID());
				System.out.println(player);
				this.callback.battlePlayerSpeak(player, chatLine.getMessage());

				break;
			}

			case Connection.ZTimeSync:
			{
				ZTimeSync timeSync = (ZTimeSync) command;

				//System.out.println("TimeSync");

				timeSync.setSentClient(System.currentTimeMillis());
				timeSync.setRecvServer(0);

				//protocol.sendCommand(timeSync);
				break;
			}

			case Connection.ZPlayerData:
			{
				ZPlayerData playerData = (ZPlayerData) command;
				int playerID = playerData.getPlayerId();
				BattlePlayer player = this.battle.getPlayer(playerID);

				System.out.println(playerData.getName() + " is here. Login Order: " + playerData.getLoginOrder() + ". Address: " + playerData.getAddress() + ". Flags: " + playerData.get_flags() + ". Player ID: " + playerData.getPlayerId() + ". Predictable count: " + playerData.getPredictableCount());

				if (player == null)
				{
					// TODO:
					player = new BattlePlayer(new Version(10, 78));
					playerData.updatePlayer(player);
					this.battle.addPlayer(player);
				}
				else
					playerData.updatePlayer(player);

				break;
			}

			case Connection.ZKeepAlive:
			{
				ZKeepAlive keepAlive = (ZKeepAlive) command;
				//System.out.println("Keepalive.");
				if (this.firstKeepAlive)
				{
					/*
						ZReadyToRock ready = new ZReadyToRock();
						ready.setReady(2);
						protocol.sendCommand(ready);*/

					this.firstKeepAlive = false;
				}
				else
					this.connection.sendCommand(keepAlive);

				break;
			}

			case Connection.ZDeclareDraw:
			{
				ZDeclareDraw draw = (ZDeclareDraw) command;
				if (draw.getPlayerId() != 0)
				{
					// Flag player as drawed.
					this.battle.playerDraw(draw.getPlayerId());

					// Has everyone drawn?
					if (this.battle.drawSuccess())
					{
						// Send a clean shutdown message to server.
						ZRequestCleanShutdown shutdown = new ZRequestCleanShutdown();
						this.connection.sendCommand(shutdown);
						this.disconnect();
					}
				}

				break;
			}

			case Connection.ZBattleOptions:
			{
				break;
			}

			case Connection.ZLoadFort:
			{
				//ZLoadFort loadFort = (ZLoadFort)command;
				//System.out.println("Load fort: " + loadFort.getName() + ". Density: " + loadFort.getDensity() + ". Flags: " + loadFort.getFortFlags());
				break;
			}

			case Connection.ZLoginReply:
			{
				//ZLoginReply loginReply = (ZLoginReply)command;
				//System.out.println("Early cap timer: " + loginReply.getEarlyCaptureTimer());
				break;
			}

			case Connection.ZInitialMoney:
			{
				ZInitialMoney initialMoney = (ZInitialMoney) command;

				for (int i = 0; i < initialMoney.getNumPlayers(); i++)
				{
					BattlePlayer player = this.battle.getPlayer(i);
					if (player != null)
					{
						System.out.println(player.getNickname() + " has " + initialMoney.getMoney(i) + " bucks.");
						player.setCurrentMoney(initialMoney.getMoney(i));
					}
				}

				break;
			}

			case Connection.ZReconnectState:
			{
				//ZReconectState reconnectState = (ZReconectState)command;
				//System.out.println("Clockmain: " + reconnectState.getClockmain());
				break;
			}

			case Connection.ZFortDataPacket:
			{
				ZFortDataPacket zfd = (ZFortDataPacket) command;
				BattlePlayer player = this.battle.getPlayer(zfd.getPlayerId());

				if (player == null)
				{
					System.err.println("Warning! FortData sent for nonexistant player ID " + zfd.getPlayerId());
					break;
				}

				player.getFortData().create("", true);
				player.getFortData().receiveStrippedImage(zfd.getFortFile());

				System.out.println(player.getNickname() + " predictable count: " + zfd.getPredictableCount());

				player.setWatcher(zfd.getWatcher() == 1);
				break;
			}

			case Connection.ZCreateCanon:
			{
				ZCreateCanon zcc = (ZCreateCanon) command;
				BattlePlayer player = this.battle.getPlayer(zcc.getPlayerId());

				NSType type = Types.findByTypeNum(zcc.getTypeNum());
				if (!type.getTypeName().equalsIgnoreCase("nugget"))
					if (player != null)
						player.decrementCurrentMoney(type.getCost());

				//if(player != null)
				//System.out.println("Create canon: " + zcc.getPlayerId() + " " + type.getName() + " " + type.getCost());

				if (type.getTypeName() == "bridge")
				{
					this.battle.incrementBridgeCount();
					player.setBridgeCount(player.getBridgeCount() + 1);
				}

				for (int j = 0; j < zcc.getSid().length; j++)
				{
					MainSquid s = (MainSquid) this.battle.take(zcc.getTypeNum(), zcc.getSid()[j]);
					s.setHitPoints(type.getMaxHitPoints());
					s.setPlayer(this.battle.getPlayer(zcc.getPlayerId()));
					s.setType(zcc.getTypeNum());

					this.callback.battlePlayerBuild(s);
				}

				if (this.debugMode)
				{
					this.say((zcc.getPlayerId() != 0 ? this.battle.getPlayer(zcc.getPlayerId()).getNickname() : "Global") + ": ZCreateCanon");
					this.say("Typenum: #" + zcc.getTypeNum() + " @ " + zcc.getX() + "," + zcc.getY() + ". Rotation: " + zcc.getRotation() + ". Createflags: " + zcc.getCreateFlags() + ". Special: " + zcc.getSpecial() + ". StreakArrivalTime: " + zcc.getStreakArrivalTime() + "Sid count: " + zcc.getSid().length);
					//this.say("SIDs: " + sids);
				}

				break;
			}

			case Connection.ZPathProcess:
			{
				ZPathProcess pathProcess = (ZPathProcess) command;

				BaseSquid unit = this.battle.getSquid(pathProcess.getAttachToSid());
				if (unit != null)
				{
					//this.say(this.battle.getPlayer(unit.getPlayerID()).getNickname() + " moved his priest.");
				}

				if (this.debugMode)
				{
					this.say("ZPathProcess: " + pathProcess.getFormSid() + ", attach: " + pathProcess.getAttachToSid() + ". " + pathProcess.getSx() + "," + pathProcess.getSy());
				}

				break;
			}

			case Connection.ZDestroySquid:
			{
				this.handleDestroySquid((ZDestroySquid) command);
				break;
			}

			case Connection.ZCompressedUpdateSquid:
			{
				ZCompressedUpdateSquid squidUpdate = (ZCompressedUpdateSquid) command;
				this.handleUpdateSquid(squidUpdate);

				break;
			}

			default:
			{
				System.out.println("Received unhandled zacket Type#" + command.getCommandID());
				break;
			}
		}
	}

	public World getBattle()
	{
		return this.battle;
	}

	public BattlePlayer getMe()
	{
		return this.battle.getPlayerBySubscriberID(this.me.getSubscriberID());
	}

	// We'll lump all our handler code here.
	private void handleUpdateSquid(ZCompressedUpdateSquid update)
	{
		MainSquid s = (MainSquid) this.battle.take(update.getTypeNum(), update.getSid());

		s.setPos(update.getPos());
		s.setHitPoints(update.getHitPoints());
		s.setPlayer(this.battle.getPlayer(update.getPlayerId()));
		s.setFrame(update.getFrame());
		s.setQ(update.getQ());

		System.out.println(s.getTypeStruct().getTypeName() + " pos:" + update.getPos() + " pid:" + update.getPlayerId() + " frame:" + update.getFrame() + " baseflags:" + update.getBaseFlags());
	}

	private void handleDestroySquid(ZDestroySquid destroy)
	{
		BaseSquid s = this.battle.getSquid(destroy.getSid());

		if (s != null)
		{
			int destroyFlags = destroy.getDestroyFlags();
			destroyFlags = destroyFlags << BaseSquid.pfSERVER_DESTROY_SHIFT;

			if (!s.isForm() && (destroyFlags & BaseSquid.pfDIE_SALVAGE) != 0)
			{
				this.battle.reward((MainSquid) s, ((MainSquid) s).getPlayer(), true);
			}

			s.destroy(BaseSquid.pfDEFAULT | BaseSquid.pfSERVER_POP | destroyFlags);
		}

	}

	public BattleOptions getBattleOptions()
	{
		return this.battle.getBattleOptions();
	}

	private BattleListener callback;

	private boolean debugMode;

	private boolean firstRTR, firstKeepAlive;

	//private int myPlayerId;			// The ID we are assigned in-game.
	private World battle;

	private MyByteBuffer fortData;

	private CommandProcessor listener;

	private Connection connection;

	private ZonePlayer me;
}
