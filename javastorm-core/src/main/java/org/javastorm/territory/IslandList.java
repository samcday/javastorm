package org.javastorm.territory;

import org.javastorm.BoardCoord;
import org.javastorm.BoardCoordRect;
import org.javastorm.Flooder;
import org.javastorm.World;
import org.javastorm.Netstorm;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.squids.MainSquid;

public class IslandList
{
	public IslandList(World world)
	{
		this.world = world;
		this.totalIslandIds = 0;
		this.islands = new NSOneIsland[IslandList.MAX_ISLANDS_PER_MAP];

		for (int i = 0; i < IslandList.MAX_ISLANDS_PER_MAP; i++)
			this.islands[i] = new NSOneIsland();
	}

	public void reset()
	{
		for (int i = 0; i < MAX_ISLANDS_PER_MAP; ++i)
		{
			this.islands[i].reset();
		}
		totalIslandIds = 0;
	}

	public boolean isValid(int islandId)
	{
		return 0 <= islandId && islandId < this.totalIslandIds;
	}

	public boolean isLegal(int islandId)
	{
		if (islandId == INVALID_ISLAND_ID)
		{
			return true;
		}
		return this.isValid(islandId);
	}

	public boolean exists(int islandId)
	{
		if (this.isValid(islandId))
		{
			return this.islands[islandId].exists;
		}

		return false;
	}

	public int getTotal()
	{
		return this.totalIslandIds;
	}

	public int addIsland(BattlePlayer player, int terrId)
	{
		int islandId = this.getTotal();
		this.totalIslandIds++;

		NSOneIsland island = this.islands[islandId];
		island.exists = true;
		island.currentPlayer = player;
		island.originalPlayer = player;
		island.originalTerrId = terrId;

		return islandId;
	}

	public void scanAdd()
	{
		for (int y = 0; y < Netstorm.BOARDDIM_IN_TILES; ++y)
		{
			for (int x = 0; x < Netstorm.BOARDDIM_IN_TILES; ++x)
			{
				MainSquid s = this.world.getSquidHash().getGridSid(new BoardCoord(x, y));
				if (s != null)
				{
					if (s.getIslandId() == -1)
					{
						Flooder f = new Flooder(this.world);
						int islandId = addIsland(null, -1);
						f.startFloodIslandId(s.getPos(), islandId);
						//setBoardCoordRect( islandId, f.bcr );
						//++totalAdded;
					}
				}
			}
		}
	}

	public BoardCoordRect getBoardCoordRect(int islandId)
	{
		if (this.exists(islandId))
		{
			return this.islands[islandId].bcr;
		}

		return null;
	}

	public void setBoardCoordRect(int islandId, BoardCoordRect bcr)
	{
		if (this.exists(islandId))
		{
			this.islands[islandId].bcr = bcr;
		}
	}

	public boolean fullyOwned(int islandID, BattlePlayer player, int terrID)
	{
		if (!this.exists(islandID))
			return false;

		NSOneIsland isle = this.islands[islandID];
		return isle.originalPlayer == player && isle.originalTerrId == terrID;
	}

	public BattlePlayer getOwner(int islandId)
	{
		if (!this.exists(islandId))
		{
			return null;
		}

		return this.islands[islandId].currentPlayer;
	}

	public int getVortexSid(int islandId)
	{
		if (!this.exists(islandId))
		{
			return 0;
		}
		return this.islands[islandId].vortexSid;
	}

	private NSOneIsland islands[];

	private int totalIslandIds;

	private World world;

	private class NSOneIsland
	{
		public NSOneIsland()
		{
			this.bcr = new BoardCoordRect();
			this.reset();
		}

		public void reset()
		{
			this.vortexSid = 0;
			this.currentPlayer = this.originalPlayer = null;
			this.exists = false;
			this.originalTerrId = -1;
			this.bcr.makeInvalid();
		}

		private int vortexSid;

		private boolean exists;

		private BattlePlayer currentPlayer;

		private int originalTerrId; // This is always the proper index into the player's terr[] array.

		private BattlePlayer originalPlayer; // This is always the proper index into the player's terr[] array.

		private BoardCoordRect bcr;
	}

	public static final int MAX_ISLANDS_PER_MAP = 100;

	public static final int INVALID_ISLAND_ID = 127;
}
