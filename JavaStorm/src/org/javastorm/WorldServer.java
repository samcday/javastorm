package org.javastorm;

import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZCompressedUpdateSquid;

// Servers maintaining a NSWorld implement this to be informed of squid updates that need to be xmitted.
public interface WorldServer
{
	public void sendTo(int playerId, NSCommand cmd);
	public void sendToAll(NSCommand cmd);
	public void updateSquid(ZCompressedUpdateSquid zcus);
}
