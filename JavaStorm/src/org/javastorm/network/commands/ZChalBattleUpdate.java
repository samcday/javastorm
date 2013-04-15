package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalBattleUpdate extends NSCommand
{
	public void setRing(int ring)
	{
		this.ring = ring;
	}

	public int getCommandID()
	{
		return Connection.ZChalBattleUpdate;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public int getExtra()
	{
		return extra;
	}

	public void setExtra(int extra)
	{
		this.extra = extra;
	}

	public int getRing()
	{
		return this.ring;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.ring = buffer.get();
		this.extra = buffer.getInt();
		int descLen = buffer.get();
		this.desc = buffer.getString(descLen);
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(7 + desc.length());
		buffer.setPosition(0);

		buffer.put(this.ring);
		buffer.putInt(this.extra);
		buffer.put(this.desc.length() + 1);
		buffer.putStr(this.desc);
		buffer.put(0);

		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	private int ring;

	private int extra;

	private String desc = "";
}
