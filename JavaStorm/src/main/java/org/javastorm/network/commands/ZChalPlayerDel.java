package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalPlayerDel extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZChalPlayerDel;
	}

	// Returns the indexes of players that are leaving.
	public int[] getPlayerIDs()
	{
		return this.playerIDs;
	}

	public void setPlayerIDs(int[] playerIDs)
	{
		this.playerIDs = playerIDs;
	}

	public int getCount()
	{
		return this.playerIDs.length;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		int count = buffer.get();

		this.playerIDs = new int[count];
		for (int i = 0; i < count; i++)
			this.playerIDs[i] = buffer.get();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(1 + this.playerIDs.length);

		buffer.put(this.playerIDs.length);

		for (int i = 0; i < this.playerIDs.length; i++)
			buffer.put(this.playerIDs[i]);

		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	private int[] playerIDs;
}
