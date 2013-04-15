package org.sambro.botsy;

import org.javastorm.challenge.ZonePlayer;

public class OrbitalBotsy extends BotsySpawn
{
	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		super.spawn(id, master, masterPlayer);

		this.botinfo.setNickname("OrbitalBotsy" + (id + 1));
		this.botinfo.setSubscriberID(botinfo.getNickname().hashCode());

		return this.connect(master.challengeServer.getHost(), master.challengeServer.getPort());
	}

	protected void challengeJoinZone()
	{
		//this.say("I feel your gravitational force, etc etc etc.");
		this.challengeServer.move(this.getMasterPlayer().getPos());
		this.orbitThread = new IslandOrbit();
		this.orbitThread.start(this, this.getMasterPlayer());
	}

	protected void challengePlayerMove(ZonePlayer player)
	{
		if (player.getSubscriberID() == this.getMasterPlayer().getSubscriberID())
		{
			this.orbitThread.stop();
			this.orbitThread.start(this, player);
		}
	}

	protected void challengeDisconnected()
	{
		super.challengeDisconnected();
		this.orbitThread.stop();
	}

	private IslandOrbit orbitThread;
}
