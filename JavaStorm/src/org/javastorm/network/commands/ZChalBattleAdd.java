package org.javastorm.network.commands;

import org.javastorm.BoardCoord;
import org.javastorm.network.Connection;
import org.javastorm.territory.Territory;
import org.javastorm.util.MyByteBuffer;

public class ZChalBattleAdd extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZChalBattleAdd;
	}

	public int getFlags()
	{
		return flags;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public Territory getBountyTerr()
	{
		return bountyTerr;
	}

	public void setBountyTerr(Territory terr)
	{
		this.bountyTerr = terr;
	}

	public BoardCoord getPos()
	{
		return pos;
	}

	public void setPos(BoardCoord pos)
	{
		this.pos = pos;
	}

	public int getRingNumber()
	{
		return this.ringNumber;
	}

	public void setRingNumber(int ringNumber)
	{
		this.ringNumber = ringNumber;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		// Which ring?
		this.ringNumber = buffer.get();

		this.flags = buffer.get();

		this.bountyTerr.read(buffer);

		this.pos = new BoardCoord(buffer.getFloat(), buffer.getFloat());
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(16);

		buffer.put(this.ringNumber);
		buffer.put(this.flags);

		this.bountyTerr.write(buffer);

		buffer.putFloat(this.pos.getX());
		buffer.putFloat(this.pos.getY());

		return buffer;
	}

	public int getVersion()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	private int ringNumber;

	private int flags;

	private Territory bountyTerr = new Territory();

	private BoardCoord pos;
}
