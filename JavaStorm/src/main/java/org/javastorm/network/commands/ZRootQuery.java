package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZRootQuery extends NSCommand
{
	public int getChecksum()
	{
		return checksum;
	}

	public void setChecksum(int checksum)
	{
		this.checksum = checksum;
	}

	public int[] getFriends()
	{
		return friends;
	}

	public void setFriends(int[] friends)
	{
		this.friends = friends;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public int getNumFriends()
	{
		return numFriends;
	}

	public void setNumFriends(int numFriends)
	{
		this.numFriends = numFriends;
	}

	public int getRequest()
	{
		return request;
	}

	public void setRequest(int request)
	{
		this.request = request;
	}

	public ZRootQuery()
	{
		this.request = 0;
		this.numFriends = 0;
		this.level = 1;
		this.friends = null;
	}

	public int getCommandID()
	{
		return Connection.ZRootQuery;
	}

	public int getVersion()
	{
		return 1;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(10 + (this.numFriends * 4));

		buffer.put(this.request);
		buffer.put(this.numFriends);
		buffer.putInt(this.checksum);
		buffer.putInt(this.level);

		for (int i = 0; i < this.numFriends; i++)
		{
			buffer.putInt(this.friends[i]);
		}

		return buffer;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.request = buffer.get();
		this.numFriends = buffer.get();
		this.checksum = buffer.getInt();
		this.level = buffer.getInt();

		for (int i = 0; i < this.numFriends; i++)
		{
			this.friends[i] = buffer.getInt();
		}
	}

	private int request;

	private int numFriends;

	private int checksum;

	private int level;

	private int[] friends;
}
