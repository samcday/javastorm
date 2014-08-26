package org.javastorm.network;

import org.javastorm.network.commands.NSCommand;

public interface CommandListener
{
	public void onCommand(NSCommand command);

	public void onException(Throwable t);

	public void onConnectionDead();
}
