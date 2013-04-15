package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZReconnectState extends NSCommand
{
	public int getClockmain()
	{
		return clockmain;
	}

	public void setClockmain(int clockmain)
	{
		this.clockmain = clockmain;
	}

	public int getCommandID()
	{
		return Connection.ZReconnectState;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(4);
		buffer.putInt(this.clockmain);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.clockmain = buffer.getInt();
	}

	private int clockmain;
}
