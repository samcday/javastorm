package org.sambro.botsy;

import org.javastorm.battle.BattlePlayer;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.Connection;
import org.javastorm.network.ConnectionListener;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZBattleOptions;
import org.javastorm.network.commands.ZPreconnect;

//Each client is so special they get their own thread!
public class BattleProxyClient implements Runnable, ConnectionListener
{
	public BattleProxyClient(ZonePlayer player, BattleProxy server, int gameServerPort)
	{
		this.battleServer = server;
		this.zonePlayer = player;
		this.gameServerPort = gameServerPort;
	}

	public void connection(Connection conn)
	{
		// If this client has moved from one slot in ring to another, destroy their old connection.
		if (this.client != null)
		{
			this.server.disconnect();
			this.client.disconnect();
		}

		this.client = conn;
		//this.client.enableDebug(true);
		//this.client.setPacketLogging(true);

		this.running = true;

		ZPreconnect preconnect = new ZPreconnect();
		preconnect.setServerSubscriberID(this.battleServer.getBotinfo().getSubscriberID());
		preconnect.setCode(1); // 1 means everything is all good.
		this.client.sendCommand(preconnect, false);

		this.server = new Connection();

		if (!this.server.connect("localhost", this.gameServerPort, 2000, true))
		{
			throw new RuntimeException("Server is boned.");
		}

		//this.server.setPacketLogging(true);

		this.thread = new Thread(this, "Battle Proxy Client Thread");
		this.thread.start();
	}

	public int getSubscriberID()
	{
		return this.zonePlayer.getSubscriberID();
	}

	public void run()
	{
		NSCommand command;

		while (this.running)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException ie)
			{
			}

			// See if client is saying anything.
			command = this.client.readCommand();
			if (command != null)
			{
				this.server.sendCommand(command, true, true);
			}

			// See if server is saying anything.
			command = this.server.readCommand();

			if (command != null)
			{
				// We do any piggybacking/overrides here.
				if (command.getCommandID() == Connection.ZBattleOptions)
				{
					// I can't be flecked doing something hacky in ugly Netstorm C++ code, so I'll just
					// do it from here.
					ZBattleOptions options = new ZBattleOptions();
					options.setBattleOptions(this.battleServer.getBattleOptions());
					this.client.sendCommand(options);
				}
				else
				{
					// If we have nothing to do with this zacket, just pass it along.
					this.client.sendCommand(command, true, true);
				}
			}
		}

		this.client.disconnect();
		this.server.disconnect();
	}

	protected BattlePlayer getBattlePlayer()
	{
		return this.battlePlayer;
	}

	protected void shutdown()
	{
		this.running = false;
	}

	private int gameServerPort;

	private Thread thread;

	private boolean running;

	private Connection client;

	private Connection server;

	private BattleProxy battleServer;

	private ZonePlayer zonePlayer;

	private BattlePlayer battlePlayer;

}
