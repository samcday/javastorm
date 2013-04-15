package org.sambro.botsy;

import org.javastorm.World;
import org.javastorm.challenge.ZonePlayer;

// All spawnable Botsy's build off this.
public class BotsySpawn extends BaseBotsy
{
	// Spawns this bot, mimicing settings of master.
	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		this.master = master;
		this.masterPlayer = masterPlayer;
		this.botID = id;

		return true;
	}

	public boolean connect(String host, int port)
	{
		//this.botinfo.getTerrain().setCanon(37);
		//this.botinfo.getTerrain().setRandSeed(24);
		return this.challengeServer.connect(this.botinfo, this.master.challengeServer);
	}

	// Get the ID of this bot.
	public int getID()
	{
		return this.botID;
	}

	// Returns the Master bot of this spawn.
	public MasterBotsy getMaster()
	{
		return this.master;
	}

	public ZonePlayer getMasterPlayer()
	{
		return this.masterPlayer;
	}

	// Gets the battle object for this Bot.
	public World getBattle()
	{
		return null;
	}

	// Kills this bot. Can be called from Debug to force a bot death. Also should be called
	// when this botspawn is done. It does the cleanup and informs Master of death.
	public void kill()
	{
		this.master.notifyDead(this);
		super.disconnect();
	}

	protected void challengeDisconnected()
	{
		this.master.notifyDead(this);
	}

	protected void battleDone()
	{
		this.kill();
	}

	// Called when a player leaves zone for any reason.
	protected void challengePlayerLeave(ZonePlayer player)
	{
		if (player.getSubscriberID() == this.masterPlayer.getSubscriberID())
		{
			this.kill();
		}
	}

	private int botID;

	private MasterBotsy master;

	private ZonePlayer masterPlayer;
}
