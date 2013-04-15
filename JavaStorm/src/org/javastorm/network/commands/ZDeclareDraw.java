package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZDeclareDraw extends NSCommand
{
	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public int getCommandID()
	{
		return Connection.ZDeclareDraw;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(2);

		buffer.put(this.playerId);
		buffer.put(this.state);

		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.playerId = buffer.get();
		this.state = buffer.get();
	}

	private int playerId;

	private int state;
}
