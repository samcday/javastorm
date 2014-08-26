package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalLoginReply extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZChalLoginReply;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		// Version.
		this.setServerMajorVersion(buffer.getShort());
		this.setServerMinorVersion(buffer.getShort());

		this.setSubId(buffer.getInt());

		this.setCode(buffer.get());
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();

		buffer.allocate(9);
		buffer.setPosition(0);

		buffer.putShort(this.serverMajorVersion);
		buffer.putShort(this.serverMinorVersion);
		buffer.putInt(this.subId);
		buffer.put(this.code);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

	public void setSubId(int subId)
	{
		this.subId = subId;
	}

	public int getSubId()
	{
		return subId;
	}

	public void setServerMinorVersion(int serverMinorVersion)
	{
		this.serverMinorVersion = serverMinorVersion;
	}

	public int getServerMinorVersion()
	{
		return serverMinorVersion;
	}

	public void setServerMajorVersion(int serverMajorVersion)
	{
		this.serverMajorVersion = serverMajorVersion;
	}

	public int getServerMajorVersion()
	{
		return serverMajorVersion;
	}

	private int serverMajorVersion;

	private int serverMinorVersion;

	private int subId;

	private int code;

	// ChalLoginReply codes.
	public static final int CLR_GENERIC_FAIL = 0; // unspecified error

	public static final int CLR_TRUE = 1; // all is well?

	public static final int CLR_INITIAL_INFO_SENT = 2; // ???

	public static final int CLR_ALREADY_HERE = 3; // someone with your subId is already on this server

	public static final int CLR_BAD_NAME_LENGTH = 4; // you name length is bad

	public static final int CLR_SERVER_FULL = 5; // there is no more room on this server

	public static final int CLR_SHUTDOWN = 6; // the challenge state is being shutdown.
}
