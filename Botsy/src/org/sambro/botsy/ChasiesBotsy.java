package org.sambro.botsy;

import org.javastorm.challenge.ZonePlayer;
import org.javastorm.network.Connection;

public class ChasiesBotsy extends BotsySpawn
{
	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		super.spawn(id, master, masterPlayer);

		this.botinfo.setNickname("ChasiesBotsy" + (id + 1));
		this.botinfo.setSubscriberID(botinfo.getNickname().hashCode());

		return this.connect(master.challengeServer.getHost(), master.challengeServer.getPort());
	}

	public void kill()
	{
		this.say("Just 'cos I'm better than  you at chasies, " + this.getMasterPlayer().getNickname() + "!");
		super.kill();
	}

	protected void challengeJoinZone()
	{
		this.say("Gonna getcha " + this.getMasterPlayer().getNickname() + "!!");
		this.challengeServer.move(this.getMasterPlayer().getPos());
		this.barkThread = new BarkThread();
		this.barkThread.start();
	}

	// Called when a player makes a normal island move.
	protected void challengePlayerMove(ZonePlayer player)
	{
		if (player.getSubscriberID() == this.getMasterPlayer().getSubscriberID())
			this.challengeServer.move(player.getPos());
	}

	protected void challengeDisconnected()
	{
		super.challengeDisconnected();
		this.barkThread.interrupt();
		this.barkThread = null;
	}

	private class BarkThread extends Thread
	{
		private String[] barks =
		{ "Give up yet %s?", "Resistance is futile %s!", "You may as well give up now %s, I have the stamina of 100 stallions!", "You run from me like a little girl %s!", "Yesss, keep running %s. I enjoy the hunt.", "Oh I'm sorry %s, were you trying to present me with a challenge?!", "All this chasing has made me hungry! I could do with a <product placement goes here> right now!", "Weee!", };

		private int barkIndex = 0;

		public void run()
		{
			while (Thread.currentThread() == barkThread)
			{
				Connection.waitMillis((45 * 1000));

				if (challengeServer.connected())
				{
					say(String.format(this.barks[this.barkIndex++], getMasterPlayer().getNickname()));
					if (this.barkIndex >= this.barks.length)
						this.barkIndex = 0;
				}
			}
		}
	}

	private Thread barkThread;
}
