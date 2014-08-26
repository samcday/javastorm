package org.javastorm.network;

import org.javastorm.BoardCoord;
import org.javastorm.Version;
import org.javastorm.World;
import org.javastorm.WorldServer;
import org.javastorm.Netstorm;
import org.javastorm.battle.BattleOptions;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZChalLaunchServer;
import org.javastorm.network.commands.ZCompressedUpdateSquid;
import org.javastorm.network.commands.ZKeepAlive;
import org.javastorm.network.commands.ZReadyToRock;
import org.javastorm.network.commands.ZChalLaunchServer.ZAClient;
import org.javastorm.squids.MainSquid;
import org.javastorm.territory.IslandBuilder;
import org.javastorm.territory.Territory;
import org.javastorm.types.Types;

// The main guts of the battle server. Operation begins by attaching NSZonePlayers who join the ring we are serving on.
// This gives us their subscriber ID, which we can then enter into the ConnectionPool.
public class BattleServer implements WorldServer
{
	public BattleServer(ZonePlayer me, Version version)
	{
		this.clients = new BattleServerClient[8];
		this.bo = new BattleOptions();
		this.me = me;
		this.battleMe = new BattlePlayer(this.me);

		this.world = new World(this);

		this.battleMe.getFlags().setServer(true);
		this.clients[0] = new BattleServerClientStub(this, this.battleMe);
		this.clientCount++;

		this.world.addPlayer(this.battleMe);

		this.started = false;

		// Make sure we're set up for the right client version.
		System.err.println("NSBattleServer: hosting a " + version + " game.");
		this.serverVersion = version;
		if (version.getMinorVersion() == 79)
		{
			System.err.println("NSBattleServer: got a .79 client, switching world to .79 sids.");
			this.world.use79SquidCounters();
		}
	}

	public void addPlayer(ZonePlayer player)
	{

		for (int i = 0; i < this.clients.length; i++)
		{
			if (this.clients[i] == null)
			{
				this.clients[i] = new BattleServerClient(this, player);
				ConnectionPool.get().attachListener(this.clients[i]);
				this.clientCount++;

				this.world.addPlayer(this.clients[i].getBattlePlayer());

				return;
			}
		}
	}

	public void removePlayer(ZonePlayer player)
	{
		for (int i = 0; i < this.clients.length; i++)
		{
			if (this.clients[i].getZonePlayer() == player)
			{
				ConnectionPool.get().detachListener(this.clients[i]);

				this.world.removePlayer(this.clients[i].getBattlePlayer());

				this.clients[i] = null;
				this.clientCount--;

				return;
			}
		}
	}

