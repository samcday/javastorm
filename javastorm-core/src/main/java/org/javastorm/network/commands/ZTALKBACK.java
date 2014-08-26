package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZTALKBACK extends NSCommand
{

	public MyByteBuffer getCommandData()
	{
		return null;
	}

	public int getCommandID()
	{
		return Connection.ZTALKBACK;
	}

	public int getVersion()
	{
		return 0;
	}

	public void readCommandData(MyByteBuffer buffer)
	{

	}
}
