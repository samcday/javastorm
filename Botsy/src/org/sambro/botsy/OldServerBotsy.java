package org.sambro.botsy;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.javastorm.World;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZoneRing;
import org.javastorm.network.Connection;
import org.javastorm.network.commands.ZChalLaunchServer;
import org.javastorm.network.commands.ZChalLaunchServer.ZAClient;
import org.javastorm.territory.Territory;

public class OldServerBotsy extends BotsySpawn
{

	public World getBattle()
	{
		return null;
	}

	public void kill()
	{
		this.exe.destroy();
		super.kill();
	}

	protected void challengeDisconnected()
	{
	}

	protected void challengePlayerSpeak(ZonePlayer player, String msg)
	{
		int subscriberID = player.getSubscriberID();

		msg = msg.toLowerCase();

		if (this.isAdmin(subscriberID))
		{
			if ((msg.indexOf("/identify") > -1))
			{
				this.say("My ID is " + this.getID());
			}
		}

		// We process special commands spoken in BATTLE chat.
		if (msg.startsWith("~[iicon.d36]"))
		{
			if (msg.indexOf("/1v1") > -1)
			{
				this.server.getBattleOptions().set1v1();
				this.challengeServer.sendBattleOptions(this.server.getBattleOptions());
			}

			if (msg.indexOf("/2v2") > -1)
			{
				this.server.getBattleOptions().set2v2();
				this.challengeServer.sendBattleOptions(this.server.getBattleOptions());
			}

			if (msg.indexOf("/3v3") > -1)
			{
				this.server.getBattleOptions().set3v3();
				this.challengeServer.sendBattleOptions(this.server.getBattleOptions());
			}

			if ((msg.indexOf("/observer") > -1))
			{
				if ((msg.indexOf("/observer") + 9) >= msg.trim().length())
				{
				}
				else
				{
					String nicknameKick = msg.substring(msg.indexOf("/observer") + 9).trim();
					ZonePlayer playerKick = this.challengeServer.findPlayerByNickname(nicknameKick);
					if (playerKick == null)
					{
						this.say("Couldn't find that player.");
						return;
					}
					else
					{
						if (this.isAdmin(player.getSubscriberID()))
						{
							this.server.getBattleOptions().setSlotSpectator(playerKick.getSlot(), !this.server.getBattleOptions().getSlotSpectator(playerKick.getSlot()));
							this.challengeServer.sendBattleOptions(this.server.getBattleOptions());
						}
					}
				}
				return;
			}
		}

		if ((msg.indexOf("/kick") > -1))
		{
			if ((msg.indexOf("/kick") + 5) >= msg.trim().length())
			{
			}
			else
			{
				String nicknameKick = msg.substring(msg.indexOf("/kick") + 5).trim();
				ZonePlayer playerKick = this.challengeServer.findPlayerByNickname(nicknameKick);
				if (playerKick == null)
				{
					this.say("Couldn't find that player.");
					return;
				}
				else
				{
					if (this.isAdmin(player.getSubscriberID()))
					{
						this.challengeServer.kickOff(playerKick.getSubscriberID());
					}
				}
			}
			return;
		}

		if (msg.indexOf(this.botinfo.getNickname().toLowerCase()) > -1)
		{
			if (msg.indexOf("/clickin") > -1)
			{
				this.challengeServer.clickIn();
			}

			if (msg.indexOf("/start") > -1)
			{
				this.challengeServer.startGame();
			}
		}

		return;
	}

	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		super.spawn(id, master, masterPlayer);

		this.exeConnection = new Socket();

		this.botinfo.setNickname("ServerBotsy" + (id + 1));
		this.botinfo.setSubscriberID(this.botinfo.getNickname().hashCode());

		this.challengeServer.forceZone(this.getMaster().challengeServer.getCurrentZone());
		if (!this.connect(master.challengeServer.getHost(), master.challengeServer.getPort()))
			return false;

		return true;
	}

	protected void challengeJoinZone()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				say("Bringing Battle Server online. One moment.");
				try
				{
					ProcessBuilder pb = new ProcessBuilder("D:\\NS\\C\\BotsyDebug\\C.exe", "" + (6797 - getID()));
					pb.directory(new File("D:\\NS"));
					exe = pb.start();
				}
				catch (IOException ioe)
				{
					say("Failed to bring Battle Server online.");
					disconnect();
					return;
				}

				int numTries = 0;

				while (numTries < 5)
				{
					try
					{
						exeConnection = new Socket();
						exeConnection.connect(new InetSocketAddress("localhost", 6797 - getID()), 1000);
					}
					catch (IOException ioe)
					{
						//ioe.printStackTrace();
						numTries++;
					}

					if (exeConnection.isConnected())
					{
						ZoneRing emptyRing = challengeServer.findEmptyRing();
						if (emptyRing == null)
						{
							say("There are no free rings for me to serve on. Disconnecting now.");
							return;
						}

						challengeServer.joinRing(emptyRing.getRingNum(), ZoneRing.WHITE);

						say("Successfully brought Battle Server online, serving a ring now.");

						return;
					}

					Connection.waitMillis(2000);
				}

				say("Failed to bring Battle Server online.");
				disconnect();
			}
		}, "Battle Server Bootup Thread").start();
	}

	protected void challengePlayerJoinRing(ZonePlayer player)
	{
		if (this.challengeServer.getRing() == null)
			return;

		if ((player.getRing() == this.challengeServer.getRing().getRingNum()) && (player != this.getMe()))
		{
			this.server.addPlayer(player);
		}
	}

	protected void challengePlayerLeaveRing(ZonePlayer player, int ring)
	{
		if (this.challengeServer.getRing() != null)
			if (ring == this.challengeServer.getRing().getRingNum())
			{
				this.server.removePlayer(player);
			}
	}

	protected void challengePlayerBattleMaster(ZonePlayer player)
	{
		if (player == this.challengeServer.getMe())
		{
			System.err.println("I AM BM! HEAR ME ROAR!");
			// Set up our NSBattleServer.
			this.server = new BattleProxy();
			if (!this.server.init(this, this.exeConnection))
			{
				this.say("I seem to be having connection problems.");
			}

			// Now we send the battle options.
			this.challengeServer.sendBattleOptions(this.server.getBattleOptions());

			// Update our status with zone server.
			this.challengeServer.acceptConnection();
		}
	}

	protected void challengeLaunchServer(int gameID, Territory bountyTerr, ZAClient[] players)
	{
		ZChalLaunchServer launch = new ZChalLaunchServer();
		launch.setBountyTerr(bountyTerr);
		launch.setClients(players);
		launch.setGameID(gameID);
		this.server.start(launch);
		this.disconnect();
	}

	private Process exe;

	private Socket exeConnection;

	private BattleProxy server;
}
