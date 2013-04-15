package org.javastorm.territory;

import org.javastorm.BoardCoord;
import org.javastorm.SyncRand;
import org.javastorm.World;
import org.javastorm.Netstorm;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.types.Types.NSTypeFrame;

public class TerrainBuilder
{
	public static final void runTerrainBuilder(World world, BattlePlayer player, ChunkCoordRect cRect, ChunkMap cMap, int onlyIslandId)
	{
		// TODO: don't think we care about not making archipelago.
		boolean makeArch = true;

		TerrainBuilder tb = new TerrainBuilder(world, player, onlyIslandId, cRect.getTl(), cRect.getBr(), cMap);

		if (makeArch)
		{
			int islandsSize = 1;
			tb.setMakingArchipelago(true);
			tb.setLocalDims(tb.getCsx() - (islandsSize - 1), tb.getCsy() - (islandsSize - 1), tb.getCex() + islandsSize, tb.getCey() + islandsSize);

			int archExpansion = 3;
			int aDim = 3 + (archExpansion + (archExpansion - 1));
			int numInitSplotches = 85 * (aDim * aDim) / (5 * 5);
			tb.makeSplotch(numInitSplotches); // 20

			SyncRand random = new SyncRand(tb.getArchSeed() * 443);
			//int totalInArea = (tb.getCex()- tb.getCsx()) * (tb.getCey() - tb.getCsy()) * NSChunkCoord.CHUNK_DIM * NSChunkCoord.CHUNK_DIM;
			int numToMake = 600;

			tb.makeFringe(0, new ChunkCoord(tb.getCsx(), tb.getCsy()), new ChunkCoord(tb.getCex(), tb.getCey()), numToMake, random);

			tb.smooth(1);
			tb.removeSingles();
			tb.makeConsolidatedPieces();
		}
	}

	public TerrainBuilder(World world, BattlePlayer ownerPlayer, int islandId, ChunkCoord tl, ChunkCoord br, ChunkMap chunkMap)
	{
		this.world = world;

		this.sidMaker = new SidMaker();

		this.chunkMap = chunkMap;
		this.typeOf = new int[256];
		this.equivalentIcon = new char[256];

		//this.onlyIslandId = NSIslandList.INVALID_ISLAND_ID;
		this.makingArchipelago = false;

		//this.onlyIslandId = islandId;

		NSType isleType = Types.findByTypeName("isle");
		NSType bigIsleType = Types.findByTypeName("isleBig");

		this.typeOf[CLOUD] = 0;

		this.typeOf[LAND] = isleType.getTypeNum();
		this.equivalentIcon[LAND] = LAND;

		this.typeOf[BIG_LAND] = bigIsleType.getTypeNum();
		this.equivalentIcon[BIG_LAND] = LAND;

		this.typeOf[MARKER] = 0;
		this.equivalentIcon[MARKER] = LAND;

		typeOf[FRINGE] = isleType.getTypeNum();
		equivalentIcon[FRINGE] = LAND;

		this.idMatrix = new char[MAP_XLEN][MAP_YLEN];
		this.isleMatrix = new char[MAP_XLEN][MAP_YLEN];
		for (int x = 0; x < MAP_XLEN; x++)
			for (int y = 0; y < MAP_YLEN; y++)
			{
				this.idMatrix[x][y] = IslandList.INVALID_ISLAND_ID;
				this.isleMatrix[x][y] = CLOUD;
			}

		this.player = ownerPlayer;

		this.setLocalDims(tl.getX(), tl.getY(), br.getX(), br.getY());
	}

