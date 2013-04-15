package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

//NSPacket implementation to authenticate at battle start.
public class ZRequestLogin extends NSCommand
{
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

	public MyByteBuffer getFortData()
	{
		return fortData;
	}

	public String getNickname()
	{
		return nickname;
	}

	public int getSubscriberID()
	{
		return subscriberID;
	}

	public ZRequestLogin()
	{
		this.subscriberID = 0;
		this.nickname = "Default";
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	public void setFortData(MyByteBuffer data)
	{
		this.fortData = data;
	}

	// Returns this packets' command id.
	public int getCommandID()
	{
		return Connection.ZRequestLogin;
	}

	public int getVersion()
	{
		return 1;
	}

	
	@Override
	public String[] outputDebug() {
		return new String[] {
			"clientMajorVersion = " + this.clientMajorVersion,
			"clientMinorVersion = " + this.clientMinorVersion,
			"subscriberID = " + this.subscriberID,
			"nickname = " + this.nickname,
			"fortData = TODO"
		};
	}
	
	// Returns this packet in raw byte data.
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer data = new MyByteBuffer();
		data.allocate(this.nickname.length() + this.fortData.size() + 14);

		data.putShort(this.clientMajorVersion);
		data.putShort(this.clientMinorVersion);

		data.putInt(this.subscriberID);

		data.put(this.nickname.length() + 1);
		data.putInt(this.fortData.size());

		data.putStr(this.nickname);
		data.put(0);

		this.fortData.setPosition(0);
		data.put(this.fortData);

		return data;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.clientMajorVersion = buffer.getShort();
		this.clientMinorVersion = buffer.getShort();

		this.subscriberID = buffer.getInt();

		buffer.get();
		int fortDataLength = buffer.getInt();

		this.nickname = buffer.getString();

		this.fortData = new MyByteBuffer();
		this.fortData.allocate(fortDataLength);
		this.fortData.put(buffer, fortDataLength);
	}

	// Challenge ID of player joining battle.
	private int subscriberID;

	private int clientMajorVersion = 0xFFFF, clientMinorVersion = 0xFFFF;

	// Nickname of player.
	private String nickname;

	// Fort data of player.
	private MyByteBuffer fortData;

}