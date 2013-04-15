package org.javastorm.network.commands;

import org.javastorm.PlayerCoreData;
import org.javastorm.Version;
import org.javastorm.Netstorm;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZonePlayerStatusFlags;
import org.javastorm.network.Connection;
import org.javastorm.territory.Territory;
import org.javastorm.util.MyByteBuffer;

public class ZChalPlayerAdd extends NSCommand
{
	public int getCommandID()
	{
		return Connection.ZChalPlayerAdd;
	}

	public ZonePlayer getNSPlayer()
	{
		return this.player;
	}

	public void setNSPlayer(ZonePlayer player)
	{
		this.player = player;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		int pIndex = buffer.get();
		int subscriberID = buffer.getInt();
		int compatibleMinorVersion = buffer.getShort();

		this.player = new ZonePlayer(new Version(Netstorm.MAJOR_VERSION, buffer.getShort()));

		// First up is the pIndex of this player.
		this.player.setPlayerIndex(pIndex);
		this.player.setSubscriberID(subscriberID);

		this.player.setCompatibleMinorVersion(compatibleMinorVersion);

		this.player.setLanguageId(buffer.get());
		this.player.setFlags(buffer.getInt());
		buffer.skip(1);

		Territory terr = new Territory();
		terr.read(buffer);
		this.player.setTerrain(terr);

		PlayerCoreData coreData = new PlayerCoreData();
		coreData.read(buffer);
		this.player.setCoreData(coreData);

		this.player.getStatusFlags().copy(new ZonePlayerStatusFlags(buffer.getInt()));

		this.player.getPos().moveTo(buffer.getFloat(), buffer.getFloat());
		this.player.setRing(buffer.get());
		this.player.setSlot(buffer.get());

		this.player.setStatusCode(buffer.getInt());

		// Get the nickname.
		this.player.setNickname(buffer.getString());
	}

	private ZonePlayer player;

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		ZonePlayer player = this.player;

		buffer.allocate(49 + player.getNickname().length() + 1);
		buffer.setPosition(0);

		buffer.put(player.getPlayerIndex());
		buffer.putInt(player.getSubscriberID());
		buffer.putShort(player.getCompatibleMinorVersion());
		buffer.putShort(player.getVersion().getMinorVersion());
		buffer.put(player.getLanguageId());
		buffer.putInt(player.getFlags());
		buffer.put(player.getNickname().length() + 1);
		player.getTerrain().write(buffer);
		player.getCoreData().write(buffer);
		buffer.putInt(player.getStatusFlags().getRaw());
		buffer.putFloat(player.getPos().getX());
		buffer.putFloat(player.getPos().getY());
		buffer.put(player.getRing());
		buffer.put(player.getSlot());
		buffer.putInt(player.getStatusCode());
		buffer.putStr(player.getNickname());
		buffer.put(0);
		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}
}
