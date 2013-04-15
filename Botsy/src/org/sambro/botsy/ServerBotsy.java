package org.sambro.botsy;

import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZoneRing;
import org.javastorm.network.BattleServer;
import org.javastorm.network.commands.ZChalAdmin;
import org.javastorm.network.commands.ZChalLaunchServer;
import org.javastorm.network.commands.ZChalLaunchServer.ZAClient;
import org.javastorm.territory.Territory;

public class ServerBotsy extends BotsySpawn
{
	public ServerBotsy()
	{
	}

	public boolean spawn(int id, MasterBotsy master, ZonePlayer masterPlayer)
	{
		super.spawn(id, master, masterPlayer);

		this.botinfo.setNickname("ServerBotsy" + (id + 1));
		this.botinfo.setSubscriberID(this.botinfo.getNickname().hashCode());

		this.challengeServer.forceZone(this.getMaster().challengeServer.getCurrentZone());

		if (!this.connect(master.challengeServer.getHost(), master.challengeServer.getPort()))
			return false;

		return true;
	}

	public void kill()
	{
		super.kill();

		if (this.battleServer != null)
		{
			this.battleServer.stop();
			this.battleServer = null;
		}
	}

	public void challengeJoinZone()
	{
		this.findEmptyRing();
	}

	private void findEmptyRing()
	{
		ZoneRing rings[];
		rings = this.challengeServer.getZoneRings();

		for (ZoneRing ring : rings)
		{
			if (ring == null)
				continue;

			if (ring.isEmpty())
			{
				this.challengeServer.joinRing(ring.getRingNum(), ZoneRing.WHITE);
				return;
			}
		}
	}

	protected void challengePlayerSpeak(ZonePlayer player, String msg)
	{
		int subscriberID = player.getSubscriberID();

		msg = msg.toLowerCase();

		if (this.isAdmin(subscriberID))
		{
			if ((msg.indexOf("/identify") > -1))
			{
				this.say("My ID is " + this.getID() + ". My master is: " + this.getMasterPlayer().getNickname());
			}
		}

		// We process special commands spoken in BATTLE chat.
		if (msg.startsWith("~[iicon.d36]"))
		{
			if (msg.indexOf("/clickin") > -1)
			{
				this.challengeServer.clickIn();
			}

			if (msg.indexOf("/start") > -1)
			{
				this.challengeServer.startGame();
			}

			if (msg.indexOf("/1v1") > -1)
			{
				this.battleServer.getBattleOptions().set1v1();
				this.challengeServer.sendBattleOptions(this.battleServer.getBattleOptions());
				this.challengeServer.changeRingDescription(this.challengeServer.getRing(), "Serving 1v1");
			}

			if (msg.indexOf("/2v2") > -1)
			{
				this.battleServer.getBattleOptions().set2v2();
				this.challengeServer.sendBattleOptions(this.battleServer.getBattleOptions());
				this.challengeServer.changeRingDescription(this.challengeServer.getRing(), "Serving 2v2");
			}

			if (msg.indexOf("/3v3") > -1)
			{
				this.battleServer.getBattleOptions().set3v3();
				this.challengeServer.sendBattleOptions(this.battleServer.getBattleOptions());
				this.challengeServer.changeRingDescription(this.challengeServer.getRing(), "Serving 3v3");
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
							this.battleServer.getBattleOptions().setSlotSpectator(playerKick.getSlot(), !this.battleServer.getBattleOptions().getSlotSpectator(playerKick.getSlot()));
							this.challengeServer.sendBattleOptions(this.battleServer.getBattleOptions());
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

		return;
	}

	protected void challengePlayerJoinRing(ZonePlayer player)
	{
		if (player == this.getMe())
		{
			if (!player.getStatusFlags().isMaster())
			{
				// Someone joined the ring a splitsecond before us. Find another ring.
				this.findEmptyRing();
			}
		}
		else
		{
			ZoneRing ring = this.challengeServer.getZoneRing(player.getRing());
			if (ring == null)
				return;

			if (ring.getBattleMaster() == this.getMe())
			{
				this.say("Added " + player.getNickname() + " to battleserver connection pool. pID=" + player.getSlot());
				this.battleServer.addPlayer(player);
			}
		}
	}

	protected void challengePlayerBattleMaster(ZonePlayer player)
	{
		if (player == this.challengeServer.getMe())
		{
			this.battleServer = new BattleServer(this.getMe(), this.getMasterPlayer().getVersion());
			this.say("Successfully found a ring to serve on.");

			// Now we send the battle options.
			//this.battleServer.getBattleOptions().set2v2();
			this.challengeServer.sendBattleOptions(this.battleServer.getBattleOptions());
			this.challengeServer.changeRingDescription(this.challengeServer.getRing(), "Serving 2v2");

			// Update our status with zone server.
			this.challengeServer.acceptConnection();
			this.challengeServer.clickIn();

			// Move our master over to our ring.
			ZChalAdmin admin = new ZChalAdmin();
			admin.setExtrainfo(this.getMasterPlayer().getPlayerIndex());
			admin.setMode(ZChalAdmin.ADMIN_MOVEPLAYER);
			admin.setPassword("NEWPASS"); // how original
			admin.setSailToBattle(this.getMe().getRing());
			admin.setSailToBattleSlot(ZoneRing.RED);
			this.challengeServer.sendRaw(admin);

			/*ZChalAdmin admin2 = new ZChalAdmin();
			admin2 = new ZChalAdmin();
			admin2.setExtrainfo(this.getMe().getRing());
			admin2.setMode(ZChalAdmin.ADMIN_STARTBATTLE);
			admin2.setPassword("NEWPASS"); // how original
			challengeServer.sendRaw(admin2);*/
		}
	}

	protected void challengeLaunchServer(int gameID, Territory bountyTerr, ZAClient[] players)
	{
		ZChalLaunchServer zcls = new ZChalLaunchServer();
		zcls.setBountyTerr(bountyTerr);
		zcls.setClients(players);
		zcls.setGameID(gameID);
		this.battleServer.start(zcls);
		this.disconnect();
	}

	protected void challengePlayerLeaveRing(ZonePlayer player, int ring)
	{
		if (ring == this.getMe().getRing())
		{
			this.say("Removed " + player.getNickname() + " from battleserver connection pool.");
			this.battleServer.removePlayer(player);
		}
	}

	private BattleServer battleServer;
}
