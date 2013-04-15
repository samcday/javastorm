package org.javastorm.network;

import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.commands.ZChalLaunchServer.ZAClient;
import org.javastorm.territory.Territory;

// Subclass this to subscribe to events while in Challenge Server.
public abstract class ChallengeListener
{
	// These get overridden by subclasses to hook into zone events.
	// Called when a player makes a normal island move.
	protected void challengePlayerMove(ZonePlayer player)
	{
	}

	// Called when a player speaketh.
	protected void challengePlayerSpeak(ZonePlayer player, String message)
	{
	}

	// Called when a player first enters zone, this won't be called until we get the ZChalPlayerAddress zacket.
	// initial specifies whether this notify is part of the zone connect spam or if we're welcoming a new player.
	protected void challengePlayerJoin(ZonePlayer player, boolean initial)
	{
	}

	// Called when a player leaves zone for any reason.
	protected void challengePlayerLeave(ZonePlayer player)
	{
	}

	// Called when a player leaves zone in a direct disconnect.
	protected void challengePlayerDisconnect(ZonePlayer player)
	{
	}

	// Called when a player leaves zone for a battle.
	protected void challengePlayerBattle(ZonePlayer player)
	{
	}

	// Called when a player leaves zone for another zone.
	protected void challengePlayerChangeZone(ZonePlayer player)
	{
	}

	// Called when a player first joins a ring.
	protected void challengePlayerJoinRing(ZonePlayer player)
	{
	}

	// Called when a player changes spot in current ring.
	protected void challengePlayerChangeSlot(ZonePlayer player, int oldSlot)
	{
	}

	// Called when a player becomes Battlemaster.
	protected void challengePlayerBattleMaster(ZonePlayer player)
	{
	}

	// Called when a player leaves a ring.
	protected void challengePlayerLeaveRing(ZonePlayer player, int ring)
	{
	}

	// Called when a player gets kicked.
	protected void challengePlayerKicked(ZonePlayer player)
	{
	}

	protected void challengePlayerClickIn(ZonePlayer player)
	{
	}

	protected void challengePlayerClickOut(ZonePlayer player)
	{
	}

	// Called when a battle starts as client.
	protected void challengeLaunchClient(int gameID, String ip)
	{
	}

	protected void challengeLaunchServer(int gameID, Territory bountyTerr, ZAClient[] clients)
	{
	}

	// Called when disconnected from the server.
	protected void challengeDisconnected()
	{
	}

	// Called when the Bot has joined the zone. This will only be called when all player data and state has been
	// loaded for this zone.
	protected void challengeJoinZone()
	{
	}

	// Called when a server message is sent to client.
	protected void challengeServerMessage(String message)
	{
	}
}
