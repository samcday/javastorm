package org.javastorm.network.commands;

import org.javastorm.challenge.ZonePlayerStatusFlags;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

//NSClientCommand implementation to perform a Ring operation.
public class ZChalRequestChangeStatus extends NSCommand
{
	public ZonePlayerStatusFlags getStatusFlags()
	{
		return this.statusFlags;
	}

	public int getSubscriberID()
	{
		return subscriberID;
	}

	public ZChalRequestChangeStatus()
	{
		this.subscriberID = 0;
	}

	public void setStatusFlags(ZonePlayerStatusFlags statusFlags)
	{
		this.statusFlags = statusFlags;
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	// Returns this packets' command id.
	public int getCommandID()
	{
		return Connection.ZChalRequestChangeStatus;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.subscriberID = buffer.getInt();
		this.statusFlags = new ZonePlayerStatusFlags(buffer.getInt());
	}

	// Returns this packet in raw byte data.
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer data = new MyByteBuffer();
		data.allocate(8);
		data.setPosition(0);

		// Challenge ID.
		data.putInt(this.subscriberID);

		// Status flags.
		data.putInt(this.statusFlags.getRaw());

		return data;
	}

	private int subscriberID;

	private ZonePlayerStatusFlags statusFlags;
}