	public void setLocalDims(int tlx, int tly, int brx, int bry)
	{
		this.csx = tlx;
		this.csy = tly;
		this.sx = this.csx * ChunkCoord.CHUNK_DIM;
		this.sy = this.csy * ChunkCoord.CHUNK_DIM;
		this.cex = brx;
		this.cey = bry;
		this.ex = this.cex * ChunkCoord.CHUNK_DIM;
		this.ey = this.cey * ChunkCoord.CHUNK_DIM;

		assert(this.cex <= ChunkMap.MAP_CHUNK_XLEN);
		assert(this.cey <= ChunkMap.MAP_CHUNK_YLEN);
		
		if (this.cex > ChunkMap.MAP_CHUNK_XLEN)
		{
			this.cex = ChunkMap.MAP_CHUNK_XLEN;
		}
		if (this.cey > ChunkMap.MAP_CHUNK_YLEN)
		{
			this.cey = ChunkMap.MAP_CHUNK_YLEN;
		}
	}

	public void makeConsolidatedPieces()
	{
		this.touch(LAND, FRINGE);

		this.makeSidsFor(LAND);
		//		addToGraph(sidMaker.lastSid);
		this.restoreFringeAndMarkers();
	}

	private void makeSidsFor(char icon)
	{
		NSType isleType = Types.findByTypeName("isle");
		NSType bigIsleType = Types.findByTypeName("isleBig");

		for (int y = this.sy; y < this.ey; ++y)
		{
			for (int x = this.sx; x < this.ex; ++x)
			{
				if (this.isEquivAt(x, y, icon))
				{
					this.setLandType(isleType, bigIsleType);
					int type = this.typeOf[isleMatrix[x][y]];
					if (type > 0)
						this.createAt(x, y, type, this.getFrameOf(x, y), this.player);
				}
			}
		}
	}

	static int redir[];

	private int getFrameOf(int x, int y)
	{
		char actualIcon = this.isleMatrix[x][y];
		char icon = this.equivalentIcon[actualIcon];
		int sideMask = getIconSideMask(x, y, icon, getIslandId(x, y));
		int cornerMask = getIconCornerMask(x, y, icon, getIslandId(x, y));
		char sideOrientation = BoardCoord.maskToOrientation[sideMask];
		char cornerOrientation = BoardCoord.maskToOrientation[cornerMask];

		NSType ts = Types.findByTypeNum(typeOf[actualIcon]);

		if (redir == null)
		{
			redir = new int[99];

			for (int i = 0; i < 99; ++i)
			{
				redir[i] = (int) Math.random(); //fastRandomInt(1000);
				if (i > 0 && (redir[i] % 4 == redir[i - 1] % 4))
				{
					redir[i]++;
				}
			}
		}
		int frame;
		if (actualIcon == LAND && sideOrientation == 'A' && cornerOrientation == 'A')
		{
			frame = ts.getFirstFrame('J' - 'A') + 1;
			int q = y / 3;
			q += redir[(redir[(y / 3) % 99] + redir[(x / 3) % 99]) % 99];
			frame += (q % 4) * 9; //(y/3)*7*(x/3)+(x/3)
			frame += (y % 3) * 3 + (x % 3);

		}
		else
		{
			frame = ts.getAnyBestFrame(sideOrientation, cornerOrientation, -1, 0);

		}

		if (frame == NSTypeFrame.EMPTY_FRAME)
			frame = 0;

		return frame;
	}

	private void setLandType(NSType normalType, NSType bigType)
	{
		typeOf[LAND] = normalType.getTypeNum();
		typeOf[BIG_LAND] = bigType.getTypeNum();
		typeOf[FRINGE] = normalType.getTypeNum();
	}

	public void restoreFringeAndMarkers()
	{
		for (int y = this.sy; y < this.ey; ++y)
			for (int x = this.sx; x < this.ex; ++x)
				if (this.isleMatrix[x][y] == FRINGE || this.isleMatrix[x][y] == MARKER || this.isleMatrix[x][y] == BIG_LAND)
					this.isleMatrix[x][y] = LAND;
	}

