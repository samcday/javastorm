package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZPathProcess extends NSCommand
{
	public int getAttachToSid()
	{
		return attachToSid;
	}

	public void setAttachToSid(int attachToSid)
	{
		this.attachToSid = attachToSid;
	}

	public int getBuildingSid()
	{
		return buildingSid;
	}

	public void setBuildingSid(int buildingSid)
	{
		this.buildingSid = buildingSid;
	}

	public int getCarriedQ()
	{
		return carriedQ;
	}

	public void setCarriedQ(int carriedQ)
	{
		this.carriedQ = carriedQ;
	}

	public int getCarriedType()
	{
		return carriedType;
	}

	public void setCarriedType(int carriedType)
	{
		this.carriedType = carriedType;
	}

	public int getDaisSid()
	{
		return daisSid;
	}

	public void setDaisSid(int daisSid)
	{
		this.daisSid = daisSid;
	}

	public int getDaisWalkPosIndex()
	{
		return daisWalkPosIndex;
	}

	public void setDaisWalkPosIndex(int daisWalkPosIndex)
	{
		this.daisWalkPosIndex = daisWalkPosIndex;
	}

	public int getFormSid()
	{
		return formSid;
	}

	public void setFormSid(int formSid)
	{
		this.formSid = formSid;
	}

	public int[] getNode()
	{
		return node;
	}

	public void setNode(int[] node)
	{
		this.node = node;
	}

	public int getStartTime()
	{
		return startTime;
	}

	public void setStartTime(int startTime)
	{
		this.startTime = startTime;
	}

	public int getSx()
	{
		return sx;
	}

	public void setSx(int sx)
	{
		this.sx = sx;
	}

	public int getSy()
	{
		return sy;
	}

	public void setSy(int sy)
	{
		this.sy = sy;
	}

	public int getCommandID()
	{
		return Connection.ZPathProcess;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(20 + this.node.length);

		buffer.putShort(this.formSid);
		buffer.putShort(this.attachToSid);

		buffer.put(this.carriedType);
		buffer.putShort(this.carriedQ);

		buffer.putShort(this.buildingSid);

		buffer.putShort(this.node.length);

		buffer.put(this.sx);
		buffer.put(this.sy);

		buffer.putInt(this.startTime);

		buffer.putShort(this.daisSid);
		buffer.put(this.daisWalkPosIndex);

		for (int i = 0; i < this.node.length; i++)
			buffer.put(this.node[i]);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.formSid = buffer.getShort();
		this.attachToSid = buffer.getShort();

		this.carriedType = buffer.get();
		this.carriedQ = buffer.getShort();

		this.buildingSid = buffer.getShort();

		int nodeCount = buffer.getShort();

		this.sx = buffer.get();
		this.sy = buffer.get();

		this.startTime = buffer.getInt();

		this.daisSid = buffer.getShort();
		this.daisWalkPosIndex = buffer.get();

		this.node = new int[nodeCount];
		for (int i = 0; i < nodeCount; i++)
			this.node[i] = buffer.get();
	}

	private int formSid;

	private int attachToSid;

	private int carriedType;

	private int carriedQ;

	private int buildingSid;

	private int sx;

	private int sy;

	private int startTime;

	private int daisSid;

	private int daisWalkPosIndex;

	private int node[];
}
