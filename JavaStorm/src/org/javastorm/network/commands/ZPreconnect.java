package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

//NSPacket implementation to join a battle.
public class ZPreconnect extends NSCommand
{
	public int getPlayerIndex()
	{
		return playerIndex;
	}

	public int getPlayerSubscriberID()
	{
		return playerSubscriberID;
	}

	public int getRing()
	{
		return ring;
	}

	public int getServerSubscriberID()
	{
		return serverSubscriberID;
	}

	public int getSlot()
	{
		return slot;
	}

	public ZPreconnect()
	{
		this.playerSubscriberID = 0;
		this.playerIndex = 0;
		this.serverSubscriberID = 0;
		this.slot = 0;
		this.ring = 0;
	}

	public void setRing(int ringNo)
	{
		this.ring = ringNo;
	}

	public void setSlot(int slotNo)
	{
		this.slot = slotNo;
	}

	public void setPlayerIndex(int playerIndex)
	{
		this.playerIndex = playerIndex;
	}

	public void setPlayerSubscriberID(int playerSubscriberID)
	{
		this.playerSubscriberID = playerSubscriberID;
	}

	public void setServerSubscriberID(int serverSubscriberID)
	{
		this.serverSubscriberID = serverSubscriberID;
	}

	// Returns this packets' command id.
	public int getCommandID()
	{
		return Connection.ZPreconnect;
	}

	public int getVersion()
	{
		return 1;
	}

	@Override
	public String[] outputDebug() {
		return new String[]
		{
			"code = " + this.code,
			"serverSubscriberId = " + this.serverSubscriberID,
			"playerIndex = " + this.playerIndex,
			"playerSubscriberId = " + this.playerSubscriberID,
			"ring = " + this.ring,
			"slot = " + this.slot
        };
	}

	// Returns this packet in raw byte data.
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer data = new MyByteBuffer();
		data.allocate(24);

		data.putInt(this.code);

		data.putInt(this.serverSubscriberID);

		data.putInt(this.playerIndex);

		data.putInt(this.playerSubscriberID);

		data.putInt(this.ring);
		data.putInt(this.slot);

		return data;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.code = buffer.getInt();

		this.serverSubscriberID = buffer.getInt();
		this.playerIndex = buffer.getInt();
		this.playerSubscriberID = buffer.getInt();

		this.ring = buffer.getInt();
		this.slot = buffer.getInt();
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

	// Challenge ID of player joining battle.
	private int playerSubscriberID;

	// Code.
	private int code;

	// pIndex of player joinin battle.
	private int playerIndex;

	// Challenge ID of battlemaster.
	private int serverSubscriberID;

	// Ring and slot we're joining.
	private int slot, ring;

}