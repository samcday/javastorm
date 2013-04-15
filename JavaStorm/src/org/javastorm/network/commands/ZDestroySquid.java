package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZDestroySquid extends NSCommand
{

	public int getDestroyFlags()
	{
		return destroyFlags;
	}

	public void setDestroyFlags(int destroyFlags)
	{
		this.destroyFlags = destroyFlags;
	}

	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid = sid;
	}

	public MyByteBuffer getCommandData()
	{
		return null;
	}

	public int getCommandID()
	{
		return Connection.ZDestroySquid;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.sid = buffer.getShort();
		this.destroyFlags = buffer.getShort();
	}

	private int sid;

	private int destroyFlags;
}
