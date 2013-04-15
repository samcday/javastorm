package org.sambro.botsy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.ChallengeClient;
import org.javastorm.network.ChallengeListener;

// All bots subclass this to get basic zone functionality.
public class BaseBotsy extends ChallengeListener
{
	public BaseBotsy()
	{
		this.botinfo = new BotAttributes();
		this.challengeServer = new ChallengeClient(this);

		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.mysql = DriverManager.getConnection("jdbc:mysql://127.0.0.1/botsy?user=root");
		}
		catch (SQLException ex)
		{
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			//System.exit(-1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(-1);
		}
	}

	public boolean connect(String host, int port)
	{
		//this.botinfo.getTerrain().setCanon(37);
		//this.botinfo.getTerrain().setRandSeed(24);
		return this.challengeServer.connect(this.botinfo, host, port);
	}

	public boolean connected()
	{
		return this.challengeServer.connected();
	}

	public void disconnect()
	{
		this.challengeServer.disconnect();
	}

	public ZonePlayer getMe()
	{
		return this.challengeServer.getMe();
	}

	protected boolean isAdmin(int subscriberID)
	{
		Statement statement = null;
		ResultSet result = null;

		try
		{
			statement = this.mysql.createStatement();
			result = statement.executeQuery("SELECT * FROM admins WHERE SubscriberID=" + subscriberID);

			if (result.first())
				return true;
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return false;
	}

	protected boolean ignored(long subscriberID)
	{
		for (int i = 0; i < this.permaIgnore.length; i++)
		{
			if (this.permaIgnore[i] == subscriberID)
				return true;
		}

		for (int i = 0; i < this.ignoreList.length; i++)
			if (this.ignoreList[i] == subscriberID)
			{
				if ((this.ignoreTime[i] + 600000) < System.currentTimeMillis())
				{
					this.ignoreTime[i] = 0;
					this.ignoreList[i] = 0;
					return false;
				}

				return true;
			}

		return false;
	}

	protected void addIgnore(int subscriberID)
	{
		if (this.isAdmin(subscriberID))
			return;

		for (int i = 0; i < this.ignoreList.length; i++)
		{
			if (this.ignoreList[i] == 0)
			{
				this.ignoreList[i] = subscriberID;
				this.ignoreTime[i] = System.currentTimeMillis();
				ZonePlayer player = this.challengeServer.findPlayerBySubscriberID(this.ignoreList[i]);
				if (player != null)
					player.setToldStfu(false);
			}
		}
	}

	// Commands bot to whisper a supplied subscriberID
	public void whisper(String sayText, int subscriberID)
	{
		ZonePlayer dest = this.challengeServer.findPlayerBySubscriberID(subscriberID);
		this.challengeServer.whisper("~[I93.111]" + String.format(this.botinfo.getWhisperFormat(), this.botinfo.getNickname(), dest.getNickname()) + sayText, subscriberID);
	}

	public void whisperNoFormat(String sayText, int subscriberID)
	{
		this.challengeServer.whisper(sayText, subscriberID);
	}

	//	Commands bot to speak in zone.
	public void say(String sayText)
	{
		if (!this.talk)
			return;

		this.challengeServer.say("~[I93.11]" + String.format(this.botinfo.getChatFormat(), this.botinfo.getNickname()) + sayText);
	}

	//	Commands bot to speak in zone.
	public void sayNoFormat(String sayText)
	{
		if (!this.talk)
			return;

		this.challengeServer.say(sayText);
	}

	// Called when a player speaketh.
	protected boolean processChat(ZonePlayer player, String msg)
	{
		int subscriberID = player.getSubscriberID();

		if (subscriberID == this.botinfo.getSubscriberID())
			return false;

		if (this.ignored(player.getSubscriberID()))
		{
			if (!player.isToldStfu())
			{
				this.say("Sorry " + player.getNickname() + ". You're on my ignore for being a toolie :)");
				player.setToldStfu(true);
			}
			return false;
		}

		if (player.getLastMessage() == 0)
			player.setLastMessage(System.currentTimeMillis());

		if ((player.getLastMessage() + 3000) < System.currentTimeMillis())
		{
			player.setMessageCount(0);
			player.setLastMessage(System.currentTimeMillis());
		}
		else
		{
			player.setMessageCount(player.getMessageCount() + 1);
		}

		if (player.getMessageCount() == 3)
		{
			this.addIgnore(player.getSubscriberID());
			return false;
		}

		return true;
	}

	protected Connection mysql;

	protected ChallengeClient challengeServer;

	protected int[] ignoreList = new int[255];

	protected long[] ignoreTime = new long[255];

	public BotAttributes botinfo; // Info about our Bot. Nickname, ID etc.

	protected int[] permaIgnore = {};

	protected boolean talk = true;
}
