package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZReadyToRock extends NSCommand
{
	public long getReady()
	{
		return ready;
	}

	public int getCommandID()
	{
		return Connection.ZReadyToRock;
	}

	public void setReady(long ready)
	{
		this.ready = ready;
	}

	@Override
	public String[] outputDebug() {
		return new String[] {
			"ready = " + this.ready
		};
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(4);
		buffer.putInt(this.ready);

		return buffer;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.ready = buffer.getInt();
	}

	public int getVersion()
	{
		return 1;
	}

	private long ready;
}
