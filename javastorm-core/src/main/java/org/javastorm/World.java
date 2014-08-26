package org.javastorm;

import java.util.Vector;

import org.javastorm.battle.BattleOptions;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZCompressedUpdateSquid;
import org.javastorm.process.BaseProcess;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SpotFlags;
import org.javastorm.squids.SquidXYHash;
import org.javastorm.territory.ChunkMap;
import org.javastorm.territory.IslandList;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.util.NSTrace;

// Represents the Netstorm world, containing squids, etc.
public class World
{
	public World()
	{
		this(new WorldServerStub());
	}

	public World(WorldServer server)
	{
		this.server = server;

		this.players = new BattlePlayer[8];
		this.squids = new BaseSquid[120000];
		this.battleStart = System.currentTimeMillis();

		this.islandList = new IslandList(this);
		this.chunkMap = new ChunkMap(this, ChunkMap.MAP_CHUNK_XLEN, ChunkMap.MAP_CHUNK_YLEN);
		this.squidHash = new SquidXYHash();
		this.spotFlags = new SpotFlags();

		this.processes = new BaseProcess[40000];

		this.bridgeCount = 0;

		this.ended = false;
		
		this.worldVersion = new Version(10, 78);
		this.firstServerSid = FIRST_SERVER_SID_78;
		this.firstPredictableSid = FIRST_PREDICTABLE_SID_78;
	}

	public void use79SquidCounters()
	{
		this.worldVersion = new Version(10, 79);
		this.firstServerSid = FIRST_SERVER_SID_79;
		this.firstPredictableSid = FIRST_PREDICTABLE_SID_79;
		 
		this.serverSidCounter = FIRST_SERVER_SID_79;
		this.clientSidCounter = FIRST_CLIENT_SID;
		this.predictableCounter = FIRST_PREDICTABLE_SID_79;
	}

	public int getFirstServerSid()
	{
		return this.firstServerSid;
	}
	
	public int getFirstPredictable()
	{
		return this.firstPredictableSid;
	}

	// ----------------	
	// PROCESS CODE HERE.
	// ----------------
	public int attachProcess(BaseProcess proc)
	{
		int slot = 0;

		try
		{
			for (int i = this.processes.length;; i--)
			{
				if (this.processes[i] == proc)
				{
					return i;
				}

				if (slot == 0 && this.processes[i] == null)
				{
					slot = i;
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
		}

		this.processes[slot] = proc;
		return slot;
	}

	public BaseProcess getProcess(int pId)
	{
		return this.processes[pId];
	}

	public void removeProcess(int pId)
	{
		assert (this.processes[pId] != null);
		this.processes[pId] = null;
	}

	public void doProcesses()
	{
		for (int i = 0; i < this.processes.length; i++)
		{
			if (this.processes[i] != null)
			{
				this.processes[i].process();
			}
		}
	}

	// ----------------
	// SQUID CODE HERE.
	// ----------------
	public BaseSquid take(int type, int sid)
	{
		if (this.squids[sid] != null)
		{
			this.squids[sid].push();
		}

		NSType ts = Types.findByTypeNum(type);
		BaseSquid squid;

		if (ts.getConstructor() != null)
		{
			squid = (BaseSquid) ts.getConstructor().construct(this, sid);
		}
		else
		{
			squid = (BaseSquid) new MainSquid(this, sid);
		}

		this.squids[sid] = squid;

		squid.setFree(false);
		squid.setDead(false);
		squid.setType(type);
		squid.notifyTake();

		return squid;
	}

	public int getNumSquids()
	{
		return this.squids.length;
	}

	public BaseSquid createSquid(int type, int allocFlags)
	{
		int sid = this.allocateSquid(allocFlags);
		
		if((allocFlags & alPREDICTABLE) > 0)
			assert(sid >= this.firstPredictableSid);
		else
			assert(sid < this.firstPredictableSid);

		NSType ts = Types.findByTypeNum(type);
		BaseSquid squid;

		if (ts.getConstructor() != null)
		{
			squid = (BaseSquid) ts.getConstructor().construct(this, sid);
		}
		else
		{
			squid = (BaseSquid) new MainSquid(this, sid);
		}

		squid.setType(type);
		squid.notifyCreate();

		this.squids[sid] = squid;

		return squid;
	}

	private int allocateSquid(int allocFlags)
	{
		if (allocFlags == World.alPREDICTABLE)
		{
			this.predictableCount++;
			return this.predictableCounter++;
		}
		else if (allocFlags == World.alCLIENT)
		{
			return this.clientSidCounter++;
		}
		else
		{
			return this.serverSidCounter++;
		}
	}

	public BaseSquid getSquid(int sID)
	{
		return this.squids[sID];
	}
	
	public int getServerSidCounter()
	{
		return this.serverSidCounter;
	}
	
	public int getClientSidCounter()
	{
		return this.clientSidCounter;
	}

	public void freeSquid(int sid)
	{
		this.squids[sid] = null;
	}

	public int getPredictableCount()
	{
		return this.predictableCount;
	}

	public void setBattleOptions(BattleOptions bo)
	{
		this.battleOptions = bo;
	}

	public BattleOptions getBattleOptions()
	{
		return battleOptions;
	}

	public BattlePlayer getPlayerBySubscriberID(int subscriberID)
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
				if (this.players[i].getSubscriberID() == subscriberID)
					return this.players[i];
		}

		return null;
	}