	public void start(ZChalLaunchServer zcls)
	{
		try
		{
			ZAClient[] clients = zcls.getClients();
			assert(clients.length > 0 && clients.length < 9);

			System.err.println("NSBattleServer: starting...");

			for(BattleServerClient client : this.clients)
			{
				if(client == null) continue;
				if(client.getBattlePlayer() == this.battleMe) continue;

				client.getBattlePlayer().setWatcher(true);
			}

			this.world.setBattleOptions(this.bo);

			this.thePrizeTerr = zcls.getBountyTerr();
			this.thePrizeTerr.setCanon(2);
			
/*			this.thePrizeTerr.setExists(true);
			this.thePrizeTerr = new NSTerritory();
			this.thePrizeTerr.setCanon(2);
			this.thePrizeTerr.setExists(true);
			this.thePrizeTerr.setRandSeed(73);*/

			System.err.println("NSBattleServer: processing ZCLS client list.");
			for(ZAClient zac : clients)
			{
				assert(zac.getSubscriberId() != 0);

				BattleServerClient client = this.getClientBySubscriberId(zac.getSubscriberId());
				assert(client != null);

				BattlePlayer player = client.getBattlePlayer();
				player.setPlayerIndex(Netstorm.FIRST_REAL_PLAYER + zac.getPlayerId());
				if(zac.get_flags() == 1) player.getFlags().setFirewalled(true);
			}
			
			System.err.println("NSBattleServer: Building map.");
			IslandBuilder ib = new IslandBuilder();
			ib.initAsBattleMode(this.world, null);
			ib.createBountyIsland(this.thePrizeTerr);

			this.debugCreateManyPriests();

			System.err.println("NSBattleServer: Spawning fake player stubs.");

			for (int i = 1; i <= 8; i++)
			{
				boolean found = false;

				for (BattleServerClient client : this.getClients())
				{
					if (client.getBattlePlayer().getPlayerIndex() == i)
						found = true;
				}

				if (!found)
				{
					System.err.println("NSBattleServer: adding a stub player for index: " + i);
					BattlePlayer fakePlayer = new BattlePlayer(this.getServerVersion());
					fakePlayer.setPlayerIndex(i);
					fakePlayer.setIp(new int[]
					{ 127, 0, 0, 1 });
					fakePlayer.setNickname("FAKEPLAYER " + i);
					fakePlayer.setSubscriberID(fakePlayer.getNickname().hashCode());
					BattleServerClientStub stub = new BattleServerClientStub(this, fakePlayer);

					for (int j = 0; j < this.clients.length; j++)
					{
						if (this.clients[j] == null)
						{
							this.clients[j] = stub;
							break;
						}
					}

					this.clientCount++;
				}
			}

			System.err.println("NSBattleServer: checking stubs.");
			for (BattleServerClient client : this.getClients())
			{
				if(client instanceof BattleServerClientStub)
				{
					BattleServerClientStub stub = (BattleServerClientStub)client;
					ib = new IslandBuilder();
					ib.initAsBattleMode(this.world, stub.getBattlePlayer());
					ib.createBattleIsland();
				}
			}

			// We send keep alives to all connected clients every 3 seconds.
			this.keepAliveThread = new KeepAliveThread();
			this.keepAliveThread.start();

			System.err.println("NSBattleServer: started!");
			this.started = true;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	// Called when battle is over.
	public void stop()
	{
		this.keepAliveThread = null;

		for (BattleServerClient client : this.getClients())
		{
			ConnectionPool.get().detachListener(client);
			client.disconnect();
			this.clients = null;
		}
	}

	// Called at the start of a battle. Sends stub player info of all connected players to a player.
	public void sendStubPlayerData(Connection connection)
	{
		BattlePlayer player;

		for (BattleServerClient client : this.getClients())
		{
			player = client.getBattlePlayer();
			if (player.getSubscriberID() == 0)
				continue;

			System.err.println("NSBattleServer: sending player stub: " + player.getNickname() + ":" + player.getSubscriberID());
			connection.sendCommand(player.getPlayerData());
		}
	}

	public void broadcastEveryoneReadyToRock()
	{
		//debugCreateManyPriests();

		try
		{
			Thread.sleep(2000);
		}
		catch(InterruptedException ie)
		{}

		ZReadyToRock zrtr = new ZReadyToRock();
		// Lol messy. Fuck Java and its lack of unsigned datatype support.
		zrtr.setReady((long) ((long) 1 << (long) 31));
		this.sendToAll(zrtr, true);		
	}

	private void debugCreateManyPriests()
	{
		int x = 15, y = 15;
		int player = 0;

		for(int i = 0; i < 8; i++)
		{
			x = 15;
			for(int j = 0; j < 10; j++)
			{
				player ++;
				if(player > 7)
					player = 0;
				
				BattlePlayer p = null;
				if(this.clients[i] != null)
					p = this.clients[i].getBattlePlayer();

				if(p == null) continue;

				MainSquid s = (MainSquid) this.world.createSquid(Types.findByTypeName("priest").getTypeNum(), 0);
				s.setPlayer(p);
				s.setPos(new BoardCoord(x, y));
				int frame = s.getTypeStruct().getAnyBestFrame('P', 'P');
				s.setFrame(frame);
				s.pop(MainSquid.pfFROM_FORT_DATA);
				x++;
			}
			y++;
		}
	}

	public void sendCurrentReadyToRock()
	{
		this.sendCurrentReadyToRock(null);
	}
 
	public void sendCurrentReadyToRock(BattleServerClient destPlayer)
	{
		System.err.println(this + ": Sending current RTR.");
		int ready = 0;

		ZReadyToRock zrtr = new ZReadyToRock();
		BattleServerClient[] clients = this.getClients();

		for (BattleServerClient client : clients)
		{
			BattlePlayer player = client.getBattlePlayer();
			if (player.getFlags().isReadyToRock())
				ready |= (1 << player.getPlayerIndex());
		}

		zrtr.setReady(ready);

		if (destPlayer == null)
			this.sendToAll(zrtr);
		else
			destPlayer.sendCommand(zrtr);
	}

	// Send the forts of all currently logged in people to specified person.
	public void sendForts(BattleServerClient dest)
	{
		BattleServerClient[] clients = this.getClients();

		for (int i = 0; i < this.totalEverLoggedIn; i++)
		{
			for (BattleServerClient client : clients)
			{
				if (client == dest)
					continue;

				if (client.getBattlePlayer().getLoginOrder() == i)
				{
					if (client.getBattlePlayer().getFlags().hasEverLoggedIn())
					{
						System.err.println("Sending " + client.getBattlePlayer().getNickname() + "'s fort to " + dest.getBattlePlayer().getNickname());
						client.sendFort(dest);
					}
				}
			}
		}
	}

	public int countPlayersDisconnected()
	{
		BattlePlayer player;
		int count = 0;

		for (BattleServerClient client : this.clients)
		{
			if (client == null)
				continue;
			player = client.getBattlePlayer();
			if (player.getFlags().hasEverLoggedIn() && player.getFlags().isDisconnected())
				count++;
		}

		return count;
	}

	public int countPlayersValid()
	{
		BattlePlayer player;
		int count = 0;

		for (BattleServerClient client : this.clients)
		{
			if (client == null)
				continue;
			player = client.getBattlePlayer();
			if (player.getSubscriberID() != 0)
				count++;
		}

		return count;
	}

	public int countPlayersRTR()
	{
		BattlePlayer player;
		int count = 0;

		for (BattleServerClient client : this.clients)
		{
			if (client == null)
				continue;
			player = client.getBattlePlayer();
			if (player.getFlags().isReadyToRock() && player.getFlags().hasEverLoggedIn() && player.getFlags().isCurrentlyLoggedIn())
				count++;
		}

		return count;
	}

	public BattleServerClient getClientByPlayerId(int playerId)
	{
		for(BattleServerClient client : this.getClients())
		{
			if(client.getBattlePlayer().getPlayerIndex() == playerId)
				return client;
		}

		return null;
	}
	
	public BattleServerClient getClientBySubscriberId(int subId)
	{
		for(BattleServerClient client : this.getClients())
		{
			if(client.getBattlePlayer().getSubscriberID() == subId)
				return client;
		}

		return null;
	}

	public BattleServerClient[] getClients()
	{
		BattleServerClient[] clients = new BattleServerClient[this.clientCount];
		int i = 0;

		for (BattleServerClient client : this.clients)
		{
			if (client == null)
				continue;
			clients[i++] = client;
		}

		return clients;
	}

	public void updateSquid(ZCompressedUpdateSquid zcus)
	{
		MainSquid s = (MainSquid) this.world.getSquid(zcus.getSid());
		String squid = s == null ? "WTF!" : s.getTypeStruct().getTypeName();
		String player = s.getPlayer() == null ? "NOONE" : s.getPlayer().getNickname();

		System.err.println("xmitting stuff." + squid + " is " + player + " baseflags: " + zcus.getBaseFlags());
		this.sendToAll(zcus);
	}

	public void sendTo(int playerId, NSCommand cmd)
	{
		BattleServerClient client = this.getClientByPlayerId(playerId);
		assert(client != null);

		System.err.println("xmitting stuff." + cmd + " is " + client.getBattlePlayer().getNickname());
		
		client.sendCommand(cmd);
	}

	public void sendToAll(NSCommand command)
	{
		this.sendToAll(command, false);
	}

	private void sendToAll(NSCommand command, boolean verbose)
	{
		for (BattleServerClient client : this.clients)
		{
			if (client == null)
				continue;

			if (verbose)
				System.err.println("Sending " + command + " to " + client.getBattlePlayer().getNickname());

			client.sendCommand(command);
		}
	}

	public synchronized int getTotalEverLoggedIn(boolean increment)
	{
		int totalEverLoggedIn = this.totalEverLoggedIn;

		if (increment)
			this.totalEverLoggedIn++;

		return totalEverLoggedIn;
	}

	public Version getServerVersion()
	{
		return this.serverVersion;	
	}
	
	public BattleOptions getBattleOptions()
	{
		return this.bo;
	}

	public ZonePlayer getMe()
	{
		return this.me;
	}

	public BattlePlayer getBattleMe()
	{
		return this.battleMe;
	}

	public Territory getPrizeTerr()
	{
		return this.thePrizeTerr;
	}

	public World getWorld()
	{
		return this.world;
	}

	public boolean isStarted()
	{
		return this.started;
	}

	private class KeepAliveThread extends Thread
	{
		public void run()
		{
			while (Thread.currentThread() == this)
			{
				try
				{
					Thread.sleep(3000);
				}
				catch (InterruptedException ie)
				{

				}

				ZKeepAlive keepAlive = new ZKeepAlive();
				keepAlive.setPing(0);

				sendToAll(keepAlive);
			}
		}
	}

	private Version serverVersion;
	
	private KeepAliveThread keepAliveThread;

	private Territory thePrizeTerr;

	private World world;

	private int totalEverLoggedIn;

	private BattleServerClient clients[];

	private int clientCount = 0;

	private BattleOptions bo;

	private ZonePlayer me;

	private BattlePlayer battleMe;

	private boolean started;
}
