package org.javastorm.network;

import java.io.File;
import java.io.PrintStream;

import org.javastorm.NSUtil;
import org.javastorm.Version;
import org.javastorm.World;
import org.javastorm.battle.BattleOptions;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.battle.BattlePlayerFlags;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.fort.FortData;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZBattleOptions;
import org.javastorm.network.commands.ZFortDataPacket;
import org.javastorm.network.commands.ZInitialMoney;
import org.javastorm.network.commands.ZLoadFort;
import org.javastorm.network.commands.ZLoginBegin;
import org.javastorm.network.commands.ZLoginReply;
import org.javastorm.network.commands.ZPreconnect;
import org.javastorm.network.commands.ZReadyToRock;
import org.javastorm.network.commands.ZRequestLogin;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.territory.IslandBuilder;
import org.javastorm.util.MyByteBuffer;

public class BattleServerClient implements ConnectionListener, CommandListener
{
	public BattleServerClient(BattleServer battleServer, ZonePlayer player)
	{
		this.battleServer = battleServer;
		this.zonePlayer = player;
		this.battlePlayer = new BattlePlayer(this.zonePlayer);
	}

	public int getSubscriberID()
	{
		return this.zonePlayer.getSubscriberID();
	}

	public ZonePlayer getZonePlayer()
	{
		return this.zonePlayer;
	}

	public void onConnectionDead()
	{
		// TODO:
		System.err.println(this + ": connection dead.");
	}

	public void onException(Throwable t)
	{
		// TODO:
		t.printStackTrace(System.err);

		//this.connection.disconnect();
	}