	public BattlePlayer getPlayerByNickname(String nickname)
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
				if (this.players[i].getNickname().equalsIgnoreCase(nickname))
					return this.players[i];
		}

		return null;
	}

	public void addPlayer(BattlePlayer player)
	{
		System.out.println("NSWorld: Added " + player.getNickname() + " to list. pIndex=" + player.getPlayerIndex());
		this.players[this.playerCount++] = player;
	}

	public void removePlayer(BattlePlayer player)
	{
		System.out.println("NSWorld: Removing " + player.getNickname() + " from list. pIndex=" + player.getPlayerIndex());

		for (int i = 0; i < this.playerCount; i++)
		{
			if (this.players[i] != player)
				continue;

			this.players[i] = null;

			// If this player wasn't at end of list, chuck someone else in his spot so we don't have any null indices.
			// This basically just makes iteration easier.
			if (i < this.playerCount - 1)
			{
				// Chuck the player on end of list in this empty space.
				this.players[i] = this.players[this.playerCount - 1];
				this.players[this.playerCount - 1] = null;
			}
			this.playerCount--;
		}
	}

	public BattlePlayer getPlayer(int index)
	{
		for (int i = 0; i < this.playerCount; i++)
		{
			if (this.players[i] == null)
				continue;

			if (this.players[i].getPlayerIndex() == index)
				return this.players[i];
		}

		return null;
	}

	public BattlePlayer[] getAllPlayers()
	{
		BattlePlayer[] playerList = new BattlePlayer[this.playerCount];
		int index = 0;
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
				playerList[index++] = this.players[i];
		}

		return playerList;
	}

	// Player at given index has declared draw.
	public void playerDraw(int index)
	{
		if (this.players[index - 1] != null)
			this.players[index - 1].setDrawed(true);
	}

	// Draw failed, clear flags.
	public void clearDraw()
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
				this.players[i].setDrawed(false);
		}
	}

	// Has every drawed?
	public boolean drawSuccess()
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
				if (!this.players[i].isWatcher())
					if (!this.players[i].isDrawed())
						return false;
		}

		return true;
	}

	public void incrementBridgeCount()
	{
		this.bridgeCount++;
	}

	public int getBridgeCount()
	{
		return this.bridgeCount;
	}

	public void battleEnded()
	{
		this.ended = true;
	}

	public boolean hasEnded()
	{
		return this.ended;
	}

	private int getAdjustedSalvageValue(MainSquid deadSid)
	{
		if (!deadSid.getTypeStruct().getTypeName().equals("nugget"))
		{
			if (deadSid.getHitPoints() < deadSid.getMaxHitPoints() / 2)
				return 0;

			return (int) ((deadSid.getTypeStruct().getCost() * .25) * ((float) deadSid.getHitPoints() / (float) deadSid.getMaxHitPoints()));
		}

		return 0;
	}

	public void reward(MainSquid deadSid, BattlePlayer whoGetsBucks, boolean isSalvage)
	{
		int myValue = deadSid.getTypeStruct().getCost();
		int amountAwarded = 0;

		if (isSalvage)
		{
			amountAwarded = getAdjustedSalvageValue(deadSid);
		}
		else
		{
			// TODO. Use battle options.
			amountAwarded = (int) (myValue * .50);
		}

		if (whoGetsBucks != null)
			whoGetsBucks.incrementCurrentMoney(amountAwarded);
	}

	public ChunkMap getChunkMap()
	{
		return this.chunkMap;
	}

	public IslandList getIslandList()
	{
		return this.islandList;
	}

	// How long has this battle been raging for?
	public int getDuration()
	{
		return (int) (System.currentTimeMillis() - this.battleStart);
	}

	public SquidXYHash getSquidHash()
	{
		return this.squidHash;
	}

	public SpotFlags spotFlags()
	{
		return this.spotFlags;
	}

	// ICKY HACK CODE! Need to figure out a nice, clean way todo this.
	private NetstormGame parent;

	public void setParent(NetstormGame parent)
	{
		this.parent = parent;
	}

	public void dirtyMe(MainSquid squid)
	{
		if (this.parent != null)
			this.parent.dirtyMe(squid);
	}

	public void resetStaticSidLists()
	{
		for (int i = 0; i < staticSidListArray.size(); ++i)
		{
			staticSidListArray.get(i).reset();
		}
	}

	public void registerSidList(StaticSidList list)
	{
		staticSidListArray.add(list);
	}

	public void unregisterSidList(StaticSidList list)
	{
		staticSidListArray.remove(list);
	}

	public void sendTo(int playerId, NSCommand cmd)
	{
		this.server.sendTo(playerId, cmd);
	}
	
	public void sendToAll(NSCommand cmd)
	{
		this.server.sendToAll(cmd);
	}

	public void transmitIfNecessary(MainSquid s)
	{
		if (this.server == null)
			return;

		ZCompressedUpdateSquid zcus = new ZCompressedUpdateSquid();
		zcus.setSid(s.getSid());
		zcus.setPos(s.getPos());
		zcus.setQ(s.getQ());
		zcus.setTypeNum(s.getType());
		zcus.setBaseFlags(s.getBaseFlags());

		if (s.getPlayer() != null)
			zcus.setPlayerId(s.getPlayer().getPlayerIndex());

		zcus.setHitPoints(s.getHitPoints());
		zcus.setZOrder(s.getZOrder());
		zcus.setFrame(s.getFrame());
		zcus.setMainSquidFlags(s.getMainSquidFlags());

		this.server.updateSquid(zcus);
	}

	private Version worldVersion;
	
	private WorldServer server;

	public boolean suppressExtraDumpInfo = false;

	public NSTrace Trace = new NSTrace();

	private Vector<StaticSidList> staticSidListArray = new Vector<StaticSidList>();

	public StaticSidList dudeList = new StaticSidList(this, DUDES_ALLOWED);

	private IslandList islandList;

	private BaseSquid[] squids;

	private BaseProcess[] processes;

	private int serverSidCounter = World.FIRST_SERVER_SID_78;
	private int clientSidCounter = World.FIRST_CLIENT_SID;
	private int predictableCounter = World.FIRST_PREDICTABLE_SID_78;

	private int firstServerSid;
	private int firstPredictableSid;
	
	private int predictableCount = 0;

	private SpotFlags spotFlags;

	private SquidXYHash squidHash;

	private ChunkMap chunkMap;

	private BattlePlayer[] players;

	private BattleOptions battleOptions;

	private boolean ended; // Has this battle ended and is waiting to be cleaned up?

	private int playerCount;

	private long battleStart;

	private int bridgeCount; // Overall count of how many bridges built in this battle.

	public static final String SID = "~y%04d~n";

	public static final int DUDES_ALLOWED = 30;

	public static final int FIRST_REAL_PLAYER = (1);

	public static final int FIRST_CLIENT_SID = (05);

	public static final int FIRST_SERVER_SID_78 = (15000);

	public static final int FIRST_PREDICTABLE_SID_78 = (FIRST_SERVER_SID_78 + (14001 - 6000));

	public static final int FIRST_SERVER_SID_79 = (40000);

	public static final int FIRST_PREDICTABLE_SID_79 = (FIRST_SERVER_SID_79 + 40000);

	public static final int alDEFAULT = (0);
	public static final int alPREDICTABLE = (1);
	public static final int alCLIENT = (2);

	private static final long serialVersionUID = 1L;
}
