package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalBattleDel extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZChalBattleDel;
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
		this.ringNumber = buffer.get();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(1);
		buffer.put(this.ringNumber);
		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	private int ringNumber;
}
