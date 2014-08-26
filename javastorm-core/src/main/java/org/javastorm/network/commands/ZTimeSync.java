package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZTimeSync extends NSCommand
{
	public double getRecvClient()
	{
		return recvClient;
	}

	public void setRecvClient(double recvClient)
	{
		this.recvClient = recvClient;
	}

	public double getRecvServer()
	{
		return recvServer;
	}

	public void setRecvServer(double recvServer)
	{
		this.recvServer = recvServer;
	}

	public double getSentClient()
	{
		return sentClient;
	}

	public void setSentClient(double sentClient)
	{
		this.sentClient = sentClient;
	}

	public int getCommandID()
	{
		return Connection.ZTimeSync;
	}

	@Override
	public String[] outputDebug() {
		return new String[] {
			"sentClient = " + this.sentClient,
			"recvServer = " + this.recvServer,
			"recvClient = " + this.recvClient
		};
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(4);

		this.sentClient = buffer.getDouble();
		this.recvServer = buffer.getDouble();
		this.recvClient = buffer.getDouble();
	}

	private double sentClient;

	private double recvServer;

	private double recvClient;

	@Override
	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(28);

		buffer.putInt(0);
		buffer.putDouble(this.sentClient);
		buffer.putDouble(this.recvServer);
		buffer.putDouble(this.recvClient);

		return buffer;
	}

	@Override
	public int getVersion()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
