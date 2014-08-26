package org.javastorm.network;

// This is used for the NSConnectionPool.
public interface ConnectionListener
{
	public void connection(Connection conn); // Called when a connection is made.

	public int getSubscriberID(); // What subscriber ID are we listening for?
}
