package org.javastorm.territory;

import org.javastorm.BoardCoord;
import org.javastorm.CanonDecoder;
import org.javastorm.World;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.types.Types;

// This class is responsible for generating sane islands in the battle map.
// It has two entry points: initAsBattleMode() and initAsFortMode().
// initAsBattleMode has two options: createBountyIsland which creates the multiplayer map.
// and createBattleIsland which creates a players home island.
// initAsFortMode is not implemented.
public class IslandBuilder
{
	public IslandBuilder()
	{
		this.cMap = null;
		this.cRect = new ChunkCoordRect(-1, -1, -1, -1);
		this.player = null;
	}

	public IslandBuilder(ChunkMap cMap, ChunkCoordRect cRect, BattlePlayer player)
	{
		this.cMap = cMap;
		this.cRect = cRect;
		this.player = player;
	}

	public void initAsBattleMode(World world, BattlePlayer player)
	{
		this.world = world;
		this.cMap = this.world.getChunkMap();
		this.player = player;
	}

	public boolean inWorld()
	{
		return this.cMap == this.world.getChunkMap();
	}

	public void createBountyIsland(Territory terr)
	{
		assert(terr.getXChunkCoord() == 0 && terr.getYChunkCoord() == 0);
		assert(this.world.getIslandList().getTotal() == 0);
		assert(this.player == null);

		this.cRect = ChunkMap.getCenterDomain(terr);
		int islandId = this.terrToChunkMap(terr, IslandList.INVALID_ISLAND_ID, true);
		this.chunkMapToSquids(islandId);

		this.world.getIslandList().scanAdd();
	}

	public void createTestIsland(Territory terr)
	{
		this.cRect = ChunkMap.getCenterDomain(terr);
		int islandId = this.terrToChunkMap(terr, IslandList.INVALID_ISLAND_ID, true);
		this.chunkMapToSquids(islandId);

		this.world.getIslandList().scanAdd();
	}
	
	public void createBattleIsland()
	{
		assert(this.player != null);

		// First step is to go through the territories in players fort data and find one fit for battle.
		int terrToKeep = 0;
		Territory terr;
		TerritoryList terrList = this.player.getFortData().interpretTerritory();

		while (terrToKeep < Territory.MR_TERR_COUNT && !(terrList.getTerritory(terrToKeep).isTakenToBattle()))
		{
			terrToKeep++;
		}

		if (terrToKeep >= Territory.MR_TERR_COUNT)
			terrToKeep = 0;

		terr = terrList.getTerritory(terrToKeep);

		this.cRect = ChunkCoordRect.getDomain(this.world, this.player, terrList.getTerritory(terrToKeep), this.cMap);
		int islandId = this.terrToChunkMap(terr, terrToKeep, true);

		// TODO: 
		// getPlayer( playerId )->homeIslandId = islandId;

		this.chunkMapToSquids(islandId);

		// TODO:
		this.player.getFortData().interpretSquids(this.world, terrToKeep, terrToKeep + 1);
		// linearGraphFill();
	}

	public int terrToChunkMap(Territory terr, int terrId, boolean relativeTo0x0)
	{
		int xOffset = relativeTo0x0 ? 0 : terr.getXChunkCoord();
		int yOffset = relativeTo0x0 ? 0 : terr.getYChunkCoord();
		int thisIslandId = IslandList.INVALID_ISLAND_ID;

		if (this.inWorld())
		{
			thisIslandId = this.world.getIslandList().addIsland(player, terrId);
		}
		else
		{
			thisIslandId = terrId;
		}

		CanonDecoder c = new CanonDecoder(Types.findByTypeName("puzzlePiece"), terr.getCanon(), terr.getRot(), new BoardCoord(this.cRect.getTl().getX() + xOffset, this.cRect.getTl().getY() + yOffset));

		int indexWithinTerr = 0;

		while (c.isValid())
		{
			ChunkCoord cc = new ChunkCoord((int) c.getPos().getX(), (int) c.getPos().getY());
			Chunk ch = this.cMap.getChunk(cc);
			ch.setIslandId(thisIslandId);
			ch.setTerr(terr);
			ch.setIndexWithinTerr(indexWithinTerr);
			ch.setSideOrientation(c.getOrientation());

			c.getNext();
			++indexWithinTerr;
		}

		if (this.inWorld())
		{
			this.world.getIslandList().setBoardCoordRect(thisIslandId, this.cMap.getChunkCoordRect(thisIslandId).getBoardCoordRect());
		}

		return thisIslandId;
	}

	private void chunkMapToSquids(int onlyIslandId)
	{
		assert(this.inWorld());
		assert(this.cRect.isValid());
		assert(this.cMap != null);

		TerrainBuilder.runTerrainBuilder(this.world, this.player, this.cRect, this.cMap, onlyIslandId);

		// TODO:
		IslandList islandList = this.world.getIslandList();
		for(int islandId = 0; islandId < islandList.getTotal(); islandId++) {
			if( onlyIslandId == IslandList.INVALID_ISLAND_ID || onlyIslandId == islandId ) {
				//NSTerritory terr = this.cMap.getTerr(islandId);
				//NSBoardCoordRect bcRect = this.cMap.getChunkCoordRect(islandId).getBoardCoordRect();
				//addRandomEdgeFarms( bcRect, islandId, terr.getChunkCount(), playerId );
			}
		}
	}

	private World world;
	private ChunkMap cMap;
	private ChunkCoordRect cRect;
	private BattlePlayer player;
}
