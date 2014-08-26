package org.javastorm.challenge;

import org.javastorm.BoardCoord;
import org.javastorm.Player;
import org.javastorm.PlayerCoreData;
import org.javastorm.Version;
import org.javastorm.network.commands.ZChalPlayerUpdate;
import org.javastorm.territory.Territory;

//Stores attributes of a NS Player in zones.
public class ZonePlayer extends Player
{
	public ZonePlayer(Version version)
	{
		super(version);
		
		this.pos = new BoardCoord();
		this.terrain = new Territory();
		this.coreData = new PlayerCoreData();
		this.statusFlags = new ZonePlayerStatusFlags();
	}

	public int getForceChase()
	{
		return forceChase;
	}

	public void setForceChase(int forceChase)
	{
		this.forceChase = forceChase;
	}

	public ZonePlayerStatusFlags getStatusFlags()
	{
		return this.statusFlags;
	}

	public int getCompatibleMinorVersion()
	{
		return compatibleMinorVersion;
	}

	public void setCompatibleMinorVersion(int compatibleMinorVersion)
	{
		this.compatibleMinorVersion = compatibleMinorVersion;
	}

	public PlayerCoreData getCoreData()
	{
		return coreData;
	}

	public void setCoreData(PlayerCoreData coreData)
	{
		this.coreData = coreData;
	}

	public int getFlags()
	{
		return flags;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public int getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(int languageId)
	{
		this.languageId = languageId;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}

	public Territory getTerrain()
	{
		return terrain;
	}

	public void setTerrain(Territory terrain)
	{
		this.terrain = terrain;
	}
	
	public BoardCoord getPos()
	{
		return this.pos;
	}

	public void setRing(int ring)
	{
		this.ring = ring;
	}

	public int getRing()
	{
		return ring;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public String getFormat()
	{
		return format;
	}

	public void setToldStfu(boolean toldStfu)
	{
		this.toldStfu = toldStfu;
	}

	public boolean isToldStfu()
	{
		return toldStfu;
	}

	public void setMessageCount(int messageCount)
	{
		this.messageCount = messageCount;
	}

	public int getMessageCount()
	{
		return messageCount;
	}

	public void setLastMessage(long lastMessage)
	{
		this.lastMessage = lastMessage;
	}

	public long getLastMessage()
	{
		return lastMessage;
	}

	public void setSlot(int slot)
	{
		//System.err.println("NSZonePlayer: " + this.getNickname() + " slot changed to " + slot);
		this.slot = slot;
	}

	public int getSlot()
	{
		return slot;
	}

	public ZChalPlayerUpdate createUpdate()
	{
		ZChalPlayerUpdate zcpu = new ZChalPlayerUpdate();
		zcpu.setPlayerIndex(this.getPlayerIndex());
		zcpu.setPos(this.pos);
		zcpu.setRing(this.ring);
		zcpu.setSlot(this.slot);
		zcpu.setStatusFlags(this.statusFlags);
		return zcpu;
	}

	private int compatibleMinorVersion;

	private int languageId;

	private int flags;

	private Territory terrain;

	private PlayerCoreData coreData;

	private int statusCode;

	private ZonePlayerStatusFlags statusFlags;

	private BoardCoord pos; // Co-ordinates of the player in zone.

	private int ring; // What ring the player is on.

	private int slot; // What slot on the ring the player is in.

	private String format;

	private boolean toldStfu = false;

	// This is for antispam.
	private int messageCount; // If more than 3 messages are sent before timeout,

	private long lastMessage; // ignore the player.

	private int forceChase;
}
