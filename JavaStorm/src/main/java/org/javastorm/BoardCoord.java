package org.javastorm;

public class BoardCoord
{
	public BoardCoord()
	{
		this.x = 0;
		this.y = 0;
	}

	public BoardCoord(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public BoardCoord(BoardCoord bc)
	{
		this.x = bc.getX();
		this.y = bc.getY();
	}

	public int getDirTo(BoardCoord bc)
	{
		return this.getDirTo(bc, false);
	}

	public int getDirTo(BoardCoord bc, boolean cardinal)
	{
		float dx = bc.x - x;
		float dy = bc.y - y;

		if (cardinal)
		{
			if (Math.abs(dx) > Math.abs(dy))
				if (dx > 0)
					return EAST;
				else
					return WEST;
			else if (dy > 0)
				return SOUTH;
			else
				return NORTH;
		}
		else
		{
			if (dx > 0)
			{
				if (dy < 0)
					return NORTH_EAST;
				else if (dy > 0)
					return SOUTH_EAST;
				else
					return EAST;
			}
			else
			{
				if (dx < 0)
				{
					if (dy < 0)
						return NORTH_WEST;
					else if (dy > 0)
						return SOUTH_WEST;
					else
						return WEST;
				}
				else
				{
					if (dy < 0)
						return NORTH;
					else if (dy > 0)
						return SOUTH;
					else
					{
						assert (false);
						return NO_DIRECTION;
					}
				}
			}
		}
	}

	public void makeInvalid()
	{
		this.x = this.y = BoardCoord.INVALID_COORD;
	}

	public void roundUp()
	{
		this.x = (int) (this.x + 0.5f);
		this.y = (int) (this.y + 0.5f);
	}

	public void roundDown()
	{
		this.x = (int) (this.x - 0.5f);
		this.y = (int) (this.y - 0.5f);
	}

	public boolean isValid()
	{
		return (0 <= this.x && this.x < Netstorm.BOARDDIM_IN_TILES) && (0 <= this.y && this.y < Netstorm.BOARDDIM_IN_TILES);
	}

	public boolean isInvalid()
	{
		return !this.isValid();
	}

	public float getX()
	{
		return x;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public float getY()
	{
		return y;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public boolean equals(BoardCoord bc)
	{
		if (bc.getX() != this.x)
			return false;

		if (bc.getY() != this.y)
			return false;

		return true;
	}

	public void moveBy(float x, float y)
	{
		this.x += x;
		this.y += y;
	}

	public void moveBy(BoardCoord bc)
	{
		this.x += bc.getX();
		this.y += bc.getY();
	}

	public void moveTo(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void moveTo(BoardCoord bc)
	{
		this.x = bc.getX();
		this.y = bc.getY();
	}

	public String toString()
	{
		return this.x + "x" + this.y;
	}

	public int hashCode()
	{
		return (int) this.x * (int) this.y;
	}

	public boolean equals(Object obj)
	{
		BoardCoord other = (BoardCoord) obj;
		if (other.getX() == this.x && other.getY() == this.y)
			return true;
		return false;
	}

	private float x;

	private float y;

	public static final float INVALID_COORD = -10000.0f;

	public static final int NUM_DIRS = (8);

	public static char maskToOrientation[] =
	{
	// WSEN
	'P', // 0000
	'N', // 0001
	'O', // 0010
	'I', // 0011
	'L', // 0100
	'J', // 0101
	'F', // 0110
	'B', // 0111
	'M', // 1000
	'H', // 1001
	'K', // 1010
	'E', // 1011
	'G', // 1100
	'D', // 1101
	'C', // 1110
	'A', // 1111

	// SSNN
	// WEEW
	};

	public static final int NO_DIRECTION = -1;

	public static final int NORTH = 0;

	public static final int NORTH_EAST = 1;

	public static final int EAST = 2;

	public static final int SOUTH_EAST = 3;

	public static final int SOUTH = 4;

	public static final int SOUTH_WEST = 5;

	public static final int WEST = 6;

	public static final int NORTH_WEST = 7;

	public static final int NORTH_MASK = 0x0001;

	public static final int EAST_MASK = 0x0002;

	public static final int SOUTH_MASK = 0x0004;

	public static final int WEST_MASK = 0x0008;

	public static final int ALL_MASKS = (NORTH_MASK | EAST_MASK | SOUTH_MASK | WEST_MASK);

	public static final int NORTH_WEST_MASK = NORTH_MASK;

	public static final int NORTH_EAST_MASK = EAST_MASK;

	public static final int SOUTH_EAST_MASK = SOUTH_MASK;

	public static final int SOUTH_WEST_MASK = WEST_MASK;

	public static int oppositeDir[] =
	{ 4, 5, 6, 7, 0, 1, 2, 3 };

	public static int dirMasks[] =
	{ NORTH_MASK, NORTH_EAST_MASK, EAST_MASK, SOUTH_EAST_MASK, SOUTH_MASK, SOUTH_WEST_MASK, WEST_MASK, NORTH_WEST_MASK };

	public static final int orientationToMask[] =
	{
	// WSEN
	15, // 'A',		// 1111
	7, // 'B',		// 0111
	14, // 'C',		// 1110
	13, // 'D',		// 1101
	11, // 'E',		// 1011
	6, // 'F',		// 0110
	12, // 'G',		// 1100
	9, // 'H',   	// 1001
	3, // 'I',		// 0011
	5, // 'J',		// 0101
	10, // 'K',		// 1010
	4, // 'L',		// 0100
	8, // 'M',		// 1000
	1, // 'N',		// 0001
	2, // 'O',		// 0010
	0, // 'P'		// 0000
	// SSNN
	// WEEW
	};

	public static final boolean dirIsCardinal(int dir)
	{
		return (dir % 2) == 0;
	}
}
