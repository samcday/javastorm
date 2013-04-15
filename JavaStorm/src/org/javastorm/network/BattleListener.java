package org.javastorm.network;

import org.javastorm.battle.BattlePlayer;
import org.javastorm.squids.MainSquid;

public class BattleListener
{
	// Called when successfully logged into battle.
	public void battleLoggedIn()
	{
	}

	// Called when all players have successfully logged in.
	public void battleStarted()
	{
	}

	// Called when the battle has ended.
	public void battleEnded()
	{
	}

	// Called when a player says something.
	public void battlePlayerSpeak(BattlePlayer player, String message)
	{
	}

	// Called when a player builds something.
	public void battlePlayerBuild(MainSquid s)
	{
	}
}
