package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZLoadFort extends NSCommand
{
	public int getDensity()
	{
		return density;
	}

	public void setDensity(int density)
	{
		this.density = density;
	}

	public int getFortFlags()
	{
		return fortFlags;
	}

	public void setFortFlags(int fortFlags)
	{
		this.fortFlags = fortFlags;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getCommandID()
	{
		return Connection.ZLoadFort;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.fortFlags = buffer.get();
		this.density = buffer.get();
		int len = buffer.getInt();
		this.name = buffer.getString(len);
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(6 + this.name.length() + 1);
		buffer.put(this.fortFlags);
		buffer.put(this.density);
		buffer.putInt(this.name.length());
		buffer.putStr(this.name);
		buffer.put(0);
		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	private int fortFlags;

	private int density;

	private String name;
}
