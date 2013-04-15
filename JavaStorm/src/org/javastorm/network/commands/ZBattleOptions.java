package org.javastorm.network.commands;

import org.javastorm.battle.BattleOptions;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZBattleOptions extends NSCommand
{
	// BattleOptions contains all of the battle options =p
	public void setBattleOptions(BattleOptions options)
	{
		this.options = options;
	}

	public BattleOptions getBattleOptions()
	{
		return this.options;
	}

	// Which ring are we setting battle options for?
	public void setRing(int ring)
	{
		this.ring = ring;
	}

	public int getRing()
	{
		return this.ring;
	}

	public int getCommandID()
	{
		return Connection.ZBattleOptions;
	}

	public int getVersion()
	{
		return 1;
	}

	public MyByteBuffer getCommandData()
	{
		int boSize = 0x57;

		if (this._getMinorVersion() > 78)
			boSize++;

		MyByteBuffer data = new MyByteBuffer();
		data.allocate(boSize + 3);

		// Specify which ring we are setting options for.
		// Not recommended to set anything else :P Server deems it a hack attempt *whistles*
		data.put(this.ring);

		// This stuff sets all the options.
		data.putShort(boSize);

		this.options.write(this._getMinorVersion(), data);

		return data;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.ring = buffer.get();

		this.options = new BattleOptions();
		this.options.read(this._getMinorVersion(), buffer);
	}

	private BattleOptions options;

	private int ring;

}
