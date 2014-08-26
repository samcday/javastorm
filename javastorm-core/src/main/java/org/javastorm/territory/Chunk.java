package org.javastorm.territory;

public class Chunk
{
	public Chunk()
	{
		this.islandId = IslandList.INVALID_ISLAND_ID;
		this.terr = new Territory();
	}

	public void clear()
	{
		this.islandId = 0;
		this.sideOrientation = 0;
		this.terr = new Territory();
		this.indexWithinTerr = 0;
	}

	public int getIndexWithinTerr()
	{
		return indexWithinTerr;
	}

	public void setIndexWithinTerr(int indexWithinTerr)
	{
		this.indexWithinTerr = indexWithinTerr;
	}

	public int getIslandId()
	{
		return islandId;
	}

	public void setIslandId(int islandId)
	{
		this.islandId = islandId;
	}

	public int getSideOrientation()
	{
		return sideOrientation;
	}

	public void setSideOrientation(int sideOrientation)
	{
		this.sideOrientation = sideOrientation;
	}

	public Territory getTerr()
	{
		return terr;
	}

	public void setTerr(Territory terr)
	{
		this.terr = terr;
	}

	private int islandId;
	private int sideOrientation;
	private Territory terr;
	private int indexWithinTerr;
}
