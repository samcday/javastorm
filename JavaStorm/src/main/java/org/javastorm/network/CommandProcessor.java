package org.javastorm.network;

import org.javastorm.network.commands.NSCommand;

// A simple class that pumps incoming NS Commands out to listeners via a thread.
public class CommandProcessor implements Runnable
{
	public void start(Connection connection, CommandListener listener)
	{
		this.listener = listener;
		this.connection = connection;
		this.thread = new Thread(this, "NS Command Processor");
		this.thread.start();
	}

	public void stop()
	{
		if (this.thread != null)
			this.thread.interrupt();
		this.thread = null;
	}

	public void run()
	{
		NSCommand command = null;

		while ((this.thread != null) && connection.connected())
		{
			// Get a command from head of list.
			command = connection.readCommand(true);

			if (command != null)
			{
				try
				{
					listener.onCommand(command);
				}
				catch (Throwable t)
				{
					listener.onException(t);
				}
			}
		}

		if (connection != null)
			connection.disconnect();
		connection = null;

		listener.onConnectionDead();
	}

	private Connection connection;

	private CommandListener listener;

	private Thread thread;
}
