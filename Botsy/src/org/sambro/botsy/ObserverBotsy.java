package org.sambro.botsy;

import org.javastorm.World;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZoneRing;
import org.javastorm.network.BattleClient;
import org.javastorm.network.Connection;

// Goes into game as spectator.
public class ObserverBotsy extends BotsySpawn
{
	public ObserverBotsy(ZonePlayer master)
	{
		super();
		this.masterPlayer = master;
	}

	public void kill()
	{
		super.kill();

		if (this.battleClient != null)
			this.battleClient.disconnect();
	}

	public World getBattle()
	{
		if (this.battleClient != null)
			return this.battleClient.getBattle();
		else
			return null;
	}

	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		super.spawn(id, master, masterPlayer);

		this.botinfo.setNickname("ObserverBotsy" + (id + 1));
		this.botinfo.setSubscriberID(this.botinfo.getNickname().hashCode());
		this.botinfo.getStatusFlags().setBadDiag(true); // Make sure OBSERVER botsy can't be passed serve accidentally.

		this.challengeServer.forceZone(this.getMaster().challengeServer.getCurrentZone());

		if (!this.connect(master.challengeServer.getHost(), master.challengeServer.getPort()))
			return false;

		return true;
	}

	protected void challengePlayerSpeak(ZonePlayer player, String msg)
	{
		int subscriberID = player.getSubscriberID();
		//String nickname = command.getNickname();

		msg = msg.toLowerCase();

		if (this.isAdmin(subscriberID))
		{
			if ((msg.indexOf("/identify") > -1))
			{
				this.say("My ID is " + this.getID());
			}
		}

		if (msg.indexOf(this.botinfo.getNickname().toLowerCase()) > -1)
		{
			if (msg.indexOf("/clickin") > -1)
			{
				this.challengeServer.clickIn();
			}

			if (msg.indexOf("/clickout") > -1)
			{
				this.challengeServer.clickOut();
			}
		}
	}

	public boolean acceptConnection(BattleClient battleClient)
	{
		if (battleClient.preconnect(this.getMe(), this.challengeServer.getRing()))
		{
			// Accept connection.
			this.challengeServer.acceptConnection();
			return true;
		}
		else
		{
			return false;
		}
	}

	protected void challengePlayerJoin(ZonePlayer player, boolean initial)
	{

	}

	protected void challengeJoinZone()
	{
		this.joinUp();
	}

	private void joinUp()
	{
		int ringNum = this.masterPlayer.getRing();

		if (ringNum == 0)
			return;

		ZoneRing ring = this.challengeServer.getZoneRing(ringNum);
		if (ring == null)
			return;

		// Make sure there isn't already another BotsySpawn on this ring.
		ZonePlayer[] players = ring.getPlayers();

		for (ZonePlayer player : players)
		{
			if (this.getMaster().isBotSpawn(player))
			{
				this.say("I already have a comrade on this ring, so I guess you don't need me ;( Disconnecting now ...");
				this.kill();
				return;
			}
		}

		if (ring.slotAvailable(ZoneRing.WHITE))
			this.challengeServer.joinRing(ringNum, ZoneRing.WHITE);
		else if (ring.slotAvailable(ZoneRing.GREEN))
			this.challengeServer.joinRing(ringNum, ZoneRing.GREEN);
		else if (ring.slotAvailable(ZoneRing.TEAL))
			this.challengeServer.joinRing(ringNum, ZoneRing.TEAL);
		else if (ring.slotAvailable(ZoneRing.ORANGE))
			this.challengeServer.joinRing(ringNum, ZoneRing.ORANGE);
		else
		{
			this.say("Doesn't seem to be any free spots for little old me :( Disconnecting now ...");
			this.kill();
		}
	}

	protected void challengeDisconnected()
	{
	}

	private void connectToHost()
	{
		final ObserverBotsy me = this;

		// If there is already a connection attempt in progress, tell it to quit, then wait for it to finish.
		if (this.connectionThread != null)
		{
			this.connectionThread = null;
		}

		this.connectionThread = new Thread()
		{
			public void run()
			{
				int numTries = 0;
				Boolean connected = false;

				//NSNetwork.waitMillis(1000);

				// If we were already connected to someone else, cut em off.
				if (battleClient != null)
					battleClient.disconnect();

				// Create our battle client.
				battleClient = new BattleClient();
				callbacks = new ObserverBattleClient(me, battleClient, botinfo);
				battleClient.setCallback(callbacks);

				while (!connected && (numTries < 5))
				{
					Connection.waitMillis(1000);

					// Make sure we're still the current connection attempt.
					if (this != connectionThread)
					{
						return;
					}

					if (acceptConnection(battleClient))
					{
						challengeServer.clickIn();
						return;
					}
					else
					{
						numTries++;
					}
				}

				say("I seem to be having connection problems. :( Try calling me again...");
				kill();
				return;
			}
		};
		this.connectionThread.start();
	}

	protected void challengePlayerBattleMaster(ZonePlayer player)
	{
		// We sit on the ring that our master is on. Battlemaster may be passed around during this time.
		if (player.getRing() == this.getMe().getRing())
		{
			this.connectToHost();
		}
	}

	protected void challengePlayerJoinRing(ZonePlayer player)
	{
		// If our master joins a ring, we shall follow!
		if (player.getSubscriberID() == this.masterPlayer.getSubscriberID())
		{
			Connection.waitMillis(1000);
			this.joinUp();
		}

		if (player == this.getMe())
		{
			if (!player.getStatusFlags().isConnected())
			{
				this.connectToHost();
			}

		}
	}

	//	 Called when a player leaves a ring.
	protected void challengePlayerLeaveRing(ZonePlayer player, int ring)
	{
		if (player.getSubscriberID() == this.masterPlayer.getSubscriberID())
		{
			this.challengeServer.move(player.getPos());
		}
	}

	protected void challengePlayerKicked(ZonePlayer player)
	{
		if (player == this.getMe())
		{
			this.say("How rude! :(");
			this.kill();
		}
	}

	// Called when a battle starts as client.
	protected void challengeLaunchClient(int gameID, String ip)
	{
		this.battleClient.startBattle(this.botinfo.getFortData());
		this.disconnect();
	}

	protected void challengePlayerClickOut(ZonePlayer player)
	{
		if (player == this.getMe())
		{
			this.challengeServer.clickIn();
		}
	}

	protected void challengePlayerMove(ZonePlayer player)
	{
		if (player.getSubscriberID() == this.masterPlayer.getSubscriberID())
		{
			if (player.getRing() == 0)
				this.challengeServer.move(this.masterPlayer.getPos());
		}
	}

	private Thread connectionThread;

	private ObserverBattleClient callbacks;

	private BattleClient battleClient; // If we're connected to a battle as player, we use this.

	private ZonePlayer masterPlayer; // The player who summoned this Botspawn.
}
