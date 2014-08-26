package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZKeepAlive extends NSCommand
{
	public int getPing()
	{
		return ping;
	}

	public void setPing(int ping)
	{
		this.ping = ping;
	}

	public int getCommandID()
	{
		return Connection.ZKeepAlive;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.ping = buffer.get();
	}

	private int ping;

	@Override
	public String[] outputDebug() {
		return new String[] {
			"ping = " + this.ping
		};
	}

	@Override
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(1);
		buffer.put(this.ping);
		return buffer;
	}

	@Override
	public int getVersion()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
