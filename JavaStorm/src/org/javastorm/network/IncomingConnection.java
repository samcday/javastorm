package org.javastorm.network;

import java.net.Socket;

import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZPreconnect;

// Handles initial preconnect/reconnect from Socket. This will then pass the Socket onto
// whoever is listening for the challengeID on the other end.
public class IncomingConnection implements Runnable
{
	public IncomingConnection(Socket socket, ConnectionPool daddy)
	{
		this.daddy = daddy;

		this.connection = new Connection();
		this.connection.init(socket);

		new Thread(this, "Battle Connection Thread").start();
	}

	public void run()
	{
		System.out.println("Seckx.");

		// Block until we get a ZPreconnect.
		// TODO: Handle whatever is sent for reconnect!

		NSCommand command = this.connection.peekCommand(true);

		// No commands = bye bye.
		if (command == null)
		{
			this.connection.disconnect();
			return;
		}

		System.out.println("k?");

		if (command.getCommandID() == Connection.ZPreconnect)
		{
			ZPreconnect zp = (ZPreconnect) command;
			this.subscriberID = zp.getPlayerSubscriberID();

			// Tell daddy who's on the other end of this line.
			this.daddy.notifyConnection(this);
		}
		else
		{
			// No dice. Dunno wtf they're sending us?
			this.connection.disconnect();
			return;
		}
	}

	public int getSubscriberID()
	{
		return this.subscriberID;
	}

	public Connection getConnection()
	{
		return this.connection;
	}

	private Connection connection;

	private int subscriberID;

	private ConnectionPool daddy;
}