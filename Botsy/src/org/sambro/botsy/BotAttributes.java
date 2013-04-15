package org.sambro.botsy;

import org.javastorm.Version;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.fort.FortData;
import org.javastorm.util.MyByteBuffer;

// Some extended attributes we need for Botsy in zones.
public class BotAttributes extends ZonePlayer
{
	public BotAttributes()
	{
		super(new Version(10, 79));
		
		this.setCompatibleMinorVersion(78);

		// Open our fort file and interpret data.
		FortData fd = new FortData(null);
		fd.open("botsy.fort");
		this.setNickname(fd.interpretNickname());
		this.setSubscriberID(fd.interpretSubscriberID());
		this.setCoreData(fd.interpretCoreData());
		this.setTerrain(fd.interpretTerritory().getTerritory(0));
		this.fortData = fd.createStrippedImage();

		this.setChatFormat("~E~B~[C.120]%s> ~j");
		this.setWhisperFormat("~E~B~[C.120]%s (to %s)> ~[C.120]");
	}

	public MyByteBuffer getFortData()
	{
		return fortData;
	}

	public void setWhisperFormat(String whisperFormat)
	{
		this.whisperFormat = whisperFormat;
	}

	public String getWhisperFormat()
	{
		return whisperFormat;
	}

	public void setChatFormat(String chatFormat)
	{
		this.chatFormat = chatFormat;
	}

	public String getChatFormat()
	{
		return chatFormat;
	}

	private String chatFormat;

	private String whisperFormat;

	private MyByteBuffer fortData;
}
