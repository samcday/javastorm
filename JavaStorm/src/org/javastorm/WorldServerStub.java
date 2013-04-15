package org.javastorm;

import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZCompressedUpdateSquid;

public class WorldServerStub
	implements WorldServer
{
	@Override
	public void sendTo(int playerId, NSCommand cmd) {
		System.err.println(cmd.toString() + " sending to " + playerId);
		
	}
	
	@Override
	public void updateSquid(ZCompressedUpdateSquid zcus) {
		System.err.println(zcus + " update.");
	}
	
	@Override
	public void sendToAll(NSCommand cmd) {
		System.err.println(cmd.toString() + " sending to all");
	}
}
