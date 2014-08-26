package org.javastorm;

public class Player
{
	public Player(Version version)
	{
		this.version = version;
	}

	public Player(Player otherPlayer)
	{
		this(otherPlayer.getVersion());

		this.subscriberID = otherPlayer.getSubscriberID();
		this.ip = otherPlayer.getIp();
		this.playerIndex = otherPlayer.getPlayerIndex();
		this.nickname = otherPlayer.getNickname();
	}

	public Version getVersion()
	{
		return this.version;
	}

	public int getSubscriberID()
	{
		return this.subscriberID;
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	public int[] getIp()
	{
		return this.ip;
	}

	public int getIp(int index)
	{
		return this.ip[index];
	}

	public void setIp(int[] ip)
	{
		this.ip = ip;
	}

	public String getIpString()
	{
		return this.ip[0] + "." + this.ip[1] + "." + this.ip[2] + "." + this.ip[3];
	}

	public String getNickname()
	{
		return this.nickname;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public int getPlayerIndex()
	{
		return playerIndex;
	}

	public void setPlayerIndex(int index)
	{
		this.playerIndex = index;
	}

	private String nickname = ""; // Player's nickname.

	private int subscriberID = 0; // Players challenge ID.

	private int[] ip; // Player's intarweb address.

	private int playerIndex; // Player's pindex.

	private Version version;
}
