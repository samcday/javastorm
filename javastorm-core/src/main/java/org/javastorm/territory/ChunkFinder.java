package org.javastorm.territory;

import org.javastorm.World;

public class ChunkFinder
{
	public ChunkFinder(World world)
	{
		this.cc = new ChunkCoord(0, 0);
		this.cMap = world.getChunkMap();
		this.chunk = this.cMap.getChunk(this.cc);
	}

	public ChunkFinder(ChunkMap cMap)
	{
		this.cc = new ChunkCoord(0, 0);
		this.cMap = cMap;
		this.chunk = this.cMap.getChunk(this.cc);
	}

	public boolean isValid()
	{
		return this.cc.getY() < this.cMap.getYLen();
	}

	public void advance()
	{
		this.cc.move(1, 0);

		if (this.cc.getX() >= this.cMap.getXLen())
		{
			this.cc.setX(0);
			this.cc.move(0, 1);
		}

		if (this.isValid())
		{
			this.chunk = this.cMap.getChunk(this.cc);
		}
		else
		{
			chunk = null;
		}
	}

	public Chunk getChunk()
	{
		return this.chunk;
	}

	private ChunkCoord cc;

	private Chunk chunk;

	private ChunkMap cMap;
}
