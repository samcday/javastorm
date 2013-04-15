package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.territory.Territory;
import org.javastorm.util.MyByteBuffer;

public class ZLoginBegin extends NSCommand
{
	public ZLoginBegin()
	{
		this.terrData = new Territory();
	}

	public int getAscendancyRand()
	{
		return ascendancyRand;
	}

	public void setAscendancyRand(int ascendancyRand)
	{
		this.ascendancyRand = ascendancyRand;
	}

	public double getAscendancyTimer()
	{
		return ascendancyTimer;
	}

	public void setAscendancyTimer(double ascendancyTimer)
	{
		this.ascendancyTimer = ascendancyTimer;
	}

	public boolean getInAscendancy()
	{
		return inAscendancy;
	}

	public void setInAscendancy(boolean inAscendancy)
	{
		this.inAscendancy = inAscendancy;
	}

	public int getLiberatedStormPower()
	{
		return liberatedStormPower;
	}

	public void setLiberatedStormPower(int liberatedStormPower)
	{
		this.liberatedStormPower = liberatedStormPower;
	}

	public int getPlayerID()
	{
		return playerID;
	}

	public void setPlayerID(int playerID)
	{
		this.playerID = playerID;
	}

	public int getCommandID()
	{
		return Connection.ZLoginBegin;
	}

	public Territory getTerrData()
	{
		return terrData;
	}

	public void setTerrData(Territory terrData)
	{
		this.terrData = terrData;

		if (this.terrData == null)
			this.terrData = new Territory();
	}

	@Override
	public String[] outputDebug() {
		return new String[] {
			"playerID = " + this.playerID,
			"terrData = " + this.terrData.toString(),
			"ascendandancyTimer = " + this.ascendancyTimer,
			"inAscendancy = " + this.inAscendancy,
			"ascendancyRand = " + this.ascendancyRand,
			"liberatedStormPower = " + this.liberatedStormPower
		};
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.playerID = buffer.getInt();

		this.terrData = new Territory();
		this.terrData.read(buffer);

		this.ascendancyTimer = buffer.getDouble();
		this.inAscendancy = buffer.getInt() == 1 ? true : false;
		this.ascendancyRand = buffer.getInt();
		this.liberatedStormPower = buffer.getInt();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(30);

		buffer.putInt(this.playerID);
		this.terrData.write(buffer);

		buffer.putDouble(this.ascendancyTimer);
		buffer.putInt(this.inAscendancy ? 1 : 0);
		buffer.putInt(this.ascendancyRand);
		buffer.putInt(this.liberatedStormPower);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	private int playerID;

	private Territory terrData;

	private double ascendancyTimer;

	private boolean inAscendancy;

	private int ascendancyRand;

	private int liberatedStormPower;
}
