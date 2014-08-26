package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZCreateCanon extends NSCommand
{
	public int getCanon()
	{
		return canon;
	}

	public void setCanon(int canon)
	{
		this.canon = canon;
	}

	public int getCreateFlags()
	{
		return createFlags;
	}

	public void setCreateFlags(int createFlags)
	{
		this.createFlags = createFlags;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public int getRotation()
	{
		return rotation;
	}

	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}

	public int[] getSid()
	{
		return sid;
	}

	public void setSid(int[] sid)
	{
		this.sid = sid;
	}

	public int getSpecial()
	{
		return special;
	}

	public void setSpecial(int special)
	{
		this.special = special;
	}

	public double getStreakArrivalTime()
	{
		return streakArrivalTime;
	}

	public void setStreakArrivalTime(double streakArrivalTime)
	{
		this.streakArrivalTime = streakArrivalTime;
	}

	public int getTypeNum()
	{
		return typeNum;
	}

	public void setTypeNum(int typeNum)
	{
		this.typeNum = typeNum;
	}

	public float getX()
	{
		return x;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public float getY()
	{
		return y;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public int getCommandID()
	{
		return Connection.ZCreateCanon;
	}

	@Override
	public String[] outputDebug() {
		String[] debug = new String[10 + this.sid.length];
		
		debug[0] = "typeNum = " + this.typeNum;
		debug[1] = "x = " + this.x;
		debug[2] = "y = " + this.y;
		debug[3] = "canon = " + this.canon;
		debug[4] = "rotation = " + this.rotation;
		debug[5] = "playerId = " + this.playerId;
		debug[6] = "createFlags = " + this.createFlags;
		debug[7] = "special = " + this.special;
		debug[8] = "streakArrivalTime = " + this.streakArrivalTime;
		debug[9] = "sidCount = " + this.sid.length;
		
		for(int i = 0; i < this.sid.length; i++)
			debug[10 + i] = "sid[" + i + "] = " + this.sid[i];
		
		return debug;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(23 + (this.sid.length * 2));

		buffer.put(this.typeNum);

		buffer.putFloat(this.x);
		buffer.putFloat(this.y);

		buffer.put(this.canon);
		buffer.put(this.rotation);

		buffer.put(this.playerId);
		buffer.put(this.createFlags);
		buffer.put(this.special);

		buffer.putDouble(this.streakArrivalTime);

		buffer.put(this.sid.length);

		for (int i = 0; i < this.sid.length; i++)
			buffer.putShort(this.sid[i]);

		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.typeNum = buffer.get();

		this.x = buffer.getFloat();
		this.y = buffer.getFloat();

		this.canon = buffer.get();
		this.rotation = buffer.get();
		this.playerId = buffer.get();
		this.createFlags = buffer.get();
		this.special = buffer.get();

		this.streakArrivalTime = buffer.getDouble();

		int sidCount = buffer.get();

		this.sid = new int[sidCount];
		for (int i = 0; i < sidCount; i++)
			this.sid[i] = buffer.getShort();
	}

	private int typeNum;

	private float x;

	private float y;

	private int canon;

	private int rotation;

	private int playerId;

	private int createFlags;

	private int special;

	private double streakArrivalTime;

	private int[] sid;
}
