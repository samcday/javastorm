package org.javastorm.challenge;

public class Zone
{
	public Zone(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public int getNumPlayers()
	{
		return numPlayers;
	}

	public void setNumPlayers(int numPlayers)
	{
		this.numPlayers = numPlayers;
	}

	public int getAvgLevel()
	{
		return avgLevel;
	}

	public void setAvgLevel(int avgLevel)
	{
		this.avgLevel = avgLevel;
	}

	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	public int getFlag()
	{
		return flag;
	}

	public void setFlag(int flag)
	{
		this.flag = flag;
	}

	private int id;

	private int numPlayers;

	private int avgLevel;

	private String info;

	private int flag;
}
