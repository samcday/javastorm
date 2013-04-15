package org.sambro.botsy;

import org.javastorm.challenge.ZonePlayer;

public class DummyBotsy extends BotsySpawn
{
	public DummyBotsy(int zone)
	{
		this.zone = zone;
	}

	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		super.spawn(id, master, masterPlayer);

		this.botinfo.setNickname("DummyBotsy" + id);
		this.botinfo.setSubscriberID(botinfo.getNickname().hashCode());

		return this.connect(master.challengeServer.getHost(), master.challengeServer.getPort());
	}

	public boolean connect(String host, int port)
	{
		//this.botinfo.getTerrain().setCanon(37);
		//this.botinfo.getTerrain().setRandSeed(24);
		return this.challengeServer.connect(this.botinfo, this.getMaster().challengeServer, this.zone);
	}

	private int zone;
}
