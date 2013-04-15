package org.javastorm.network.commands;

import org.javastorm.BoardCoord;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZonePlayerStatusFlags;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalPlayerUpdate extends NSCommand
{

	public int getCommandID()
	{
		return Connection.ZChalPlayerUpdate;
	}

	// Gets the player index for this action command.
	public int getPlayerIndex()
	{
		return this.playerIndex;
	}

	// Updates the supplied NSPlayer with all the information contained in this command.
	public void updatePlayer(ZonePlayer player)
	{
		player.getPos().moveTo(this.pos);
		player.setRing(this.ring);
		player.setSlot(this.slot);

		player.getStatusFlags().copy(this.statusFlags);
	}

	public int getRing()
	{
		return this.ring;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		// Index of player that did something.
		this.playerIndex = buffer.get();

		this.statusFlags = new ZonePlayerStatusFlags(buffer.getShort());

		// The co-ordinates of the players new location.
		this.pos = new BoardCoord(buffer.getFloat(), buffer.getFloat());

		// If the player is in a ring, it will be specified here.
		this.ring = buffer.get();
		this.slot = buffer.get();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(13);

		buffer.put(this.playerIndex);
		buffer.putShort(this.statusFlags.getRaw());
		buffer.putFloat(this.pos.getX());
		buffer.putFloat(this.pos.getY());
		buffer.put(this.ring);
		buffer.put(this.slot);

		return buffer;
	}

	public int getVersion()
	{
		return 0;
	}

	public ZonePlayerStatusFlags getStatusFlags()
	{
		return statusFlags;
	}

	public void setStatusFlags(ZonePlayerStatusFlags flags)
	{
		this.statusFlags = flags;
	}

	public void setPos(BoardCoord bc)
	{
		this.pos = bc;
	}

	public BoardCoord getPos()
	{
		return this.pos;
	}

	public int getSlot()
	{
		return slot;
	}

	public void setSlot(int slot)
	{
		this.slot = slot;
	}

	public void setPlayerIndex(int playerIndex)
	{
		this.playerIndex = playerIndex;
	}

	public void setRing(int ring)
	{
		this.ring = ring;
	}

	private int playerIndex;

	private ZonePlayerStatusFlags statusFlags;

	private BoardCoord pos;

	private int ring, slot;
}
