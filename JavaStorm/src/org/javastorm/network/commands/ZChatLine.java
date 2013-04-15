package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

//NSClientCommand implementation to speak.
public class ZChatLine extends NSCommand
{
	public ZChatLine()
	{
		this.destSubscriberIDs = new int[255];
		this.destSubscriberIDsCount = 0;

		for (int i = 0; i < this.destSubscriberIDs.length; i++)
			this.destSubscriberIDs[i] = -1;

		this.nickname = "Default";
		this.message = "Default message.";
		this.subscriberID = 1;

		this.messageLineBreak = true;
		this.messageNullTerm = true;
	}

	public String getNickname()
	{
		return this.nickname;
	}

	public String getMessage()
	{
		return this.message;
	}

	public int getSubscriberID()
	{
		return this.subscriberID;
	}

	public void setMessageLineBreak(boolean lineBreak)
	{
		this.messageLineBreak = lineBreak;
	}

	public void setMessageNullTerm(boolean nullTerm)
	{
		this.messageNullTerm = nullTerm;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;

		if (this.nickname == null)
		{
			this.nickname = new String("");
		}
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	public void addDestinationSubscriberID(int subscriberID)
	{
		this.destSubscriberIDs[destSubscriberIDsCount++] = subscriberID;
	}

	public void clearDestinationSubscriberIDs()
	{
		this.destSubscriberIDs = new int[255];
		this.destSubscriberIDsCount = 0;
	}

	public int[] getDestinationSubscriberIDs()
	{
		return this.destSubscriberIDs;
	}

	public boolean isServerMessage()
	{
		return this.isServerMessage;
	}

	// Returns this packets' command id.
	public int getCommandID()
	{
		return Connection.ZChatLine;
	}

	public int getVersion()
	{
		return 3;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		int messageOffset;

		this.version = buffer.get();
		this.encoding = buffer.get();

		// subscriberID of player that spoke.
		this.subscriberID = buffer.getInt();

		// Length of nickname including null-term.
		int nicknameLen = buffer.getShort();

		// Nickname.
		int msgLen = buffer.getShort();
		int addrLen = buffer.get();

		this.nickname = buffer.getString(nicknameLen);

		// Message.
		messageOffset = buffer.position();
		/*for(int i = 0; i < msgLen; i++)
			System.out.print((char)buffer.get());
		buffer.setPosition(messageOffset);*/

		this.message = buffer.getString(msgLen);
		buffer.setPosition(messageOffset + msgLen - 2);

		if (this.subscriberID > 0)
		{
			this.messageLineBreak = false;
			this.messageNullTerm = false;

			int test = buffer.get();
			if (test == 0x0A)
			{
				this.messageLineBreak = true;
				this.message = this.message.substring(0, this.message.length() - 1);
			}

			test = buffer.get();
			if (test == 0)
			{
				this.messageNullTerm = true;
			}
			else if (test == 0x0A)
			{
				this.messageLineBreak = true;
				this.message = this.message.substring(0, this.message.length() - 1);
			}

			this.destSubscriberIDs = new int[addrLen];
			for (int i = 0; i < addrLen; i++)
			{
				this.destSubscriberIDs[i] = buffer.getInt();
			}
		}
		else
		{

			this.isServerMessage = true;
		}
	}

	// Returns this packet in raw byte data.
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer data = new MyByteBuffer();
		int messageExtra = 0;
		int length = 12 + (this.destSubscriberIDsCount * 4) + this.nickname.length() + this.message.length();

		if (this.messageLineBreak)
			messageExtra++;
		if (this.messageNullTerm)
			messageExtra++;

		length += messageExtra;

		data.allocate(length);
		data.setPosition(0);

		data.put(this.version); // _version
		data.put(this.encoding); // encoding

		// Challenge ID of sender.
		data.putInt(this.subscriberID);

		// Length of nickname.
		data.putShort(this.nickname.length() + 1);

		// Length of message.
		data.putShort(this.message.length() + messageExtra);

		// Number of subscriberIDs we're sending to.
		data.put(this.destSubscriberIDsCount);

		// Nickname. With null term.
		data.putStr(this.nickname);
		data.put(0);

		// Message with LF null term.
		data.putStr(this.message);

		if (this.messageLineBreak)
			data.put(0x0A);

		if (this.messageNullTerm)
			data.put(0);

		// List of subscriberIDs to receive this message.
		for (int i = 0; i < this.destSubscriberIDsCount; i++)
		{
			data.putInt(this.destSubscriberIDs[i]);
		}

		return data;
	}

	public int getEncoding()
	{
		return encoding;
	}

	public void setEncoding(int encoding)
	{
		this.encoding = encoding;
	}

	private int version = 255;

	private int encoding = CHAT_ASCII;

	// The challenge ID of who's speakin'.
	private int subscriberID;

	// The nickname of speaker.
	private String nickname;

	// The message.
	private String message;

	// Array of destination subscriberIDs.
	private int[] destSubscriberIDs;

	private int destSubscriberIDsCount;

	private boolean messageLineBreak;

	private boolean messageNullTerm;

	private boolean isServerMessage;

	public static final int CHAT_ASCII = 0x01;

	public static final int CHAT_POPUP = 0x80;

	public static final int CHAT_BROADCAST = 0x00;
}