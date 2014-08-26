package org.javastorm.network.commands;

import org.javastorm.NSUtil;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalPlayerAddress extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZChalPlayerAddress;
	}

	// Gets the player index this command is updating.
	public int getPlayerIndex()
	{
		return this.playerIndex;
	}

	// Updates the supplied NSPlayer with the extra info contained in this command.
	public void updatePlayer(ZonePlayer player)
	{
		if (player != null)
		{
			player.setSubscriberID(this.subscriberID);
			player.setIp(this.ip);
		}
	}

	public void readCommandData(MyByteBuffer buffer)
	{

		// First up is the pIndex of the player in question.
		this.playerIndex = buffer.get();

		// Challenge ID of this player.
		this.subscriberID = buffer.getInt();

		// Len of IP.
		buffer.skip(1);

		// Get the IP.
		this.ip = NSUtil.ipStringToArray(buffer.getString());
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();

		StringBuilder ipString = new StringBuilder();

		for (int i = 0; i < this.ip.length; i++)
		{
			ipString.append(this.ip[i]);
			if (i < this.ip.length - 1)
				ipString.append(".");
		}

		buffer.allocate(7 + ipString.length());

		buffer.put(this.playerIndex);
		buffer.putInt(this.subscriberID);
		buffer.put(ipString.length() + 1);
		buffer.put(0);
		buffer.putStr(ipString.toString());

		return buffer;
	}

	public int[] getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = NSUtil.ipStringToArray(ip);
	}

	public int getSubscriberID()
	{
		return subscriberID;
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	public void setPlayerIndex(int playerIndex)
	{
		this.playerIndex = playerIndex;
	}

	private int playerIndex;

	private int[] ip;

	private int subscriberID;
}
