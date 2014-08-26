package org.javastorm.network.commands;

import org.javastorm.PlayerCoreData;
import org.javastorm.challenge.ZonePlayerStatusFlags;
import org.javastorm.network.Connection;
import org.javastorm.territory.Territory;
import org.javastorm.util.MyByteBuffer;

public class ZChalRequestLogin extends NSCommand
{
	public int getPlayerLevel()
	{
		return playerLevel;
	}

	public void setPlayerLevel(int playerLevel)
	{
		this.playerLevel = playerLevel;
	}

	public Territory getTerr()
	{
		return terr;
	}

	public void setTerr(Territory terr)
	{
		this.terr = terr;
	}

	public PlayerCoreData getPlayerData()
	{
		return playerData;
	}

	public void setPlayerData(PlayerCoreData playerData)
	{
		this.playerData = playerData;
	}

	public ZChalRequestLogin()
	{
		this.statusFlags = new ZonePlayerStatusFlags();
		this.fd = -1;
	}

	public int get_flags()
	{
		return _flags;
	}

	public void set_flags(int _flags)
	{
		this._flags = _flags;
	}

	public int getSubscriberID()
	{
		return subscriberID;
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	public int getChalStateId()
	{
		return chalStateId;
	}

	public void setChalStateId(int chalStateId)
	{
		this.chalStateId = chalStateId;
	}

	public int getClientMajorVersion()
	{
		return clientMajorVersion;
	}

	public void setClientMajorVersion(int clientMajorVersion)
	{
		this.clientMajorVersion = clientMajorVersion;
	}

	public int getClientMinorVersion()
	{
		return clientMinorVersion;
	}

	public void setClientMinorVersion(int clientMinorVersion)
	{
		this.clientMinorVersion = clientMinorVersion;
	}

	public int getCompatibleMinorVersion()
	{
		return compatibleMinorVersion;
	}

	public void setCompatibleMinorVersion(int compatibleMinorVersion)
	{
		this.compatibleMinorVersion = compatibleMinorVersion;
	}

	public int getFd()
	{
		return fd;
	}

	public void setFd(int fd)
	{
		this.fd = fd;
	}

	public int getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(int languageId)
	{
		this.languageId = languageId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ZonePlayerStatusFlags getStatusFlags()
	{
		return this.statusFlags;
	}

	public int getCommandID()
	{
		return Connection.ZChalRequestLogin;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(42 + this.name.length() + 1);
		buffer.setPosition(0);

		// Client version.
		// Major.
		buffer.putShort(this.clientMajorVersion);
		// Minor.
		buffer.putShort(this.clientMinorVersion);

		// Compatible version (so our rings don't seem incompatible)
		buffer.putShort(this.compatibleMinorVersion);

		buffer.put(this.languageId);

		// Multiplayer ID
		buffer.putInt(this.subscriberID);

		// No clue.
		buffer.putInt(this._flags);

		// Zone to be joined.
		buffer.put(this.chalStateId);

		// Modifies the look of the island.
		this.terr.write(buffer);

		this.playerData.write(buffer);

		buffer.put(7);

		buffer.putInt(this.statusFlags.getRaw());
		//buffer.putInt(0);

		buffer.putInt(this.fd);

		// Len of nickname.
		buffer.put(this.name.length());

		// Nickname.
		buffer.putStr(this.name);

		// Null-term.
		buffer.put(0);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.clientMajorVersion = buffer.getShort();
		this.clientMinorVersion = buffer.getShort();
		this.compatibleMinorVersion = buffer.getShort();

		this.languageId = buffer.get();

		this.subscriberID = buffer.getInt();
		this._flags = buffer.getInt();

		this.chalStateId = buffer.get();

		this.terr = new Territory();
		this.terr.read(buffer);

		this.playerData = new PlayerCoreData();
		this.playerData.read(buffer);

		this.playerLevel = buffer.get();

		this.statusFlags = new ZonePlayerStatusFlags(buffer.getInt());
		this.fd = buffer.getInt();

		buffer.get();
		this.name = buffer.getString();
	}

	private int clientMajorVersion;

	private int clientMinorVersion;

	private int compatibleMinorVersion;

	private int languageId;

	private int subscriberID;

	private int _flags;

	private int chalStateId;

	private Territory terr;

	private PlayerCoreData playerData;

	private int playerLevel;

	private ZonePlayerStatusFlags statusFlags;

	private int fd;

	private String name;
}