	private void makeSplotch(int reps)
	{

		SyncRand random = new SyncRand();
		random.seed(getArchSeed() * 93);

		while (reps > 0)
		{
			int x = random.get(sx + 2, ex - 2);
			int y = random.get(sy + 2, ey - 2);
			ChunkCoord cc = new ChunkCoord(new BoardCoord(x, y));
			if (!chunkMap.exists(cc) || chunkMap.islandIdAt(cc) == 0)
			{
				putIcon(x, y, LAND, 0);
				--reps;
			}
		}
	}

	private int smoothCorner(int x, int y, int limit)
	{
		if (this.getIcon(x, y) != LAND)
		{
			return 0;
		}

		int count = 0;
		for (int i = 0; i < 8; ++i)
		{
			if (this.getIconDir(x, y, i) == LAND)
			{
				++count;
				if ((i & 1) == 0)
				{
					++count;
				}
			}
		}
		if (count <= limit)
		{
			this.putIcon(x, y, CLOUD, IslandList.INVALID_ISLAND_ID);
			return 1;
		}
		return 0;
	}

	private void smoothSpot(int x, int y)
	{
		int count = 0;
		int currentId = IslandList.INVALID_ISLAND_ID;
		for (int i = 0; i < 8 + 5; ++i)
		{
			int dir = i & 7;
			if (this.getIconDir(x, y, dir) == LAND)
			{
				if (count == 0)
				{
					currentId = this.getIslandIdDir(x, y, dir);
				}
				if (currentId == this.getIslandIdDir(x, y, dir))
				{
					++count;
					if (count >= 5 && ((dir & 1) != 0))
					{
						this.putIcon(x, y, LAND, currentId);
						break;
					}
				}
				else
				{
					count = 0;
				}
			}
			else
			{
				count = 0;
			}
		}
	}

	private void smooth(int reps)
	{
		int count = 0;
		while (count < reps)
		{
			for (int y = sy + 1; y < ey/*-1*/; ++y)
			{
				for (int x = sx + 1; x < ex - 1; ++x)
				{
					if (isleMatrix[x][y] == CLOUD)
					{
						if (isleMatrix[x - 1][y] == LAND || isleMatrix[x + 1][y] == LAND || isleMatrix[x][y - 1] == LAND || isleMatrix[x][y + 1] == LAND)
						{
							this.smoothSpot(x, y);
						}
					}
					//					if( count > 0 ) {
					//						smoothCorner( x, y, 3 );
					//					}
				}
			}
			++count;
		}
	}

	private void removeSingles()
	{
		int found = 1;
		while (found > 0)
		{
			found = 0;
			for (int y = sy + 1; y < ey - 1; ++y)
			{
				for (int x = sx + 1; x < ex - 1; ++x)
				{
					found |= this.smoothCorner(x, y, 4);
				}
			}
		}
	}

	private void touch(int fromIcon, int toIcon)
	{
		for (int y = this.sy; y < this.ey; ++y)
		{
			for (int x = this.sx; x < this.ex; ++x)
			{
				int icon = this.getIcon(x, y);
				if (icon == fromIcon)
				{
					int islandId = this.getIslandId(x, y);
					for (int d = 0; d < BoardCoord.NUM_DIRS; ++d)
						if (!this.isEquivAt(x + dirXOffset[d], y + dirYOffset[d], icon, islandId))
						{
							this.putIcon(x, y, (char) toIcon, islandId);
							break;
						}
				}
			}
		}
	}

	public void makeFringe(int islandId, ChunkCoord tl, ChunkCoord br, int numLeft, SyncRand random)
	{
		int reps = 1000;
		int x;
		int y;

		int xBase = tl.getX() * ChunkCoord.CHUNK_DIM;
		int yBase = tl.getY() * ChunkCoord.CHUNK_DIM;

		int xLen = ((br.getX() - tl.getX()) * ChunkCoord.CHUNK_DIM) - 1;
		int yLen = ((br.getY() - tl.getY()) * ChunkCoord.CHUNK_DIM) - 1;

		while (numLeft > 0 && (reps-- > 0))
		{
			x = xBase + random.get(xLen); // Put these -1's here to keep from extending
			y = yBase + random.get(yLen); // outside of the chunk with +1 below.
			random.get(1);
			if (isleMatrix[x][y] == LAND && (makingArchipelago || getIslandId(x, y) == islandId))
			{
				int startingNumLeft = numLeft;
				for (int py = y - 1; py <= y + 1; ++py)
					for (int px = x - 1; px <= x + 1; ++px)
						if (this.tryPutIcon(px, py, LAND, islandId))
							--numLeft;
				if (numLeft < startingNumLeft)
					reps = 1000;
			}
		}
	}

