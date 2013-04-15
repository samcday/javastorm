package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZFortDataPacket extends NSCommand
{

	public MyByteBuffer getFortFile()
	{
		return fortFile;
	}

	public void setFortFile(MyByteBuffer fortFile)
	{
		this.fortFile = fortFile;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public int getPredictableCount()
	{
		return predictableCount;
	}

	public void setPredictableCount(int predictableCount)
	{
		this.predictableCount = predictableCount;
	}

	public int getWatcher()
	{
		return watcher;
	}

	public void setWatcher(boolean watcher)
	{
		this.watcher = watcher ? 1 : 0;
	}

	public int getCommandID()
	{
		return Connection.ZFortDataPacket;
	}

	@Override
	public String[] outputDebug() {
		return new String[] {
			"playerId = " + this.playerId,
			"predictableCount = " + this.predictableCount,
			"fortFileSize = " + this.fortFile.size(),
			"watcher = " + this.watcher,
		};
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(13 + this.fortFile.size());

		buffer.putInt(this.playerId);
		buffer.putInt(this.predictableCount);
		buffer.putInt(this.fortFile.size());
		buffer.put(this.watcher);

		this.fortFile.setPosition(0);
		buffer.put(this.fortFile);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		int fortFileSize;

		this.playerId = buffer.getInt();
		this.predictableCount = buffer.getInt();
		fortFileSize = buffer.getInt();
		this.watcher = buffer.get();

		this.fortFile = new MyByteBuffer();
		this.fortFile.allocate(fortFileSize);
		this.fortFile.put(buffer, fortFileSize);
	}

	private int playerId;

	private int predictableCount;

	private int watcher;

	private MyByteBuffer fortFile = new MyByteBuffer();
}
