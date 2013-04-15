package org.javastorm.territory;

import org.javastorm.BoardCoord;

public class ChunkCoord
{
	public ChunkCoord()
	{
		this.x = this.y = 0;
	}

	public ChunkCoord(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public ChunkCoord(BoardCoord bc)
	{
		this.x = (int) (bc.getX() / CHUNK_DIM);
		this.y = (int) (bc.getY() / CHUNK_DIM);
	}

	public boolean isValid()
	{
		return this.x != -1 && this.y != -1;
	}

	public void move(int bx, int by)
	{
		this.x += bx;
		this.y += by;
	}

	public BoardCoord getBoardCoord()
	{
		return new BoardCoord(this.x * CHUNK_DIM, this.y * CHUNK_DIM);
	}

	public BoardCoord getBottomRight()
	{
		return new BoardCoord(((x + 1) * CHUNK_DIM) - 1, ((y + 1) * CHUNK_DIM) - 1);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	private int x, y;

	public static final int RIM_SIZE = 7;

	public static final int INTERIOR_DIM = 2;

	public static final int CHUNK_DIM = INTERIOR_DIM + RIM_SIZE + RIM_SIZE;
}