	public void connection(Connection connection)
	{
		// Dunno if this could happen - but safe to do this anyway.
		if (this.connection != null)
		{
			if (this.connection.connected())
			{
				this.connection.disconnect();
				this.processor.stop();
			}
		}

		this.connection = connection;
		
		try
		{
			File file = new File("D:\\botsyserver " + this.getZonePlayer().getNickname() + ".log");
			file.createNewFile();
			this.connection.enableDebug(new PrintStream(file));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		this.connection.setMinorVersion(this.battleServer.getServerVersion().getMinorVersion());

		this.processor = new CommandProcessor();
		this.processor.start(this.connection, this);
	}

	public void disconnect()
	{
		this.connection.disconnect();
	}

	public void onCommand(NSCommand command)
	{
		switch (command.getCommandID())
		{
			case Connection.ZPreconnect:
			{
				this.handlePreconnect((ZPreconnect) command);

				break;
			}
			case Connection.ZRequestLogin:
			{
				this.handleRequestLogin((ZRequestLogin) command);

				break;
			}
			case Connection.ZReadyToRock:
			{
				this.handleReadyToRock((ZReadyToRock) command);

				break;
			}
			case Connection.ZTimeSync:
			{
				// TODO
				break;
			}
			case Connection.ZKeepAlive:
			{
				// TODO
				break;
			}

			default:
			{
				System.err.println(this + ": received unhandled command: " + command);
			}
		}
	}

	private void handlePreconnect(ZPreconnect zp)
	{
		System.err.println(this + ": handling ZPreconnect.");
		// Do some checks.
		boolean responseCode = false;

		ZPreconnect zpr = new ZPreconnect();

		if (zp.getSlot() == zonePlayer.getSlot() && zp.getRing() == zonePlayer.getRing() && zp.getPlayerSubscriberID() == zonePlayer.getSubscriberID() && zp.getSlot() == zonePlayer.getSlot())
		{
			responseCode = true;
		}

		this.battlePlayer.setPlayerIndex(zp.getPlayerIndex());

		zpr.setCode(responseCode ? 1 : 0);
		zpr.setServerSubscriberID(this.battleServer.getMe().getSubscriberID());

		this.connection.sendCommand(zpr);
	}

	private void handleRequestLogin(ZRequestLogin zrl)
	{
		System.err.println(this + ": Handling login request.");

		// Don't do anything until server has started. This will likely only happen on lans that the client gets the loginrequest packet
		// in before the server has even received the ZChalLaunchServer lol.
		while (!this.battleServer.isStarted())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		// For good measure.
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		boolean alreadyLoggedIn = this.battlePlayer.getFlags().hasEverLoggedIn();

		if(!this.checkLogin())
		{
			this.sendLoginReply(false);
			this.disconnect();
			return;
		}

		// Begin the login process.
		this.sendLoginBegin();
		
		// Send the client the battle options.
		this.sendBattleOptions();

		// Send player stub data to this client.
		this.battleServer.sendStubPlayerData(this.connection);

		this.sendLoadFort();

		// PREPARE HIS FORT DATA FILE IF HE IS NEWLY ARRIVED
		if (!alreadyLoggedIn)
		{
			// Debug, save fort data out for inspection.
			FortData fd = new FortData(this.battlePlayer);
			fd.create("debug_" + this.battlePlayer.getNickname() + ".fort", false);
			fd.receiveStrippedImage(zrl.getFortData());

			this.getNewPlayerFortData(zrl.getFortData());
		}

		this.sendPlayerData();

		// Great success! Send login reply now.
		this.sendLoginReply(true);

		// Let the client know how much initial sp everyone has.
		this.sendInitialMoney();

		// NOW SEND EVERYONE'S DATA TO THE NEW PLAYER
		// HE GETS HIS OWN DATA HERE IF HE HAS ALREADY LOGGED IN

		// RIGHT HERE WE ARE GOING TO SEND EVERYONE FORT DATA
		
		// Send everyone elses forts to client.
		this.battleServer.sendForts(this);

		// HEY NEW PLAYER, HERE IS THE STATE OF THE SERVER SQUIDS!
		this.sendAllServerSquids();
				
		// OK, NOW THE NEW PLAYER'S GAME STATE IS EXACTLY IDENTICAL TO THE EXISTING
		// PLAYERS! (Theoretically speaking...)

		// IF HE IS BRAND NEW, WE NOW SEND HIS FORT TO HIM AND EVERYONE ELSE!
		if(!alreadyLoggedIn)
			this.sendMyFort();

		this.makeBattleIsland();

		// This player is Ready to Rock!
		//this.battlePlayer.setReadyToRock(true);
		this.battleServer.sendCurrentReadyToRock(this);
		//this.battleServer.broadcastEveryoneReadyToRock();

		// TODO:
		//this.checkStartupSpecialcase();
		
		System.err.println(this.battlePlayer + ": ever logged in: " + this.battlePlayer.getFlags().hasEverLoggedIn() + ". Currently logged in: " + this.battlePlayer.getFlags().isCurrentlyLoggedIn());
	}

	private void handleReadyToRock(ZReadyToRock rtr)
	{
		System.err.println(this + ": Handling ready to rock.");

		this.battlePlayer.getFlags().setReadyToRock(true);

		//System.out.println(this.battleServer.countPlayersValid() - this.battleServer.countPlayersDisconnected() + " players. RTR: " + this.battleServer.countPlayersRTR());
		if ((this.battleServer.countPlayersValid() - this.battleServer.countPlayersDisconnected()) == this.battleServer.countPlayersRTR())
		{
			System.err.println(this + ": Everyone is ready to rock.");
			this.battleServer.broadcastEveryoneReadyToRock();
		}
		else
			this.battleServer.sendCurrentReadyToRock();
	}

	private boolean checkLogin()
	{
		BattlePlayerFlags flags = this.battlePlayer.getFlags();

		if(!flags.isCurrentlyLoggedIn())
		{
			flags.setCurrentlyLoggedIn(true);
			flags.setEverLoggedIn(true);
			flags.setDisconnected(false);

			return true;
		}
		else
		{
			return false;
		}
	}

	private ZFortDataPacket createFortDataPacket()
	{
		ZFortDataPacket zfdp = new ZFortDataPacket();

		MyByteBuffer fortData = this.battlePlayer.getFortData().copyTo();
		fortData.setPosition(0);

		zfdp.setFortFile(fortData);
		zfdp.setPredictableCount(this.battlePlayer.getPredictableCount());
		zfdp.setWatcher(this.battlePlayer.isWatcher());
		zfdp.setPlayerId(this.battlePlayer.getPlayerIndex());

		return zfdp;
	}

	// Send client fort to specified person.
	public void sendFort(BattleServerClient client)
	{
		client.sendCommand(this.createFortDataPacket());
	}

	// Send my fort to all connected clients.
	public void sendMyFort()
	{
		ZFortDataPacket zfdp = this.createFortDataPacket();
		zfdp.setPredictableCount(this.battleServer.getWorld().getPredictableCount());
		this.sendToAll(zfdp, true);
	}
	
	private void sendAllServerSquids()
	{
		World world = this.battleServer.getWorld();
		MainSquid s;

		// TODO: send all surfaces first.
		for(int i = world.getFirstServerSid(); i < world.getFirstPredictable(); i++)
		{
			
		}

			for(int i = world.getFirstServerSid(); i < world.getFirstPredictable(); i++)
			{
				s = (MainSquid)world.getSquid(i);
				if(s == null) continue;

				if(s.getTypeStruct().getTypeName().equalsIgnoreCase("Isle"))
				{
					System.out.println(":S");
				}

				if(!s.isFree() && !s.isForm() && !s.isSurface())
				{
					this.sendSquid(s);
				}
			}
	}

	private void sendSquid(MainSquid s)
	{
		int popFlags = (s.isAbstract() ? BaseSquid.pfCREATED_ABSTRACT : BaseSquid.pfCREATED );
		if(s.isAstral())
		{
			popFlags = BaseSquid.pfCREATED_ASTRAL;
		}

		s.transmitTo(this.getBattlePlayer().getPlayerIndex(), BaseSquid.TRANSMIT_MESSAGE_UPDATE, popFlags);
		this.recurseSendChildren(s);
	}

	private void recurseSendChildren(MainSquid s)
	{
		// TODO:
	}

	private void sendPlayerData()
	{
		// Fix up flags and xmit.
		this.battlePlayer.getFlags().setEverLoggedIn(true);
		this.battlePlayer.getFlags().setCurrentlyLoggedIn(true);
		this.battlePlayer.getFlags().setDisconnected(false);
		this.sendToAll(this.battlePlayer.getPlayerData(), true);

		// Send everyone elses data to this player.
		BattleServerClient[] clients = this.battleServer.getClients();
		for (BattleServerClient client : clients)
		{
			this.connection.sendCommand(client.getBattlePlayer().getPlayerData());
		}
	}

	private void sendLoginBegin()
	{
		ZLoginBegin zlb = new ZLoginBegin();

		zlb.setAscendancyRand(0);
		zlb.setInAscendancy(false);
		zlb.setPlayerID(this.battlePlayer.getPlayerIndex());
		zlb.setLiberatedStormPower(0);
		zlb.setTerrData(this.battleServer.getPrizeTerr());

		System.err.println(this.battleServer.getPrizeTerr());

		this.connection.sendCommand(zlb);
	}

	private void sendBattleOptions()
	{
		System.err.println(this + ": sending ZBattleOptions.");

		BattleOptions bo = this.battleServer.getBattleOptions();
		ZBattleOptions zbo = new ZBattleOptions();

		zbo.setRing(0);
		zbo.setBattleOptions(bo);

		this.connection.sendCommand(zbo);
	}

	private void sendLoadFort()
	{
		System.err.println(this + ": sending ZLoadFort.");

		ZLoadFort zlf = new ZLoadFort();

		zlf.setName("$Random");
		zlf.setFortFlags(0);
		zlf.setDensity(NSUtil.fastRandomInt(30, 50));

		this.connection.sendCommand(zlf);
	}

	private void sendLoginReply(boolean success)
	{
		System.err.println(this + ": sending ZLoginReply.");
		ZLoginReply zlr = new ZLoginReply();
		zlr.setCode(success ? 1 : 0);

		Version version = this.battleServer.getServerVersion();
		zlr.setServerMajorVersion(version.getMajorVersion());
		zlr.setServerMinorVersion(version.getMinorVersion());
		zlr.setEarlyCaptureTimer(100);

		this.connection.sendCommand(zlr);
	}

	private void sendInitialMoney()
	{
		System.err.println(this + ": sending ZInitialMoney.");

		ZInitialMoney zim = new ZInitialMoney();

		zim.setNumPlayers(8);

		for (int i = 1; i <= 8; i++)
		{
			zim.setMoney(i, 6500);
		}

		this.connection.sendCommand(zim);
	}

	private void makeBattleIsland()
	{
		System.err.println(this + ": Making battle island.");
		IslandBuilder ib = new IslandBuilder();
		ib.initAsBattleMode(this.battleServer.getWorld(), this.battlePlayer);
		ib.createBattleIsland();
	}

	// Sends a packet out to all connected clients excluding myself.
	private void sendToAll(NSCommand command, boolean includeMyself)
	{
		BattleServerClient[] clients = this.battleServer.getClients();

		for (BattleServerClient client : clients)
		{
			if (client == null)
				continue;
			if (client.getConnection() == null)
				continue;
			if (client == this && !includeMyself)
				continue;

			client.getConnection().sendCommand(command);
		}
	}

	private void getNewPlayerFortData(MyByteBuffer buffer)
	{
		this.battlePlayer.setLoginOrder(this.battleServer.getTotalEverLoggedIn(true));
		this.battlePlayer.setPredictableCount(this.battleServer.getWorld().getPredictableCount());

		this.battlePlayer.getFortData().create("NONAME", true);
		this.battlePlayer.getFortData().receiveStrippedImage(buffer);
	}

	public BattlePlayer getBattlePlayer()
	{
		assert(this.battlePlayer != null);

		return this.battlePlayer;
	}

	public Connection getConnection()
	{
		return this.connection;
	}

	public void sendCommand(NSCommand command)
	{
		if(this.connection != null)
			this.connection.sendCommand(command);
	}

	public String toString()
	{
		return "ZBattleServerClient$" + this.battlePlayer.getNickname();
	}

	private BattleServer battleServer;

	private ZonePlayer zonePlayer;

	private Connection connection;

	private CommandProcessor processor;

	protected BattlePlayer battlePlayer;
}
