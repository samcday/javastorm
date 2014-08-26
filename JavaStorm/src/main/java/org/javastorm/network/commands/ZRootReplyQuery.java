package org.javastorm.network.commands;

import org.javastorm.challenge.Zone;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZRootReplyQuery extends NSCommand
{
	public int get_flags()
	{
		return _flags;
	}

	public void set_flags(int _flags)
	{
		this._flags = _flags;
	}

	public void setZones(Zone[] zones)
	{
		this.zones = zones;
	}

	public Zone[] getZones()
	{
		return this.zones;
	}

	public int getNumFriendsInChal()
	{
		return numFriendsInChal;
	}

	public void setNumFriendsInChal(int numFriendsInChal)
	{
		this.numFriendsInChal = numFriendsInChal;
	}

	public int getRootListChecksum()
	{
		return rootListChecksum;
	}

	public void setRootListChecksum(int rootListChecksum)
	{
		this.rootListChecksum = rootListChecksum;
	}

	public String getRootServer()
	{
		return this.rootServer;
	}

	public void setRootServer(String rootServer)
	{
		this.rootServer = rootServer;
	}

	public int getServerMajorVersion()
	{
		return serverMajorVersion;
	}

	public void setServerMajorVersion(int serverMajorVersion)
	{
		this.serverMajorVersion = serverMajorVersion;
	}

	public int getServerMinorVersion()
	{
		return serverMinorVersion;
	}

	public void setServerMinorVersion(int serverMinorVersion)
	{
		this.serverMinorVersion = serverMinorVersion;
	}

	public int getZone()
	{
		return zone;
	}

	public void setZone(int zone)
	{
		this.zone = zone;
	}

	public int getCommandID()
	{
		return Connection.ZRootReplyQuery;
	}

	public int getVersion()
	{
		return 0;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		int numChals;

		buffer.setPosition(0);

		this.serverMajorVersion = buffer.getShort();
		this.serverMinorVersion = buffer.getShort();

		this._flags = buffer.get();
		this.zone = buffer.get();
		numChals = buffer.get();
		this.numFriendsInChal = buffer.get();
		int rootServerLen = buffer.getInt();
		this.rootListChecksum = buffer.getInt();

		if (rootServerLen > 0)
			this.rootServer = buffer.getString(rootServerLen);

		this.zones = new Zone[numChals];
		for (int i = 0; i < numChals; i++)
		{
			Zone zone = new Zone(buffer.get());
			zone.setNumPlayers(buffer.get());
			zone.setAvgLevel(buffer.get());
			zone.setInfo(buffer.getPaddedString(40));
			zone.setFlag(buffer.getInt());

			this.zones[i] = zone;
		}

		for (int i = 0; i < numChals; i++)
		{/*
					int port;
					int linkPort;
					String server;
					
					port = buffer.getInt();
					linkPort = buffer.get();
					server = buffer.getPaddedString(40);*/
		}
	}

	public MyByteBuffer getCommandData()
	{
		int rootServerLen = 0;
		if (this.rootServer != null)
			rootServerLen = this.rootServer.length();

		MyByteBuffer buffer = new MyByteBuffer();

		buffer.allocate(16 + (this.zones.length * 47) + (this.zones.length * 45));
		buffer.setPosition(0);

		buffer.putShort(this.serverMajorVersion);
		buffer.putShort(this.serverMinorVersion);

		buffer.put(this._flags);
		buffer.put(this.zone);
		buffer.put(this.zones.length);
		buffer.put(this.numFriendsInChal);
		buffer.putInt(rootServerLen);
		buffer.putInt(this.rootListChecksum);
		buffer.putStr(this.rootServer);

		Zone zone;
		for (int i = 0; i < this.zones.length; i++)
		{
			zone = this.zones[i];
			buffer.put(zone.getId());
			buffer.put(zone.getNumPlayers());
			buffer.put(zone.getAvgLevel());
			buffer.putStr(zone.getInfo(), 40);
			buffer.putInt(zone.getFlag());
		}

		for (int i = 0; i < this.zones.length; i++)
		{
			buffer.putInt(0);
			buffer.put(0);
			buffer.putStr("", 40);
		}

		return buffer;
	}

	private Zone[] zones;

	private int serverMajorVersion;

	private int serverMinorVersion;

	private int _flags;

	private int zone;

	private int numFriendsInChal;

	private String rootServer;

	private int rootListChecksum;
}
