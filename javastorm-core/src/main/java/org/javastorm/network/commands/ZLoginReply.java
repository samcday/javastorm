package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZLoginReply extends NSCommand
{
	public int getCode()
	{
		return code;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public double getEarlyCaptureTimer()
	{
		return earlyCaptureTimer;
	}

	public void setEarlyCaptureTimer(double earlyCaptureTimer)
	{
		this.earlyCaptureTimer = earlyCaptureTimer;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public int getServerMajorVersion()
	{
		return serverMajorVersion;
	}

	public void setServerMajorVersion(int serverMajorVersion)
	{
		this.serverMajorVersion = serverMajorVersion;
	}

	public int getServerMinorVersion()
	{
		return serverMinorVersion;
	}

	public void setServerMinorVersion(int serverMinorVersion)
	{
		this.serverMinorVersion = serverMinorVersion;
	}

	public int getCommandID()
	{
		return Connection.ZLoginReply;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.serverMajorVersion = buffer.getShort();
		this.serverMinorVersion = buffer.getShort();
		this.playerId = buffer.get();
		this.code = buffer.get();
		this.earlyCaptureTimer = buffer.getDouble();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(14);

		buffer.putShort(this.serverMajorVersion);
		buffer.putShort(this.serverMinorVersion);
		buffer.put(this.playerId);
		buffer.put(this.code);
		buffer.putDouble(this.earlyCaptureTimer);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	private int serverMajorVersion;

	private int serverMinorVersion;

	private int playerId;

	private int code;

	private double earlyCaptureTimer;
}
