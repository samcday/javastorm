package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalServerInfo extends NSCommand
{
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(5 + (this.mod.length() + 1) + (this.contact.length * 4));

		buffer.putShort(this.mod.length() + 1);
		buffer.put(this.contact.length * 4);
		buffer.put(this.activeMapW);
		buffer.put(this.activeMapH);
		buffer.putStr(this.mod);
		buffer.put(0);

		for (int i = 0; i < this.contact.length; i++)
			buffer.putFloat(this.contact[i]);

		return buffer;
	}

	public int getCommandID()
	{
		return Connection.ZChalServerInfo;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.getShort();
		int contactLen = buffer.get();
		this.activeMapW = buffer.get();
		this.activeMapH = buffer.get();

		this.mod = buffer.getString();

		if (contactLen == 1)
			return;

		this.contact = new float[contactLen / 4];
		for (int i = 0; i < (contactLen / 4); i++)
		{
			this.contact[i] = buffer.getFloat();
		}
	}

	public int getActiveMapH()
	{
		return activeMapH;
	}

	public void setActiveMapH(int activeMapH)
	{
		this.activeMapH = activeMapH;
	}

	public int getActiveMapW()
	{
		return activeMapW;
	}

	public void setActiveMapW(int activeMapW)
	{
		this.activeMapW = activeMapW;
	}

	public float[] getContact()
	{
		return contact;
	}

	public void setContact(float[] contact)
	{
		this.contact = contact;
	}

	public String getMod()
	{
		return mod;
	}

	public void setMod(String mod)
	{
		this.mod = mod;
	}

	private String mod;

	private float[] contact;

	private int activeMapW;

	private int activeMapH;
}
