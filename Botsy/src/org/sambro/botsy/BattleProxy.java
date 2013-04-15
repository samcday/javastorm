package org.sambro.botsy;

import java.net.Socket;

import org.javastorm.battle.BattleOptions;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.battle.BattlePlayerFlags;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.Connection;
import org.javastorm.network.ConnectionPool;
import org.javastorm.network.commands.ZBattleOptions;
import org.javastorm.network.commands.ZChalLaunchServer;

// NSBattleProxy just proxies BotsyStorm
// my hacked .exe for official serving ;)
public class BattleProxy implements Runnable
{
	public boolean init(OldServerBotsy bot, Socket exe)
	{
		this.battleOptions = new BattleOptions();
		this.bot = bot;
		this.clients = new BattleProxyClient[8];
		this.gameServerPort = exe.getPort();
		this.server = new Connection();
		if (!this.server.init(exe))
		{
			throw new RuntimeException("Server is boned.");
		}

		// Create a BattleClient to act on our behalf.
		ZonePlayer botPlayer = bot.getMe();
		this.addPlayer(botPlayer); // Add ourselves to this battle.
		//this.me = new NSBattleClientOld(bot);
		//this.me.preconnect(bot.challengeServer.getRing());

		return true;
	}

	public void run()
	{

	}

	// Adds a zone player to this Battle.
	public void addPlayer(ZonePlayer player)
	{
		for (int i = 0; i < this.clients.length; i++)
		{
			if (this.clients[i] == null)
			{
				this.clients[i] = new BattleProxyClient(player, this, this.gameServerPort);
				ConnectionPool.get().attachListener(this.clients[i]);
				return;
			}
		}
	}

	// Adds a zone player to this Battle.
	public void removePlayer(ZonePlayer player)
	{
		for (int i = 0; i < this.clients.length; i++)
		{
			if (this.clients[i] != null)
			{
				if (this.clients[i].getSubscriberID() == player.getSubscriberID())
				{
					this.clients[i].shutdown();
					ConnectionPool.get().detachListener(this.clients[i]);
					this.clients[i] = null;
					return;
				}
			}
		}
	}

	protected BotAttributes getBotinfo()
	{
		return bot.botinfo;
	}

	public BattleOptions getBattleOptions()
	{
		return this.battleOptions;
	}

	public void setBattleOptions(BattleOptions options)
	{
		this.battleOptions = options;
	}

	protected void sendPlayerData(Connection protocol)
	{
		/*
		for(int i = 0; i < this.clients.length; i++)
		{
			if(this.clients[i] != null)
			{
				if(this.clients[i].getBattlePlayer() != null)
				{
					ZPlayerData data = new ZPlayerData();
					data.setPlayer(this.clients[i].getBattlePlayer());
					protocol.sendCommand(data);
				}
			}
			
			ZPlayerData data = new ZPlayerData();
			data.setName("Bob");
			data.setPlayerId(i);
			data.setSubscriberID(1000 + i);
			data.set_flags(NSBattleServerClient.PF_EVER_LOGGED_IN | NSBattleServerClient.PF_CURRENTLY_LOGGED_IN);
			protocol.sendCommand(data);
		}
		
		ZPlayerData data = new ZPlayerData();
		data.setPlayer(this.serverPlayer);
		protocol.sendCommand(data);*/
	}

	protected synchronized int getLoginOrder()
	{
		return this.loginCount++;
	}

	public void start(ZChalLaunchServer launch)
	{
		ZBattleOptions battleOps = new ZBattleOptions();
		battleOps.setBattleOptions(this.battleOptions);
		this.server.sendCommand(battleOps);

		// TODO:
		this.serverPlayer = new BattlePlayer(this.bot.botinfo);
		
		BattlePlayerFlags flags = this.serverPlayer.getFlags();
		flags.setServer(true);
		flags.setEverLoggedIn(true);
		flags.setCurrentlyLoggedIn(true);
		flags.setReadyToRock(true);
		//this.serverPlayer.setPlayerIndex(this.bot.findPlayerBySubscriberID(this.bot.botinfo.getSubscriberID()).getSlot() + 1);

		// We trigger the slave .exe by sending a ZChalLaunchServer command, gogo hacks.
		this.server.sendCommand(launch);

		// Now we send a login request for our client.
		//this.me.startBattle();

		this.started = true;
	}

	public boolean started()
	{
		return this.started;
	}

	private int gameServerPort;

	private int loginCount; // Used to pass out login orders to clients.

	//private NSBattleClient me;

	private BattlePlayer serverPlayer;

	private boolean started = false;

	private BattleProxyClient clients[];

	private OldServerBotsy bot;

	private BattleOptions battleOptions;

	// This is Botsy's connection to .exe.
	private Connection server;
}
