package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZInitialMoney extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZInitialMoney;
	}

	public int getMoney(int index)
	{
		return this.money[index];
	}

	public void setMoney(int index, int money)
	{
		this.money[index - 1] = money;
	}

	public int getNumPlayers()
	{
		return numPlayers;
	}

	public void setNumPlayers(int numPlayers)
	{
		this.numPlayers = numPlayers;
		this.money = new int[numPlayers];
	}

	@Override
	public String[] outputDebug() {
		String[] debug = new String[this.numPlayers + 1];
		debug[0] = "numPlayers = " + this.numPlayers;
		for(int i = 0; i < this.numPlayers; i++)
			debug[i + 1] = i + ": " + this.money[i];
		
		return debug;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.numPlayers = buffer.getInt();

		this.money = new int[this.numPlayers];
		if (this._getMinorVersion() == 78)
		{
			for (int i = 0; i < this.numPlayers; i++)
			{
				this.money[i] = buffer.getInt();
			}
		}
		else
		{
			for (int i = 0; i < this.numPlayers; i++)
			{
				this.money[i] = buffer.getShort();
			}
		}
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();

		int size = 4;

		if (this._getMinorVersion() == 78)
		{
			size += (this.numPlayers + 1) * 4;
		}
		else
		{
			size += (this.numPlayers + 1) * 2;
		}

		buffer.allocate(size);

		buffer.putInt(this.numPlayers + 1);

		if (this._getMinorVersion() == 78)
		{
			buffer.putInt(-1);

			for (int i = 0; i < this.numPlayers; i++)
				buffer.putInt(this.money[i]);
		}
		else
		{
			buffer.putShort(-1);
			for (int i = 0; i < this.numPlayers; i++)
			{
				buffer.putShort(this.money[i]);
			}
		}

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	private int numPlayers;

	private int money[];
}
