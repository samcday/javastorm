package org.javastorm;

import org.javastorm.battle.BattlePlayer;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidXYHash;
import org.javastorm.types.Types.NSTypeFrame;

public class Flooder
{
	public Flooder(World world)
	{
		this.world = world;
		this.hash = this.world.getSquidHash();
	}

	public void startFloodIslandId(BoardCoord pos, int newIslandId)
	{
		this.floodPlayerId = false;
		this.newIslandId = newIslandId;
		this.floodId(new BoardCoord(pos));
	}

	public void startFloodPlayerId(BoardCoord pos, BattlePlayer newPlayer)
	{
		this.floodPlayerId = true;
		this.newPlayer = newPlayer;
		this.floodId(new BoardCoord(pos));
	}

	private void floodId(BoardCoord pos)
	{
		MainSquid s = this.hash.getGridSid(pos);

		if (s != null)
		{
			boolean flood = false;

			if (this.floodPlayerId && s.getPlayer() != this.newPlayer)
				flood = true;
			else if (s.getIslandId() != this.newIslandId)
				flood = true;

			if (flood)
			{
				if (this.floodPlayerId)
				{
					s.setPlayer(this.newPlayer);
				}
				else
				{
					s.setIslandId(this.newIslandId);
				}

				NSTypeFrame fs = s.getCurrentFrame();
				int side = BoardCoord.orientationToMask[fs.getSideOrientation() - 'A'];

				if ((side & BoardCoord.NORTH_MASK) > 0)
				{
					BoardCoord scan = new BoardCoord(pos);
					scan.moveBy(0, -1);
					floodId(scan);
				}

				if ((side & BoardCoord.EAST_MASK) > 0)
				{
					BoardCoord scan = new BoardCoord(pos);
					scan.moveBy(+1, 0);
					floodId(scan);
				}

				if ((side & BoardCoord.SOUTH_MASK) > 0)
				{
					BoardCoord scan = new BoardCoord(pos);
					scan.moveBy(0, +1);
					floodId(scan);
				}

				if ((side & BoardCoord.WEST_MASK) > 0)
				{
					BoardCoord scan = new BoardCoord(pos);
					scan.moveBy(-1, 0);
					floodId(scan);
				}
			}
		}
	}

	private boolean floodPlayerId;

	private int newIslandId;

	private BattlePlayer newPlayer;

	private World world;

	private SquidXYHash hash;
}
