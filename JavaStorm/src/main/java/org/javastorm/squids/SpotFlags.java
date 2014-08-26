package org.javastorm.squids;

import java.util.Arrays;

import org.javastorm.BoardCoord;
import org.javastorm.Netstorm;

public class SpotFlags
{
	public SpotFlags()
	{
		this.spot = new int[Netstorm.BOARDDIM_IN_TILES * Netstorm.BOARDDIM_IN_TILES];
		this.clearSpotArray();
	}

	public void clearSpotArray()
	{
		Arrays.fill(this.spot, 0);
	}

	public final int getSpotFlags(int x, int y)
	{
		return this.spot[(y << 8) + x];
	}

	public final int getSpotFlags(float x, float y)
	{
		return this.getSpotFlags((int) Math.ceil(x), (int) Math.ceil(y));
	}

	public final int getSpotFlags(BoardCoord bc)
	{
		return this.getSpotFlags(bc.getX(), bc.getY());
	}

	public final void setSpotFlags(int x, int y, int flags)
	{
		this.spot[(y << 8) + x] = flags;
	}

	public final void setSpotFlags(float x, float y, int flags)
	{
		this.setSpotFlags((int) Math.ceil(x), (int) Math.ceil(y), flags);
	}

	public final void orSpotFlags(int x, int y, int flags)
	{
		this.spot[(y << 8) + x] |= flags;
	}

	public final void orSpotFlags(float x, float y, int flags)
	{
		this.orSpotFlags((int) Math.ceil(x), (int) Math.ceil(y), flags);
	}

	public final int orSpotFlagsRect(int sx, int sy, int ex, int ey)
	{
		int result = 0;

		sy = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, sy));
		ey = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, ey));
		sx = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, sx));
		ex = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, ex));

		for (int y = sy; y <= ey; ++y)
		{
			int index = ((y << 8) + sx);

			for (int x = sx; x <= ex; ++x, index++)
				result |= this.spot[index];
		}

		return result;
	}

	public final int andSpotFlagsRect(int sx, int sy, int ex, int ey)
	{
		int result = 0xFFFF;

		sy = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, sy));
		ey = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, ey));
		sx = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, sx));
		ex = Math.min(Netstorm.BOARDDIM_IN_TILES - 1, Math.max(1, ex));

		for (int y = sy; y <= ey; ++y)
		{
			int index = ((y << 8) + sx);

			for (int x = sx; x <= ex; ++x, index++)
				result |= this.spot[index];
		}

		return result;
	}

	public final int andSpotFlagsRect(float sx, float sy, float ex, float ey)
	{
		return this.andSpotFlagsRect((int) Math.ceil(sx), (int) Math.ceil(sy), (int) Math.ceil(ex), (int) Math.ceil(ey));
	}

	public final void andSpot(int mask)
	{
		for (int i = 0; i < this.spot.length; i++)
		{
			this.spot[i] = this.spot[i] & mask;
		}
	}

	private int[] spot;
}