	private int createAt(int x, int y, int type, int frame, BattlePlayer player)
	{
		return this.sidMaker.createAt(new BoardCoord(x, y), type, frame, player, this.idMatrix[x][y]);
	}

	public boolean tryPutIcon(int x, int y, char icon, int islandId)
	{
		if ((this.isLegal(x, y)) && (this.isleMatrix[x][y] != icon) && (this.makingArchipelago || this.getChunkIslandId(x, y) == islandId))
		{
			this.putIcon(x, y, icon, islandId);
			return true;
		}
		return false;
	}

	private void putIcon(int x, int y, char icon, int islandId)
	{
		this.isleMatrix[x][y] = icon;
		this.idMatrix[x][y] = (char) islandId;
	}

	public boolean isLegal(int x, int y)
	{
		return x >= this.sx && y >= this.sy && x <= this.ex && y <= this.ey;
	}

	public int getArchSeed()
	{
		for (int cy = this.csy; cy < this.cey; ++cy)
		{
			for (int cx = this.csx; cx < this.cex; ++cx)
			{
				Chunk chunk = this.chunkMap.getChunk(new ChunkCoord(cx, cy));
				if (chunk.getIslandId() != IslandList.INVALID_ISLAND_ID)
				{
					return chunk.getTerr().getRandSeed();
				}
			}
		}

		return 0x2333;
	}

	int getChunkIslandId(int x, int y)
	{
		return chunkMap.islandIdAt(new ChunkCoord(new BoardCoord(x, y)));
	}

	public void setMakingArchipelago(boolean set)
	{
		this.makingArchipelago = set;
	}

	public int getCex()
	{
		return cex;
	}

	public int getCey()
	{
		return cey;
	}

	public int getCsx()
	{
		return csx;
	}

	public int getCsy()
	{
		return csy;
	}

	public boolean isEquivAt(int x, int y, char icon)
	{
		if (!this.isLegal(x, y))
			return false;
		return (this.equivalentIcon[isleMatrix[x][y]] == icon);
	}

	public boolean isEquivAt(int x, int y, int icon, int islandId)
	{
		if (!this.isLegal(x, y))
			return false;
		return (this.equivalentIcon[isleMatrix[x][y]] == icon && this.idMatrix[x][y] == islandId);
	}

	char getIcon(int x, int y)
	{
		return this.isleMatrix[x][y];
	}

	char getIslandId(int x, int y)
	{
		return this.idMatrix[x][y];
	}

	char getIconDir(int x, int y, int dir)
	{
		return this.isleMatrix[x + dirXOffset[dir]][y + dirYOffset[dir]];
	}

	char getIslandIdDir(int x, int y, int dir)
	{
		return this.idMatrix[x + dirXOffset[dir]][y + dirYOffset[dir]];
	}

	int getIconMask(int x, int y, int icon, char islandId, int dir)
	{
		return (isEquivAt(x + dirXOffset[dir], y + dirYOffset[dir], icon, islandId) ? BoardCoord.dirMasks[dir] : 0);
	}

	int getIconSideMask(int x, int y, int icon, char islandId)
	{
		return this.getIconMask(x, y, icon, islandId, BoardCoord.NORTH) | this.getIconMask(x, y, icon, islandId, BoardCoord.EAST) | this.getIconMask(x, y, icon, islandId, BoardCoord.SOUTH) | this.getIconMask(x, y, icon, islandId, BoardCoord.WEST);
	}

