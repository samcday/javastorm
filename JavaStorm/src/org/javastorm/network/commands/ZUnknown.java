package org.javastorm.network.commands;

import org.javastorm.util.MyByteBuffer;

public class ZUnknown extends NSCommand
{
	public ZUnknown(int commandID)
	{
		this.commandID = commandID;
	}

	public int getCommandID()
	{
		return this.commandID;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	public String toString()
	{
		return "Unknown Server Command Type #" + this.commandID;
	}

	public MyByteBuffer getCommandData()
	{
		buffer.setPosition(buffer.size());
		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	private int commandID;

	private MyByteBuffer buffer;
}
