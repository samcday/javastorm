package org.javastorm.territory;

import org.javastorm.util.MyByteBuffer;

public class TerritoryList
{
	public TerritoryList()
	{
		this.terrList = new Territory[Territory.MR_TERR_COUNT];
	}

	public void setTerritory(int index, Territory terr)
	{
		this.terrList[index] = terr;
	}

	public Territory getTerritory(int index)
	{
		return this.terrList[index];
	}

	public void write(MyByteBuffer buffer)
	{
		for (int i = 0; i < this.terrList.length; i++)
		{
			this.terrList[i].write(buffer);
		}
	}

	public void read(MyByteBuffer buffer)
	{
		for (int i = 0; i < this.terrList.length; i++)
		{
			this.terrList[i] = new Territory();
			this.terrList[i].read(buffer);
		}
	}

	private Territory terrList[];
}
