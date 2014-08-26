package org.javastorm.fort;

import org.javastorm.BoardCoord;

public class IntCoord
{
	public IntCoord(BoardCoord bc)
	{
		this.x = (int) bc.getX();
		this.y = (int) bc.getY();
	}

	public IntCoord(IntCoord root, BoardCoord bc)
	{
		this.x = (int) bc.getX() - root.x;
		this.y = (int) bc.getY() - root.y;
	}

	public IntCoord(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public IntCoord(BoardCoord bc, int x, int y)
	{
		this.x = (int) bc.getX() + x;
		this.y = (int) bc.getY() + y;
	}

	public BoardCoord getPos()
	{
		return new BoardCoord(x, y);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	private int x;

	private int y;
}
