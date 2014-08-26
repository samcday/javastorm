package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.territory.Territory;
import org.javastorm.util.MyByteBuffer;

public class ZChalLaunchServer extends NSCommand
{
	public Territory getBountyTerr()
	{
		return bountyTerr;
	}

	public void setBountyTerr(Territory bountyTerr)
	{
		this.bountyTerr = bountyTerr;
	}

	public ZAClient[] getClients()
	{
		return clients;
	}

	public void setClients(ZAClient[] clients)
	{
		this.clients = clients;
	}

	public int getGameID()
	{
		return gameID;
	}

	public void setGameID(int gameID)
	{
		this.gameID = gameID;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(11 + (71 * this.clients.length));

		this.bountyTerr.write(buffer);

		buffer.putInt(this.gameID);
		buffer.put(this.clients.length);

		for (int i = 0; i < this.clients.length; i++)
		{
			buffer.put(this.clients[i].getPlayerId());
			buffer.putInt(this.clients[i].getSubscriberId());
			buffer.putInt(this.clients[i].get_flags());
			buffer.putStr(this.clients[i].getName(), 32);
			buffer.putStr(this.clients[i].getAddress(), 30);
		}

		return buffer;
	}

	public int getCommandID()
	{
		return Connection.ZChalLaunchServer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.bountyTerr = new Territory();
		this.bountyTerr.read(buffer);

		this.gameID = buffer.getInt();

		int clientCount = buffer.get();
		this.clients = new ZAClient[clientCount];

		for (int i = 0; i < clientCount; i++)
		{
			// TODO:
			this.clients[i] = new ZAClient();
			this.clients[i].setPlayerId(buffer.get());
			this.clients[i].setSubscriberId(buffer.getInt());
			this.clients[i].set_flags(buffer.getInt());
			this.clients[i].setName(buffer.getPaddedString(32));
			this.clients[i].setAddress(buffer.getPaddedString(30));
		}
	}

	public static final class ZAClient
	{
		public int getPlayerId()
		{
			return playerId;
		}

		public void setPlayerId(int playerId)
		{
			this.playerId = playerId;
		}

		public int getSubscriberId()
		{
			return subscriberId;
		}

		public void setSubscriberId(int subscriberId)
		{
			this.subscriberId = subscriberId;
		}

		public int get_flags()
		{
			return _flags;
		}

		public void set_flags(int _flags)
		{
			this._flags = _flags;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getAddress()
		{
			return address;
		}

		public void setAddress(String address)
		{
			this.address = address;
		}

		private int playerId;

		private int subscriberId;

		private int _flags;

		private String name;

		private String address;
	}

	private Territory bountyTerr;

	private int gameID;

	private ZAClient[] clients;
}
