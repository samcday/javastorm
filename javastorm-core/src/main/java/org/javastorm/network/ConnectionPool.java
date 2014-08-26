package org.javastorm.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

// This class basically spins in it's own thread, waiting for connections and accepting them.
// Since there can be multiple battles going on at any one time, this is a singleton. Active NSBattleServers
// will get their Socket from here, based on challenge ID.

// In a nutshell this object will accept all incoming connections on specified port, and then pass these connections
// on to whoever asks for them.
public class ConnectionPool
{
	private ConnectionPool()
	{
		this.listeners = new ConnectionListener[MAX_LISTENERS];
		this.pendingConnections = new Vector<IncomingConnection>();
		this.scls = new ServerConnectionListener[256];
	}

	public static ConnectionPool get()
	{
		if (ConnectionPool.singleton == null)
			ConnectionPool.singleton = new ConnectionPool();

		return ConnectionPool.singleton;
	}

	public void listen(int port)
	{
		System.out.println("Listening on port #" + port);

		ServerConnectionListener cl = new ServerConnectionListener();

		cl.port = port;
		cl.pool = this;

		try
		{
			// We want a nonblocking server socket for easy thread exit.
			cl.serverSocket = new ServerSocket(cl.port);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return;
		}

		new Thread(cl, "Battle Connection Listener Thread").start();

		for (int i = 0; i < this.scls.length; i++)
		{
			if (this.scls[i] == null)
			{
				this.scls[i] = cl;
				break;
			}
		}
	}

	// Shuts the listener down.
	public void shutdown()
	{
		// We'll let the cleanup be done by the thread once it's exited it's loop.
		try
		{
			for (ServerConnectionListener l : this.scls)
			{
				if (l != null)
					l.serverSocket.close();
			}
		}
		catch (IOException ioe)
		{
		}
	}

	// A successful connection by a client with a subID.
	public void notifyConnection(IncomingConnection connection)
	{
		if (connection.getConnection() == null)
			return;

		int subscriberID = connection.getSubscriberID();

		for (int i = 0; i < this.listeners.length; i++)
		{
			if (this.listeners[i] != null)
			{
				if (this.listeners[i].getSubscriberID() == subscriberID)
				{
					this.listeners[i].connection(connection.getConnection());
					return;
				}
			}
		}

		// If we couldn't find the listener, there's a chance that somehow the client connected to us before we were ready.
		// This is taken care of by the pending connection list. We add the connection here now, and next time a listener is attached
		// we check to see if there is already a connection available for that listener.
		this.pendingConnections.add(connection);
	}

	// Attaches a listener to be fired when a connection is made from specified IP.
	public void attachListener(ConnectionListener listener)
	{
		// First, add this new listener to our list.
		for (int i = 0; i < this.listeners.length; i++)
		{
			if (this.listeners[i] == null)
			{
				this.listeners[i] = listener;
				break;
			}
		}

		// Now we scan through the pending connections and feed any through if relevant.
		Object[] pendConns = this.pendingConnections.toArray();
		for (Object pendConnObj : pendConns)
		{
			IncomingConnection pendConn = (IncomingConnection) pendConnObj;

			if (pendConn.getSubscriberID() == listener.getSubscriberID())
			{
				// Remove the pending connection from list and fire it off to the listener.
				this.pendingConnections.remove(pendConn);
				listener.connection(pendConn.getConnection());
			}
		}
	}

	// Attaches a listener to be fired when a connection is made from specified IP.
	public void detachListener(ConnectionListener listener)
	{
		// First, add this new listener to our list.
		for (int i = 0; i < this.listeners.length; i++)
		{
			if (this.listeners[i] == listener)
			{
				this.listeners[i] = null;
				return;
			}
		}
	}

	private class ServerConnectionListener implements Runnable
	{
		public void run()
		{
			Socket socket;
			System.out.println("ServerConnectionListener running on port " + this.port + " started.");

			while (!this.serverSocket.isClosed())
			{
				socket = null;
				try
				{
					// Any new connections?
					socket = this.serverSocket.accept();
				}
				catch (IOException ioe)
				{
				}

				if (socket == null)
					continue;

				new IncomingConnection(socket, pool);

				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException ie)
				{
				}
			}

			System.out.println("ServerConnectionListener running on port " + this.port + " closed.");
			// We've shutdown.
			try
			{
				this.serverSocket.close();
			}
			catch (IOException ioe)
			{
			}
		}

		private int port;

		private ConnectionPool pool;

		private ServerSocket serverSocket;
	}

	private ServerConnectionListener[] scls;

	private Vector<IncomingConnection> pendingConnections;

	private static ConnectionPool singleton;

	private ConnectionListener listeners[];

	private static final int MAX_LISTENERS = 255;
}
