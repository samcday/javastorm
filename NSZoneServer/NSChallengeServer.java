import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Vector;

import org.javastorm.NSBoardCoord;
import org.javastorm.NSPlayerCoreData;
import org.javastorm.NSUtil;
import org.javastorm.challenge.NSZone;
import org.javastorm.challenge.NSZonePlayer;
import org.javastorm.challenge.NSZoneRing;
import org.javastorm.network.NSConnection;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZChalBattleDel;
import org.javastorm.network.commands.ZChalLaunchClient;
import org.javastorm.network.commands.ZChalLaunchServer;
import org.javastorm.network.commands.ZChalPlayerDel;
import org.javastorm.network.commands.ZChalRequestLogin;
import org.javastorm.network.commands.ZChalServerInfo;
import org.javastorm.network.commands.ZRootReplyQuery;
import org.javastorm.network.commands.ZChalLaunchServer.ZAClient;
import org.javastorm.territory.NSTerritory;

public class NSChallengeServer implements Runnable
{
	/*public static void main(String[] args)
	{
		NSChallengeServer zoneserver = new NSChallengeServer(6800);
		zoneserver.start();
	}*/

	public NSChallengeServer(int port)
	{
		this.clients = new Vector<NSChallengeServerClient>();
		this.port = port;

		this.initialize();

		this.initRings();
	}

	public void createZombies()
	{
		NSChallengeServerClient client;

		for (int i = 0; i < 20; i++)
		{
			NSTerritory terr = new NSTerritory(7, 0, false, false, false, true, 13, 1, 1, 24, 66, 6);

			client = new NSChallengeServerClient(this);
			ZChalRequestLogin zcrl = new ZChalRequestLogin();
			zcrl.setClientMajorVersion(10);
			zcrl.setClientMinorVersion(78);
			zcrl.setName("ZOMBIE");
			zcrl.setSubscriberID(666 + i);
			zcrl.setTerr(terr);
			zcrl.setPlayerData(new NSPlayerCoreData());
			zcrl.setCompatibleMinorVersion(78);
			zcrl.setPlayerLevel(6);

			client.onCommand(zcrl);
		}
	}

	private void initialize()
	{
		// Create zones.
		this.zones = new NSZone[10];
		for (int i = 0; i < this.zones.length; i++)
		{
			this.zones[i] = new NSZone(i + 1);
			this.zones[i].setAvgLevel(0);
			this.zones[i].setInfo("sambrorox0r");
			this.zones[i].setNumPlayers(0);
		}

		// This shit.
		// Is nasty.
		// I made the right decision by not trying to understand any of this shit.
		float bs = (float) Math.max(CHAL_RING_H_IN_TILES, CHAL_RING_W_IN_TILES);
		float cs = (float) Math.max(CHAL_ZONE_H_IN_TILES, CHAL_ZONE_W_IN_TILES);
		float ps = (float) Math.max(MAX_TERR_W_IN_TILES, MAX_TERR_H_IN_TILES);
		int nGrid = (int) Math.floor(Math.sqrt((float) CL_NUM_CHAL_PLAYERS / 2));
		float gr = (float) ((nGrid * ps) / (2.0f * Math.cos(Math.PI / 4.0)));
		float c = ps + bs + ps + cs * 2 + gr;
		float br = gr + cs * CL_NUM_ZONE_RINGS + ps + bs / 2.0f;

		// Sort out player positioning.
		int i = 1;
		this.playerPosList = new NSBoardCoord[CL_NUM_CHAL_PLAYERS];
		while (i < CL_NUM_CHAL_PLAYERS)
		{
			int x, y;
			for (y = 0; y < nGrid && i < CL_NUM_CHAL_PLAYERS; ++y)
			{
				for (x = 0; x < nGrid && i < CL_NUM_CHAL_PLAYERS; ++x)
				{
					this.playerPosList[i] = new NSBoardCoord((float) x * ps + c - nGrid * ps / 2.0f + CLIENT_X_FUDGE, (float) y * ps + c - nGrid * ps / 2.0f - ((i % 2 > 0) ? 0 : PLAYER_Y_FUDGE));
					++i;
				}
			}
		}

		// Sort out battle ring positioning.
		float dt = (float) (2.0 * Math.PI / (CL_NUM_CHAL_BATTLES - 1));
		float t;
		this.battlePosList = new NSBoardCoord[CL_NUM_CHAL_BATTLES];
		for (i = 0, t = 0.0f; i < CL_NUM_CHAL_BATTLES; ++i, t += dt)
		{
			this.battlePosList[i] = new NSBoardCoord((float) (br * Math.cos(t)) + c + CLIENT_X_FUDGE, (float) (br * Math.sin(t)) + c);
		}

		// Calc the radii of the challenge zone rings
		this.chalZoneRadius = new float[CL_NUM_ZONE_RINGS];
		for (i = 0; i < CL_NUM_ZONE_RINGS; ++i)
		{
			this.chalZoneRadius[i] = gr + cs * (CL_NUM_ZONE_RINGS - 1 - i) + cs / 2.0f;
		}

		this.chalCenter = new NSBoardCoord(c + CLIENT_X_FUDGE, c);

		this.activeMapH = (int) (c * 2);
		this.activeMapW = (int) (c * 2 + CLIENT_X_FUDGE * 2);
	}

