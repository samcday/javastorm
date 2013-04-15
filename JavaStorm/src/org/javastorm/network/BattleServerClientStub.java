package org.javastorm.network;

import org.javastorm.battle.BattlePlayer;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.fort.FortData;
import org.javastorm.network.commands.NSCommand;

// This is a stub for our own selves in the 
public class BattleServerClientStub extends BattleServerClient
{
	public BattleServerClientStub(BattleServer battleServer, BattlePlayer player)
	{
		super(battleServer, new ZonePlayer(player.getVersion()));
		this.battlePlayer = player;

		player.getFlags().setCurrentlyLoggedIn(true);
		player.getFlags().setEverLoggedIn(true);
		player.getFlags().setReadyToRock(true);

		// TODO: make this better.
		// Opens up a fort, strips it and feeds it into battleplayer fort data (otherwise we'd be transmitting the whole fort to clients).
		FortData fd = new FortData(null);
		fd.open("botsy.fort");
		FortData myFD = this.battlePlayer.getFortData();
		myFD.create("NONAME", true);
		myFD.receiveStrippedImage(fd.createStrippedImage());
		myFD.updateSubscriberID(player.getSubscriberID());
		myFD.updateNickname(player.getNickname());
	}

	public void makeBattleIsland()
	{
		
	}

	public void sendCommand(NSCommand command)
	{
		// No.
	}

	public Connection getConnection()
	{
		return null;
	}
}
