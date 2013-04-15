package org.javastorm.territory;

import org.javastorm.BoardCoordRect;
import org.javastorm.World;
import org.javastorm.battle.BattlePlayer;

public class ChunkCoordRect
{
	public ChunkCoordRect()
	{
		this.tl = new ChunkCoord();
		this.br = new ChunkCoord();
	}

	public ChunkCoordRect(int sx, int sy, int ex, int ey)
	{
		this.tl = new ChunkCoord(sx, sy);
		this.br = new ChunkCoord(ex, ey);
	}

	public boolean isValid()
	{
		return this.tl.isValid() && this.br.isValid();
	}

	public void move(int bx, int by)
	{
		this.tl.move(bx, by);
		this.br.move(bx, by);
	}

	public BoardCoordRect getBoardCoordRect()
	{
		return new BoardCoordRect(this.tl.getBoardCoord(), this.br.getBoardCoord());
	}

	public ChunkCoord getBr()
	{
		return br;
	}

	public ChunkCoord getTl()
	{
		return tl;
	}

	public static final int shuffleDir[] =
	{ 4, 0, 6, 2, 7, 3, 5, 1 };

	public static final ChunkCoordRect getDomain(World world, BattlePlayer player, Territory terr, ChunkMap cm)
	{
		assert(player != null);

		int mapSize = world.getBattleOptions().getSimpleBattleOptions().getMapSize();

		int dir = shuffleDir[player.getPlayerIndex() - 1];
		int bx = 9;
		int by = 9;
		int aDir = dir & ~1;

		bx += TerrainBuilder.dirXOffset[aDir] * mapSize;
		by += TerrainBuilder.dirYOffset[aDir] * mapSize;
		bx += TerrainBuilder.dirYOffset[aDir] * 0;
		by -= TerrainBuilder.dirXOffset[aDir] * 0;

		if ((dir & 1) > 0)
		{
			bx -= TerrainBuilder.dirYOffset[aDir] * 4;
			by += TerrainBuilder.dirXOffset[aDir] * 4;
		}

		ChunkCoordRect me = new ChunkCoordRect();
		me.tl.setX(bx - 2);
		me.tl.setY(by - 2);
		me.br.setX(bx);
		me.br.setY(by);

		return me;
	}

	private ChunkCoord tl;

	private ChunkCoord br;
}