	int getIconCornerMask(int x, int y, int icon, char islandId)
	{
		return getIconMask(x, y, icon, islandId, BoardCoord.NORTH_WEST) | getIconMask(x, y, icon, islandId, BoardCoord.NORTH_EAST) | getIconMask(x, y, icon, islandId, BoardCoord.SOUTH_EAST) | getIconMask(x, y, icon, islandId, BoardCoord.SOUTH_WEST);
	}

	private class SidMaker
	{
		private SidMaker()
		{
			this.makePredictable = World.alPREDICTABLE;
			//this.lastSid = 0;
			this.excludeLitFlags = 0;
		}

		private int createAt(BoardCoord bc, int type, int frame, BattlePlayer player, int islandId)
		{
			MainSquid newPiece = (MainSquid) world.createSquid(type, this.makePredictable);
			//lastSid = newPiece.getSid();
			newPiece.setPlayer(null); //playerId );
			newPiece.setFrame(frame);
			NSType ts = newPiece.getTypeStruct();

			newPiece.setPos(bc);
			newPiece.pop(pfSIMO_CREATION);

			if ((ts.getGenus() & NSType.gISLAND) > 0)
			{
				if (islandId == IslandList.INVALID_ISLAND_ID)
				{
					newPiece.setIslandId(chunkMap.islandIdAt(new ChunkCoord(bc)));
				}
				else
				{
					newPiece.setIslandId(islandId);
				}

				NSTypeFrame f = ts.getFrameInfo(frame);

				if ((f.getFlags() & NSTypeFrame.FF_FRINGE) > 0)
				{
					NSType fts = Types.findByTypeName("fringe");
					int fringeFrame = NSTypeFrame.EMPTY_FRAME;
					do
					{
						fringeFrame = fts.getAnyBestFrame(f.getSideOrientation(), f.getCornerOrientation());
					}
					while ((fts.getFrameInfo(fringeFrame).getFlags() & excludeLitFlags) > 0);

					if (fringeFrame != NSTypeFrame.EMPTY_FRAME)
					{
						MainSquid fringe = (MainSquid) world.createSquid(fts.getTypeNum(), makePredictable);
						fringe.setFrame(fringeFrame);
						bc.moveBy(0, 4);
						fringe.setPos(bc);
						fringe.pop(pfSIMO_CREATION);
					}
				}
			}

			return newPiece.getSid();
		}

		private int makePredictable;

		//private int lastSid;
		private int excludeLitFlags;

		// use FF_LIT|FF_UNLIT to exclude fringe with any windows at all. Use FF_UNLIT
		// to insure that all fringe frames start out lit.

		private static final int pfSIMO_CREATION = (BaseSquid.pfCREATED | BaseSquid.pfPREDICTABLE_POP | BaseSquid.pfDONT_TRANSMIT | BaseSquid.pfDONT_RECALC_GRAPH_CONNECTIONS);
	};

	private World world;

	private SidMaker sidMaker;

	private char isleMatrix[][];

	private char idMatrix[][];

	private boolean makingArchipelago;

	private char equivalentIcon[];

	private int typeOf[];

	private BattlePlayer player;

	//private int onlyIslandId;
	private ChunkMap chunkMap;

	int sx;

	int sy;

	int ex;

	int ey;

	int csx;

	int csy;

	int cex;

	int cey;

	private static final char CLOUD = ('.');

	private static final char LAND = ('x');

	private static final char BIG_LAND = ('X');

	private static final char FRINGE = ('=');

	private static final char MARKER = ('o');

	public static final int MAP_XLEN = Netstorm.BOARDDIM_IN_TILES;

	public static final int MAP_YLEN = Netstorm.BOARDDIM_IN_TILES;

	public static final int dirXOffset[] =
	{ 0, 1, 1, 1, 0, -1, -1, -1 };

	public static final int dirYOffset[] =
	{ -1, -1, 0, 1, 1, 1, 0, -1 };
}