	private void initRings()
	{
		NSZoneRing ring;

		this.rings = new NSZoneRing[CL_NUM_CHAL_BATTLES];

		for (int i = 0; i < this.rings.length; i++)
		{
			ring = this.rings[i] = new NSZoneRing(i + 1);
			ring.setFlags(NSZoneRing.CBF_VALID);
			ring.setPos(this.battlePosList[i]);
		}
	}

	public void start()
	{
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void run()
	{
		try
		{
			this.server = new ServerSocket(this.port);
		}
		catch (IOException ioe)
		{
			System.out.println("Error creating server socket.");
			ioe.printStackTrace();
			System.exit(-1);
		}

		while (Thread.currentThread() == this.thread)
		{
			try
			{
				new NSChallengeServerClient(this, this.server.accept());
			}
			catch (IOException ioe)
			{
				System.out.println("Error while blocking for connection.");
				ioe.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void addClient(NSChallengeServerClient client)
	{
		this.clients.add(client);
	}

	public void removeClient(NSChallengeServerClient client)
	{
		this.sendPlayerDisconnect(client);
		this.clients.remove(client);
	}

	public int getUnusedPlayerIndex()
	{
		NSChallengeServerClient[] clients = this.getConnectedClients();
		boolean found;

		// See if we can find an index not being used by connected clients.
		for (int i = 1; i <= clients.length; i++)
		{
			found = false;
			for (NSChallengeServerClient client : clients)
			{
				if (client.getPlayer() == null)
					continue;
				if (client.getPlayer().getPlayerIndex() == i)
					found = true;
			}
			if (!found)
				return i;
		}

		// Player indexes are mapped to clients perfectly at the moment, just return a new index.
		return clients.length + 1;
	}

	public void startBattle(NSZoneRing ring)
	{
		NSZonePlayer players[] = ring.getPlayers();

		if (ring.allClicked())
		{
			// First, tell everyone in zone that these players are off to battle, and fix up their player ids to match battle convention.
			ZAClient clients[] = new ZAClient[players.length];
			int i = 0;
			for (NSZonePlayer player : players)
			{
				player.getStatusFlags().setToBattle(true);
				this.sendToAll(player.createUpdate());
				
				ZAClient client = clients[i++] = new ZAClient();
				client.setName(player.getNickname());
				client.setPlayerId(player.getSlot());
				client.setSubscriberId(player.getSubscriberID());
				client.setAddress(player.getIpString());
				client.set_flags(player.getStatusFlags().isBadDiag() ? 1: 0);
			}

			NSTerritory terr = new NSTerritory();
			terr.setCanon(2);
			terr.setRandSeed(NSUtil.fastRandomInt(250));
			terr.setExists(true);

			int gameID = NSUtil.fastRandomInt(1, 10000);

			// Now send the launchserver command to the BM.
			NSZonePlayer bm = ring.getBattleMaster();
			ZChalLaunchServer zcls = new ZChalLaunchServer();
			zcls.setClients(clients);
			zcls.setGameID(gameID);
			zcls.setBountyTerr(terr);
			this.getClientBySubscriberID(bm.getSubscriberID()).sendCommand(zcls);

			// Finally, send the launchclient command to clients on ring.
			ZChalLaunchClient zclc = new ZChalLaunchClient();
			zclc.setGameID(gameID);
			zclc.setIp(bm.getIpString());
			this.sendToRing(ring, zclc, true);

			// Wait and give the packets time to send.
			NSConnection.waitMillis(1000);

			// Now disconnect the clients.
			for (NSZonePlayer player : players)
			{
				this.getClientBySubscriberID(player.getSubscriberID()).disconnect();
			}

			// Remove the ring and spawn a new one.
			ZChalBattleDel zcbd = new ZChalBattleDel();
			zcbd.setRingNumber(ring.getRingNum());
			this.sendToAll(zcbd);
			ring.clear();
		}
	}

	public void sendPlayerDisconnect(NSChallengeServerClient client)
	{
		ZChalPlayerDel zcpd = new ZChalPlayerDel();
		zcpd.setPlayerIDs(new int[]
		{ client.getPlayer().getPlayerIndex() });
		this.sendToAll(zcpd, client);
	}

	// Sends provided command to all players connected, with the option to ignore someone (usually the sender)
	public void sendToAll(NSCommand command)
	{
		this.sendToAll(command, null);
	}

	public void sendToAll(NSCommand command, NSChallengeServerClient ignore)
	{
		NSChallengeServerClient clients[] = this.getConnectedClients();

		for (NSChallengeServerClient client : clients)
		{
			if (client == ignore)
				continue;

			client.sendCommand(command);
		}
	}

	// Sends provided command to all clients attached to ring.
	public void sendToRing(NSZoneRing ring, NSCommand command)
	{
		this.sendToRing(ring, command, true);
	}

	public void sendToRing(NSZoneRing ring, NSCommand command, boolean sendToBM)
	{
		NSZonePlayer players[] = ring.getPlayers();
		NSChallengeServerClient client;
		NSZonePlayer bm = ring.getBattleMaster();

		for (NSZonePlayer player : players)
		{
			client = this.getClientBySubscriberID(player.getSubscriberID());
			if (client == null)
				continue;
			if ((client.getPlayer() == bm) && !sendToBM)
				continue;

			client.sendCommand(command);
		}
	}

	public int getConnectedClientCount()
	{
		return this.clients.size();
	}

	public NSChallengeServerClient[] getConnectedClients()
	{
		return this.clients.toArray(new NSChallengeServerClient[] {});
	}

	public NSZoneRing[] getRings()
	{
		return this.rings;
	}

	public NSZoneRing getRing(int ringNum)
	{
		return this.rings[ringNum - 1];
	}

	public ZRootReplyQuery createRootReply()
	{
		ZRootReplyQuery zrrq;
		zrrq = new ZRootReplyQuery();

		zrrq.setZone(1);
		zrrq.setServerMajorVersion(10);
		zrrq.setServerMinorVersion(78);
		zrrq.setZones(this.zones);

		return zrrq;
	}

	public ZChalServerInfo createServerInfo()
	{
		ZChalServerInfo zcsi = new ZChalServerInfo();
		zcsi.setActiveMapW(this.activeMapW);
		zcsi.setActiveMapH(this.activeMapH);
		zcsi.setContact(new float[]
		{ this.chalCenter.getX(), this.chalCenter.getY(), this.chalZoneRadius[0], this.chalZoneRadius[1] });
		zcsi.setMod("");
		return zcsi;
	}

	public NSChallengeServerClient getClientBySubscriberID(int subscriberID)
	{
		Iterator<NSChallengeServerClient> i = this.clients.iterator();
		NSZonePlayer player;
		NSChallengeServerClient client;

		while (i.hasNext())
		{
			client = i.next();
			player = client.getPlayer();

			if (player != null)
			{
				if (player.getSubscriberID() == subscriberID)
					return client;
			}
		}

		return null;
	}

	public NSBoardCoord getPlayerStartingPosition(int pIndex)
	{
		return this.playerPosList[pIndex];
	}

	private NSZone[] zones;

	private int activeMapW, activeMapH;

	private NSBoardCoord chalCenter;

	private float chalZoneRadius[];

	public NSBoardCoord battlePosList[];

	private NSBoardCoord playerPosList[];

	private NSZoneRing rings[];

	private Vector<NSChallengeServerClient> clients;

	private int port;

	private Thread thread;

	private ServerSocket server;

	public static final int CHAL_RING_W_IN_TILES = 12;

	public static final int CHAL_RING_H_IN_TILES = 11;

	public static final int CHAL_ZONE_W_IN_TILES = 8;

	public static final int CHAL_ZONE_H_IN_TILES = 8;

	public static final int MAX_TERR_W_IN_TILES = 8;

	public static final int MAX_TERR_H_IN_TILES = 8;

	public static final int CL_NUM_CHAL_BATTLES = 9;

	public static final int CL_NUM_ZONE_RINGS = 2;

	public static final int CL_NUM_CHAL_PLAYERS = 40;

	public static final int CLIENT_X_FUDGE = 13;

	public static final int PLAYER_Y_FUDGE = 2;
}
