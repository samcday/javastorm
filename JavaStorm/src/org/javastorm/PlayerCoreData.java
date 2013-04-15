package org.javastorm;

import org.javastorm.util.MyByteBuffer;

// Represents the core data of a player, how many games played, reliability, tech list, etc.
// NEtwork representation is 10 bytes in length.
public class PlayerCoreData
{
	public void setTech(int tech)
	{
		this.tech = tech;
	}

	public int getTech()
	{
		return tech;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}

	public int getRank()
	{
		return rank;
	}

	public void setGamesCompleted(int gamesCompleted)
	{
		this.gamesCompleted = gamesCompleted;
	}

	public int getGamesCompleted()
	{
		return gamesCompleted;
	}

	public void setGamesPlayed(int gamesPlayed)
	{
		this.gamesPlayed = gamesPlayed;
	}

	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	public void setAltarLevel(int altarLevel)
	{
		this.altarLevel = altarLevel;
	}

	public int getAltarLevel()
	{
		return altarLevel;
	}

	public int getLevel()
	{
		// TODO! Make this work!
		return 6;
	}

	// Writes 10 bytes.
	public void write(MyByteBuffer buffer)
	{
		buffer.putShort(this.gamesPlayed);
		buffer.putShort(this.gamesCompleted);
		buffer.put(this.rank);
		buffer.put(this.altarLevel);
		buffer.putInt(this.tech);
	}

	// Reads 10 bytes.
	public void read(MyByteBuffer buffer)
	{
		this.gamesPlayed = buffer.getShort();
		this.gamesCompleted = buffer.getShort();
		this.rank = buffer.get();
		this.altarLevel = buffer.get();
		this.tech = buffer.getInt();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PlayerCoreData: ");
		
		sb.append("gamesPlayed=").append(this.gamesPlayed).append(". ");
		sb.append("gamesCompleted=").append(this.gamesCompleted).append(". ");
		sb.append("rank=").append(this.rank).append(". ");
		sb.append("altarLevel=").append(this.altarLevel).append(". ");
		sb.append("tech=").append(this.tech).append(". ");
		
		return sb.toString();
	}

	private int gamesPlayed;

	private int gamesCompleted;

	private int rank;

	private int altarLevel; // only levels 2 and 3 are meaningful. Level 1 and "no Altar" both report 0.

	private int tech; // Bit field storing which technology you own. 6 Bits reserved for new units
}
