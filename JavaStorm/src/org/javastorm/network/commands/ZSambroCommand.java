package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

// RAWR!
public class ZSambroCommand extends NSCommand
{

	public String getCommand()
	{
		return this.command;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(4 + this.command.length() + 1);

		buffer.putInt(this.command.length() + 1);
		buffer.putStr(this.command);
		buffer.put(0);

		return buffer;
	}

	public int getCommandID()
	{
		return Connection.ZSambroCommand;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{

	}

	private String command;
}
