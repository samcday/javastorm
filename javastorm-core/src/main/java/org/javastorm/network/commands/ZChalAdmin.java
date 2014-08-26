package org.javastorm.network.commands;

import org.javastorm.BoardCoord;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZChalAdmin extends NSCommand
{
	public ZChalAdmin()
	{
		this.bcasting = "";
		this.password = "";
		this.bc = new BoardCoord();
	}

	public BoardCoord getBc()
	{
		return bc;
	}

	public void setBc(BoardCoord bc)
	{
		this.bc = bc;
	}

	public String getBcasting()
	{
		return bcasting;
	}

	public void setBcasting(String bcasting)
	{
		this.bcasting = bcasting;
	}

	public int getExtrainfo()
	{
		return extrainfo;
	}

	public void setExtrainfo(int extrainfo)
	{
		this.extrainfo = extrainfo;
	}

	public int getExtrainfo2()
	{
		return extrainfo2;
	}

	public void setExtrainfo2(int extrainfo2)
	{
		this.extrainfo2 = extrainfo2;
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public int getSailToBattle()
	{
		return sailToBattle;
	}

	public void setSailToBattle(int sailToBattle)
	{
		this.sailToBattle = sailToBattle;
	}

	public int getSailToBattleSlot()
	{
		return sailToBattleSlot;
	}

	public void setSailToBattleSlot(int sailToBattleSlot)
	{
		this.sailToBattleSlot = sailToBattleSlot;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(30 + this.password.length() + this.bcasting.length() + 1);
		buffer.putInt(this.password.length());
		buffer.putInt(this.bcasting.length() + 1);
		buffer.putInt(this.mode);
		buffer.putInt(this.extrainfo);
		buffer.putInt(this.extrainfo2);
		buffer.putBC(this.bc);
		buffer.put(this.sailToBattle);
		buffer.put(this.sailToBattleSlot);
		buffer.putStr(this.encodePassword(this.password));
		buffer.putStr(this.bcasting);
		buffer.put(0);

		return buffer;
	}

	public int getCommandID()
	{
		return Connection.ZChalAdmin;
	}

	public void readCommandData(MyByteBuffer buffer)
	{

	}

	// xor encodes the password when sending to server.
	private String encodePassword(String password)
	{
		StringBuilder builder = new StringBuilder(password.length());
		char[] passwordChars = password.toCharArray();
		char[] keyChars = this.key.toCharArray();
		int keyLength = this.key.length();
		int passwordLength = password.length();

		for (int i = 0; i < passwordLength; i++)
		{
			builder.append((char) (passwordChars[i] ^ keyChars[i % keyLength]));
		}

		return builder.toString();
	}

	public int getVersion()
	{
		return 2;
	}

	private int mode;

	private int extrainfo;

	private int extrainfo2;

	private String password;

	private String bcasting;

	private BoardCoord bc;

	private int sailToBattle;

	private int sailToBattleSlot;

	public static int ADMIN_KICK = 0; // were going to kick a player.

	public static int ADMIN_SHUTUP = 1; // were going to make a player not speak

	public static int ADMIN_RESET = 3; // were going to reset the server

	public static int ADMIN_BATTLEKICK = 4; // were going to kick someone off of a battle ring

	public static int ADMIN_BATTLEKICKALL = 5; // were going to kick everyone off a given ring becuase we can!

	public static int ADMIN_MOVEPLAYER = 6; // were going to move a selected player joy joy

	public static int ADMIN_BCAST = 7; // were going to send a bcast messgae

	public static int ADMIN_MCAST = 8; // were going to send a mcast messgae

	public static int ADMIN_SETADMIN = 9; // were going to set this player as a admin in the system

	public static int ADMIN_STARTBATTLE = 10; // were going to auto launch someone into battle

	public static int ADMIN_SETPLAYERTAG = 11; // were going to set someones player tag here

	public static int ADMIN_SETZONEPASS = 12; // were going to set someones player tag here

	// Password is xor encoded with this key.
	private String key = "QWERTYUI";
}
