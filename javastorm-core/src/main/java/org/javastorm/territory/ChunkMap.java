package org.javastorm.territory;

import org.javastorm.BoardCoord;
import org.javastorm.BoardCoordRect;
import org.javastorm.World;
import org.javastorm.Netstorm;

public class ChunkMap
{
	public ChunkMap(World world, int xLen, int yLen)
	{
		this.world = world;
		this.xLen = xLen;
		this.yLen = yLen;

		this.chunk = new Chunk[MAP_CHUNK_XLEN][MAP_CHUNK_YLEN];
		for (int x = 0; x < MAP_CHUNK_XLEN; x++)
			for (int y = 0; y < MAP_CHUNK_YLEN; y++)
				this.chunk[x][y] = new Chunk();

		this.setValid();
		this.clear();
	}

	public void clear()
	{
		boolean oldValid = this.validFlag;
		this.validFlag = true;
		for (ChunkFinder f = new ChunkFinder(this); f.isValid(); f.advance())
		{
			f.getChunk().clear();
			f.getChunk().setIslandId(IslandList.INVALID_ISLAND_ID);
		}
		this.validFlag = oldValid;
	}

	public Chunk getChunk(ChunkCoord cc)
	{
		assert(this.isValid());
		assert(cc.getX() >= 0 && cc.getY() >= 0 && cc.getX() < this.xLen && cc.getY() < this.yLen);

		cc.setX(Math.min(Math.max(cc.getX(), 0), this.xLen - 1));
		cc.setY(Math.min(Math.max(cc.getY(), 0), this.yLen - 1));

		return this.chunk[cc.getX()][cc.getY()];
	}

	public ChunkCoordRect getChunkCoordRect(int islandId)
	{
		assert(world.getIslandList().exists(islandId));

		int cx = 0;
		int cy = 0;
		ChunkCoordRect cRect = new ChunkCoordRect(MAP_CHUNK_XLEN, MAP_CHUNK_YLEN, 0, 0);

		for (cy = 0; cy < this.yLen; cy++)
			for (cx = 0; cx < this.xLen; cx++)
			{
				if (this.getChunk(new ChunkCoord(cx, cy)).getIslandId() == islandId)
				{
					cRect.getTl().setX(Math.min(cRect.getTl().getX(), cx));
					cRect.getTl().setY(Math.min(cRect.getTl().getY(), cy));
					cRect.getBr().setX(Math.min(cRect.getBr().getX(), cx));
					cRect.getBr().setY(Math.min(cRect.getBr().getY(), cy));
				}
			}

		cRect.getBr().move(1, 1);

		return cRect;
	}

	public boolean anyIslandIntersects(ChunkCoordRect cRect, int excludeIslandId)
	{
		int cx, cy;

		for (cy = cRect.getTl().getY(); cy < cRect.getBr().getY(); cy++)
			for (cx = cRect.getTl().getX(); cx < cRect.getBr().getX(); cx++)
			{
				int islandId = getChunk(new ChunkCoord(cx, cy)).getIslandId();
				if (islandId != IslandList.INVALID_ISLAND_ID && islandId != excludeIslandId)
					return true;
			}

		return false;
	}

	public int islandIdAt(ChunkCoord cc)
	{
		return this.getChunk(cc).getIslandId();
	}

	public boolean exists(ChunkCoord cc)
	{
		return this.getChunk(cc).getIslandId() != IslandList.INVALID_ISLAND_ID;
	}

	public boolean isLegal(ChunkCoord cc)
	{
		return (cc.getX() >= 0) && (cc.getY() >= 0) && (cc.getX() < this.xLen) && (cc.getY() < this.yLen);
	}

	public BoardCoordRect getChunkRect(ChunkCoord cc)
	{
		BoardCoord tl = cc.getBoardCoord();
		BoardCoord br = new BoardCoord(tl);
		br.moveBy(ChunkCoord.CHUNK_DIM - 1, ChunkCoord.CHUNK_DIM - 1);
		return new BoardCoordRect(tl, br);
	}

	public boolean isValid()
	{
		return this.validFlag;
	}

	public void setValid()
	{
		this.validFlag = true;
	}

	public void setInvalid()
	{
		this.validFlag = false;
	}

	public int getXLen()
	{
		return this.xLen;
	}

	public int getYLen()
	{
		return this.yLen;
	}

	public static final ChunkCoordRect getCenterDomain(Territory terr)
	{
		return ChunkMap.getStartingTerrRect(terr);
	}

	public static final ChunkCoordRect getStartingTerrRect(Territory terr)
	{
		ChunkCoord dim = terr.getDim();
		ChunkCoordRect cRect = new ChunkCoordRect(((MAP_CHUNK_XLEN - 3) / 2), ((MAP_CHUNK_XLEN - 3) / 2), ((MAP_CHUNK_XLEN - 3) / 2) + dim.getX(), ((MAP_CHUNK_XLEN - 3) / 2) + dim.getY());

		return cRect;
	}

	private World world;
	private boolean validFlag;
	private int xLen, yLen;
	private Chunk chunk[][];

	public static final int MAP_CHUNK_XLEN = Netstorm.BOARDDIM_IN_TILES / ChunkCoord.CHUNK_DIM;

	public static final int MAP_CHUNK_YLEN = Netstorm.BOARDDIM_IN_TILES / ChunkCoord.CHUNK_DIM;
}
