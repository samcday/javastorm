package org.javastorm.network.commands;

import org.javastorm.BoardCoord;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

//NSPacket implementation to move island in zones.
public class ZChalRequestSailTo extends NSCommand
{
	public ZChalRequestSailTo()
	{
		this.slot = 0;
		this.ring = 0;
	}

	public BoardCoord getPos()
	{
		return this.bc;
	}

	public void setPos(BoardCoord bc)
	{
		this.bc = bc;
	}

	public int getRing()
	{
		return this.ring;
	}

	public void setRing(int ringNo)
	{
		this.ring = ringNo;
	}

	public int getSlot()
	{
		return this.slot;
	}

	public void setSlot(int slotNo)
	{
		this.slot = slotNo;
	}

	// Returns this packets' command id.
	public int getCommandID()
	{
		return Connection.ZChalRequestSailTo;
	}

	public int getVersion()
	{
		return 1;
	}

	// Returns this packet in raw byte data.
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer data = new MyByteBuffer();
		data.allocate(10);
		data.setPosition(0);

		// Location.
		if (this.bc != null)
		{
			data.putFloat(this.bc.getX());
			data.putFloat(this.bc.getY());
		}
		else
		{
			data.putFloat(1f);
			data.putFloat(1f);
		}

		// Ring and slot.
		data.put(this.ring);
		data.put(this.slot);

		return data;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.bc = new BoardCoord(buffer.getFloat(), buffer.getFloat());
		this.ring = buffer.get();
		this.slot = buffer.get();
	}

	// The location we will be moving to.
	private BoardCoord bc;

	// The slot and ring being joined (0 both if not joining any).
	private int slot, ring;

}