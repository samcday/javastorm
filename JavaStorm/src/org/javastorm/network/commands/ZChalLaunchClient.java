package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalLaunchClient extends NSCommand
{
	public int getGameID()
	{
		return gameID;
	}

	public void setGameID(int gameID)
	{
		this.gameID = gameID;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getCommandID()
	{
		return Connection.ZChalLaunchClient;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.gameID = buffer.getInt();

		this.ip = buffer.getString();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(34);

		buffer.putInt(this.gameID);
		buffer.putStr(this.ip, 30);

		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	private int gameID;

	private String ip;
}
