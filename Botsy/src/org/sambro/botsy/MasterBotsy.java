package org.sambro.botsy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.io.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.javastorm.BoardCoord;
import org.javastorm.Filesystem;
import org.javastorm.Player;
import org.javastorm.PlayerCoreData;
import org.javastorm.NSUtil;
import org.javastorm.World;
import org.javastorm.Netstorm;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZonePlayerStatusFlags;
import org.javastorm.challenge.ZoneRing;
import org.javastorm.network.BandwidthMonitor;
import org.javastorm.network.Connection;
import org.javastorm.network.ConnectionPool;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZChalAdmin;
import org.javastorm.territory.Territory;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;

import com.ibm.jadt.Dictionary;
import com.ibm.jadt.DictionaryRecord;
import com.maxmind.geoip.*;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

// This is the primary Botsy people come in contact with. It sits in zones keeping people amused and also
// spawns the other bots that may serve/observe/etc.
public class MasterBotsy extends BaseBotsy implements CommandParserHost
{
	public boolean connect(String host, int port)
	{
		this.motd = "";
		this.gotMOTD = false;

		return super.connect(
			host, port);
	}

	private class DisconnectCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			killSpawn();
			disconnecting = true;
			disconnect();
		}
	}

	// Here be all the chat commands Master Botsy accepts.
	private class ExceptionCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			throw new RuntimeException(
				"generated exception.");
		}
	}

	private class PlayerFlagsCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer target = (ZonePlayer) args[0];
			ZonePlayerStatusFlags flags = target.getStatusFlags();

			StringBuilder sb = new StringBuilder();
			sb.append(
				target.getNickname()).append(
				": ");

			if (flags.isAccepted())
				sb.append("CPF_ACCEPTED ");

			if (flags.isBadDiag())
				sb.append("CPF_BADDIAG ");

			if (flags.isChange())
				sb.append("CPF_CHANGE ");

			if (flags.isConnected())
				sb.append("CPF_CONNECTED ");

			if (flags.isInitial())
				sb.append("CPF_INITIAL ");

			if (flags.isKicked())
				sb.append("CPF_KICKED ");

			if (flags.isLaunch())
				sb.append("CPF_LAUNCH ");

			if (flags.isMaster())
				sb.append("CPF_MASTER ");

			if (flags.isValid())
				sb.append("CPF_VALID ");

			if (flags.isWatcher())
				sb.append("CPF_WATCHER ");

			say(sb.toString());
		}
	}

	private class CountryCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args == null)
			{
				String ip = player.getIpString();
				say(player.getNickname() + ", you live in: " + cl.getCountry(
					ip).getName());
			}
			else
			{
				ZonePlayer playerIP = (ZonePlayer) args[0];
				String ip = playerIP.getIpString();
				say(playerIP.getNickname() + " lives in: " + cl.getCountry(
					ip).getName());
			}
		}
	}

	private class DictionaryCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			DictionaryRecord dr = dictionary.getMeaning((String) args[0]);

			while (dr != null)
			{
				String strName = dr.getWordName();
				String pronunciation = dr.getPronunciation();
				String type = dr.getType();
				String meaning = dr.getDescription();

				say(String.format(
					"%s - %s %s \n %s", strName, pronunciation, type, meaning));

				dr = dr.getNextRecord();
			}
		}
	}

	private class HasTechCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer zonePlayer = (ZonePlayer) args[0];
			String tech = (String) args[1];

			if (hasTech(
				zonePlayer, tech))
			{
				say(zonePlayer.getNickname() + " has " + tech);
			}
			else
				say(zonePlayer.getNickname() + " does not have " + tech);
		}
	}

	private class TechListCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer zonePlayer = (ZonePlayer) args[0];
			NSType units[] = Types.getAllTypes();
			StringBuilder techList = new StringBuilder();
			techList.append(
				"~E~B~u<h2>Tech List For ").append(
				zonePlayer.getNickname()).append(
				"</h2>");

			StringBuilder rainList = new StringBuilder();
			StringBuilder sunList = new StringBuilder();
			StringBuilder windList = new StringBuilder();
			StringBuilder thunderList = new StringBuilder();

			sunList.append("Sun Cannon, Sun Disc Thrower");
			if (zonePlayer.getVersion().getMinorVersion() > 78)
				sunList.append(", Stone Tower");

			for (int i = 0; i < units.length; i++)
			{
				NSType unit = units[i];

				if ((unit.getTechBit() > -1))
				{
					if (hasTech(
						zonePlayer, unit.getName()))
					{
						StringBuilder appendTo = null;

						if (unit.getAlignment().equalsIgnoreCase(
							"Rain"))
							appendTo = rainList;
						else if (unit.getAlignment().equalsIgnoreCase(
							"Thunder"))
							appendTo = thunderList;
						else if (unit.getAlignment().equalsIgnoreCase(
							"Wind"))
							appendTo = windList;
						else if (unit.getAlignment().equalsIgnoreCase(
							"Sun"))
							appendTo = sunList;

						if (appendTo != null)
						{
							if (appendTo.length() > 0)
								appendTo.append(", ");
							appendTo.append(unit.getName());
						}
					}
				}
			}

			if (sunList.length() > 0)
				techList.append(
					"~E~B~iSun: ~w").append(
					sunList).append(
					"<br>");
			if (windList.length() > 0)
				techList.append(
					"~E~B~iWind: ~w").append(
					windList).append(
					"<br>");
			if (rainList.length() > 0)
				techList.append(
					"~E~B~iRain: ~w").append(
					rainList).append(
					"<br>");
			if (thunderList.length() > 0)
				techList.append(
					"~E~B~iThunder: ~w").append(
					thunderList).append(
					"<br>");

			hostPopupTo(
				techList.toString(), player);
		}
	}

	private class IPCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args == null)
			{
				String ip = player.getIpString();
				whisper(
					player.getNickname() + ", your IP address is: " + ip,
					player.getSubscriberID());
			}
			else
			{
				if (!isAdmin(player.getSubscriberID()))
					return;

				ZonePlayer playerIP = (ZonePlayer) args[0];
				String ip = playerIP.getIpString();
				whisper(
					playerIP.getNickname() + " IP: " + ip,
					player.getSubscriberID());
			}
		}
	}

	private class ChuckCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args == null)
			{
				if ((uselessFactSpam + 15000) < System.currentTimeMillis())
				{
					say(ChuckNorris.getChuck());
					uselessFactSpam = System.currentTimeMillis();
					return;
				}
			}
			else
			{
				if ((uselessFactSpam + 15000) < System.currentTimeMillis())
				{
					float num = (Float) args[0];
					String hehe = ChuckNorris.getChuckNumber((int) num);
					if (hehe == null)
					{
						say("That number is out of range!");
						return;
					}
					else
						say(hehe);

					uselessFactSpam = System.currentTimeMillis();
					return;
				}
			}
		}
	}

	private class RandomFactCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args == null)
			{
				if ((uselessFactSpam + 15000) < System.currentTimeMillis())
				{
					say(RandomFacts.getRandomFact());
					uselessFactSpam = System.currentTimeMillis();
					return;
				}
			}
			else
			{
				if ((uselessFactSpam + 15000) < System.currentTimeMillis())
				{
					float num = (Float) args[0];
					String hehe = RandomFacts.getRandomFact((int) num);
					if (hehe == null)
					{
						say("That number is out of range!");
						return;
					}
					else
						say(hehe);

					uselessFactSpam = System.currentTimeMillis();
					return;
				}
			}
		}
	}

	private class GroupHugCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer[] players = challengeServer.getPlayers();
			BoardCoord bc = getMe().getPos();
			say("GROUUUUUP HUUUUG!");
			for (int i = 0; i < players.length; i++)
			{
				if (players[i].getRing() > 0)
					continue;
				challengeServer.movePlayer(
					players[i], bc);
			}
		}
	}

	private class ChaseCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args != null)
				chase(
					(ZonePlayer) args[0], false);
			else
				chase(
					(ZonePlayer) player, false);
		}
	}

	private class OrbitCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args != null)
				orbit(
					(ZonePlayer) args[0], false);
			else
				orbit(
					(ZonePlayer) player, false);
		}
	}

	private class OrbitAllCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer[] players = challengeServer.getPlayers();
			for (int i = 0; i < players.length; i++)
			{
				if (players[i].getNickname().toLowerCase().indexOf(
					"botsy") == -1)
					orbit(
						players[i], true);
			}
		}
	}

	private class MoveToRingCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			int ringNum = ((Float) args[0]).intValue();
			ZoneRing ring = challengeServer.getZoneRing(ringNum);
			challengeServer.move(ring.getPos());
		}
	}

	private class DateTimeCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			Calendar date = Calendar.getInstance();
			if (args != null)
			{
				TimeZone tz = TimeZone.getTimeZone((String) args[0]);
				if ((tz.getID() == TimeZone.getTimeZone(
					"GMT").getID() && !((String) args[0]).equalsIgnoreCase("GMT")))
				{
					whisper(
						"That timezone is not recognized.",
						player.getSubscriberID());
					return;
				}
				date = Calendar.getInstance(tz);
			}

			whisper(
				"The date/time is " + date.get(Calendar.DAY_OF_MONTH) + "/" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.YEAR) + " " + date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND) + " according to " + date.getTimeZone().getDisplayName(),
				player.getSubscriberID());
		}
	}

	private class PingCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			whisper(
				"Attempting to ping you now, " + player.getNickname(),
				player.getSubscriberID());

			try
			{
				InetAddress ip = InetAddress.getByAddress(new byte[]
				{
					(byte) player.getIp()[0],
					(byte) player.getIp()[1],
					(byte) player.getIp()[2],
					(byte) player.getIp()[3] });
				long connectStart = System.currentTimeMillis();
				if (!ip.isReachable(3000))
				{
					whisper(
						"Ping failed.", player.getSubscriberID());
					return;
				}

				long connectEnd = System.currentTimeMillis();
				whisper(
					"Ping successful. Your ping is: " + (connectEnd - connectStart) + "ms",
					player.getSubscriberID());
			}
			catch (Exception e)
			{
				whisper(
					"Ping failed.", player.getSubscriberID());
				e.printStackTrace();
			}
		}
	}

	private class FirewallCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			whisper(
				"Checking the status of port 6799 now, " + player.getNickname(),
				player.getSubscriberID());

			try
			{
				InetAddress ip = InetAddress.getByAddress(new byte[]
				{
					(byte) player.getIp()[0],
					(byte) player.getIp()[1],
					(byte) player.getIp()[2],
					(byte) player.getIp()[3] });
				Socket sock = new Socket();
				sock.connect(
					new InetSocketAddress(
						ip, 6799), 2000);
				if (sock != null)
				{
					whisper(
						"You seem to have passed my firewall test, " + player.getNickname() + ".",
						player.getSubscriberID());
					return;
				}
			}
			catch (Exception e)
			{
			}

			whisper(
				"You seem to be firewalled on port 6799, " + player.getNickname() + ", this port is required to serve a game.",
				player.getSubscriberID());
		}
	}

	private class ChangeZoneCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			float zoneF = (Float) args[0];
			int zone = (int) (zoneF);
			challengeServer.joinZone(zone);
		}
	}

	/*private class PriestFestCommand implements ChatCommandCallback
	{
		public void execute(NSPlayer player, boolean whisper, Object... args)
		{
			NSZoneRing[] rings = challengeServer.getZoneRings();
			
			for(int i = 0; i < rings.length; i++)
			{
				if(rings[i].getBattleMaster() != null)
				{					
					if(rings[i].getBattleMaster() == player)
						for(int j = 0; j < 8; j++)
						{
							spawnBattleZombie(rings[i].getRingNum());
							NSProtocol.waitMillis(100);
						}
				}
			}
		}
	}*/

	private class LastSeenCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			Timestamp lastSeen = getPlayerLastSeen((String) args[0]);
			if (lastSeen == null)
			{
				say("Couldn't find that player in database.");
				return;
			}
			else
			{
				DateFormat df = DateFormat.getDateTimeInstance();
				df.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
				say("I last saw " + (String) args[0] + " descend into zones @ " + df.format(lastSeen) + " (server time)");
			}
		}
	}

	private class IslandHumpCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer hump = (ZonePlayer) args[0];
			if (humpThread.isRunning())
				humpThread.stop();
			orbitalThread.stop();

			// hahahahahahahahahahahhahahahahahahahahahahhahahaha this is cute.
			humpThread.start(
				MasterBotsy.this, hump);
		}
	}

	private class ZoneKickCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerKick = (ZonePlayer) args[0];
			ZChalAdmin admin = new ZChalAdmin();
			admin.setExtrainfo(playerKick.getSubscriberID());
			admin.setMode(ZChalAdmin.ADMIN_KICK);
			admin.setPassword("NEWPASS"); // how original
			challengeServer.sendRaw(admin);
		}
	}

	private class ZoneMcastCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZChalAdmin admin = new ZChalAdmin();
			admin.setMode(ZChalAdmin.ADMIN_MCAST);
			admin.setPassword("NEWPASS"); // how original
			admin.setBcasting((String) args[0]);
			challengeServer.sendRaw(admin);
		}
	}

	private class ZoneBcastCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZChalAdmin admin = new ZChalAdmin();
			admin.setMode(ZChalAdmin.ADMIN_BCAST);
			admin.setPassword("NEWPASS"); // how original
			admin.setBcasting((String) args[0] + "\n");
			challengeServer.sendRaw(admin);
		}
	}

	private class SendToBattleCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerSend = (ZonePlayer) args[0];
			ZoneRing emptyRing = challengeServer.findEmptyRing();

			if (emptyRing == null)
			{
				say("Can't find an empty ring.");
				return;
			}

			ZChalAdmin admin = new ZChalAdmin(), admin2 = new ZChalAdmin();

			admin.setExtrainfo(playerSend.getPlayerIndex());
			admin.setMode(ZChalAdmin.ADMIN_MOVEPLAYER);
			admin.setPassword("NEWPASS"); // how original
			admin.setSailToBattle(emptyRing.getRingNum());
			admin.setSailToBattleSlot(1);
			challengeServer.sendRaw(admin);

			admin2 = new ZChalAdmin();
			//admin.setExtrainfo2(playerSend.getPlayerIndex());
			admin2.setExtrainfo(emptyRing.getRingNum());
			admin2.setMode(ZChalAdmin.ADMIN_STARTBATTLE);
			admin2.setPassword("NEWPASS"); // how original
			challengeServer.sendRaws(new NSCommand[]
			{ admin, admin2 });
		}
	}

	private class ResetServerCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZChalAdmin admin = new ZChalAdmin();
			admin.setMode(ZChalAdmin.ADMIN_RESET);
			admin.setPassword("NEWPASS"); // how original
			challengeServer.sendRaw(admin);
		}
	}

	private class TooSexyCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer[] players = challengeServer.getPlayers();
			for (int i = 0; i < players.length; i++)
			{
				if (players[i].getRing() == 0)
				{
					challengeServer.movePlayer(
						players[i], ((ZonePlayer) player).getPos());
				}
			}
		}
	}

	private class SubIDCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if(args == null)
			{
				whisper(
					"Your subscriberID is " + player.getSubscriberID(),
					player.getSubscriberID());
			}
			else
			{
				ZonePlayer targetPlayer = (ZonePlayer) args[0];
				whisper(
					targetPlayer.getNickname() + "'s subscriberID is " + targetPlayer.getSubscriberID(),
					player.getSubscriberID());
			}
		}
	}

	private class SpawnObserverCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer zonePlayer = (ZonePlayer) player;

			if (zonePlayer.getRing() == 0)
			{
				whisper(
					"You're not on a ring you silly dildo!",
					player.getSubscriberID());
				return;
			}

			if (!playerMaySpawn(zonePlayer))
			{
				whisper(
					"You already have already summoned a spawn. If you no longer required your current spawn, please /dismiss it.",
					player.getSubscriberID());
				return;
			}

			whisper(
				player.getNickname() + ", I'd like you to meet one of my brothers ... ",
				player.getSubscriberID());

			synchronized (spawn)
			{
				ObserverBotsy client = new ObserverBotsy(
					zonePlayer);

				if (client.spawn(
					spawn.size(), MasterBotsy.this, zonePlayer))
					whisper(
						"He'll be here shortly to watch your game.",
						player.getSubscriberID());
				else
				{
					whisper(
						"Actually, never mind.", player.getSubscriberID());
					return;
				}

				spawn.add(client);
			}
		}
	}

	private class SpawnServerCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer zonePlayer = (ZonePlayer) player;

			if (!playerMaySpawn(zonePlayer))
			{
				whisper(
					"You already have already summoned a spawn. If you no longer required your current spawn, please /dismiss it.",
					player.getSubscriberID());
				return;
			}

			whisper(
				player.getNickname() + ", I know this guy, he's really good at serving games ...",
				player.getSubscriberID());

			synchronized (spawn)
			{
				BotsySpawn server = new ServerBotsy();
				if (server.spawn(
					spawn.size(), MasterBotsy.this, zonePlayer))
					whisper(
						"He'll be here shortly to serve yours.",
						player.getSubscriberID());
				else
				{
					whisper(
						"Actually, never mind. He's busy right now sorry.",
						player.getSubscriberID());
					return;
				}
				spawn.add(server);
			}

		}
	}

	private class DismissCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer zonePlayer = (ZonePlayer) player;

			BotsySpawn[] slaves = getPlayerSpawn(zonePlayer);
			if (slaves != null)
			{
				for (int i = 0; i < slaves.length; i++)
					slaves[i].kill();
			}
		}
	}

	private class SpawnRulesCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("~E~B~u<h2>Spawn Rules</h2>");
			builder.append("~E~wPlease understand that intentional misuse of the Botsy spawn system can cause server instability. This is why these rules are very important, and will be enforced strongly.<br>");
			builder.append("~E~rRule 1.~w One Botsy spawn per person. No exceptions.<br>");
			builder.append("~E~rRule 2.~w You will not exploit Botsy's logic to obtain more than one spawn for yourself.<br>");
			builder.append("~E~rRule 3.~w You will not find exploits to cause Botsy spawns to linger in zones when you leave. Botsy spawns should disconnect if you do. If you find a case where Botsy spawns do not leave when you do, then you must report this immediately.<br>");
			challengeServer.popupTo(
				builder.toString(), player.getSubscriberID());
		}
	}

	private class SpawnListCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ListIterator<BotsySpawn> iter = spawn.listIterator();

			if (!iter.hasNext())
			{
				whisper(
					"There are no active spawns alive.",
					player.getSubscriberID());
			}

			while (iter.hasNext())
			{
				BotsySpawn theSpawn = iter.next();

				whisper(
					String.format(
						"~iSpawn: ~w%s ~iMaster: ~w%s",
						theSpawn.getMe().getNickname(),
						theSpawn.getMasterPlayer().getNickname()),
					player.getSubscriberID());
			}
		}
	}

	private class MimicCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerMimic = (ZonePlayer) args[0];
			whisper(
				"Mimicing " + playerMimic.getNickname() + " now :)",
				player.getSubscriberID());
			mimicID = playerMimic.getSubscriberID();
		}
	}

	private class StopMimicCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			whisper(
				"Stopping Mimic.", player.getSubscriberID());
			mimicID = -1;
		}
	}

	private class DumpTerrCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerDump = (ZonePlayer) args[0];
			Territory terr = playerDump.getTerrain();
			say("canon: " + terr.getCanon() + ". rot: " + terr.getRot() + ". exists: " + terr.isExists() + ". notPlacedInPuzzleYet: " + terr.isNotPlacedInPuzzleYet() + ". takenToBattle: " + terr.isTakenToBattle() + ". decorated: " + terr.isDecorated() + ". reserved: " + terr.getReserved() + ". xChunkCoord: " + terr.getXChunkCoord() + ". yChunkCoord: " + terr.getYChunkCoord() + ". randSeed: " + terr.getRandSeed() + ". reserved: " + terr.getReserved2() + ". reserved2: " + terr.getReserved3());
		}
	}

	private class DumpCoreCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerDump = (ZonePlayer) args[0];
			PlayerCoreData coreData = playerDump.getCoreData();
			float reliability = 100f;
			if ((coreData.getGamesPlayed() != 0) && (coreData.getGamesCompleted() != 0))
				reliability = ((float) coreData.getGamesCompleted() / (float) coreData.getGamesPlayed()) * 100f;
			say("Games played: " + coreData.getGamesPlayed() + ". Games completed: " + coreData.getGamesCompleted() + "(" + reliability + "% reliability). Altar Level: " + coreData.getAltarLevel() + ". Level: " + coreData.getLevel() + ". Rank: " + coreData.getRank() + ". Tech: " + coreData.getTech());
		}
	}

	private class DescendPlayerCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer zonePlayer = (ZonePlayer) player;

			ZonePlayer playerSend = (ZonePlayer) args[0];
			if (playerSend.getRing() == 0)
				return;

			if (!playerSend.getStatusFlags().isMaster())
			{
				whisper(
					playerSend.getNickname() + " is not the battlemaster.",
					player.getSubscriberID());
				return;
			}

			ZChalAdmin admin2 = new ZChalAdmin();
			admin2 = new ZChalAdmin();
			admin2.setExtrainfo(zonePlayer.getRing());
			admin2.setMode(ZChalAdmin.ADMIN_STARTBATTLE);
			admin2.setPassword("NEWPASS"); // how original
			challengeServer.sendRaw(admin2);
		}
	}

	private class KickPlayerCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerKick = (ZonePlayer) args[0];
			ZChalAdmin admin = new ZChalAdmin();
			admin.setExtrainfo2(playerKick.getRing());
			admin.setExtrainfo(playerKick.getPlayerIndex());
			admin.setMode(ZChalAdmin.ADMIN_BATTLEKICK);
			admin.setPassword("NEWPASS"); // how original
			challengeServer.sendRaw(admin);
		}
	}

	private class KickBattleCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerKick = (ZonePlayer) args[0];
			ZChalAdmin admin = new ZChalAdmin();
			admin.setExtrainfo(playerKick.getRing());
			admin.setMode(ZChalAdmin.ADMIN_BATTLEKICKALL);
			admin.setPassword("NEWPASS"); // how original
			challengeServer.sendRaw(admin);
		}
	}

	private class SpawnDummiesCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			float countF = (Float) args[0];
			final int count = (int) countF;
			final ZonePlayer zonePlayer = (ZonePlayer) player;

			say(count + " dummy bots comin' up.");
			new Thread(
				new Runnable()
				{
					public void run()
					{
						synchronized (spawn)
						{
							for (int i = 0; i < count; i++)
							{
								BotsySpawn dummy = new DummyBotsy(
									1);
								dummy.spawn(
									spawn.size() + 1, MasterBotsy.this,
									zonePlayer);
								spawn.add(dummy);
							}
						}
					}
				}, "Dummy Spawn Thread").start();
		}
	}

	private class SpawnShowOffCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			final ZonePlayer zonePlayer = (ZonePlayer) player;

			new Thread(
				new Runnable()
				{
					public void run()
					{
						synchronized (spawn)
						{
							for (int i = 1; i < challengeServer.getZoneCount(); i++)
							{
								BotsySpawn dummy = new DummyBotsy(
									i);
								dummy.spawn(
									spawn.size() + 1, MasterBotsy.this,
									zonePlayer);
								spawn.add(dummy);
								dummy = new DummyBotsy(
									i);
								dummy.spawn(
									spawn.size() + 1, MasterBotsy.this,
									zonePlayer);
								spawn.add(dummy);
							}
						}
					}
				}, "Dummy Spawn Thread").start();
		}
	}

	private class KillSpawnCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			int id = -1;
			if (args != null)
			{
				float idF = (Float) args[0];
				id = (int) idF;

				if (id < 0 || id > spawn.capacity())
					return;
			}

			if (id == -1)
			{
				say(player.getNickname() + ", killing all my offspring now :(");

				killSpawn();
			}

			else
			{
				BotsySpawn theSpawn = findSpawnByID(id);
				if (theSpawn == null)
					return;
				BaseBotsy spawnBotsy = (BaseBotsy) theSpawn;
				say(player.getNickname() + ", killing " + spawnBotsy.getMe().getNickname() + " now :(");
				theSpawn.kill();
			}
		}
	}

	private class IslandTagCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerTag = (ZonePlayer) args[0];
			ZChalAdmin admin = new ZChalAdmin();
			admin.setMode(ZChalAdmin.ADMIN_SETPLAYERTAG);
			admin.setExtrainfo(playerTag.getSubscriberID());
			admin.setPassword("NEWPASS"); // how original
			admin.setBcasting((String) args[1]);
			challengeServer.sendRaw(admin);
		}
	}

	private class BandwidthCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			StringBuilder builder = new StringBuilder();

			BandwidthMonitor master = Connection.masterBwMonitor;
			BandwidthMonitor bw = challengeServer.getBandwidthMonitor();

			builder.append("~E~B~u<h2>Bandwidth Info</h2>");
			builder.append(String.format(
				"~E~B~rAll Traffic:<br>~yIn:~w(~iCurr: ~w%s. ~iAvg: ~w%s. ~iPeak: %s. ~iTotal: ~w%s)<br>~yOut:~w(~iCurr: ~w%s. ~iAvg: ~w%s. ~iPeak: %s. ~iTotal: ~w%s)<p>",
				master.getBpsInCurrent(), master.getBpsInAverage(),
				master.getBpsInPeak(), master.getInTraffic(),
				master.getBpsOutCurrent(), master.getBpsOutAverage(),
				master.getBpsOutPeak(), master.getOutTraffic()));
			builder.append(String.format(
				"~E~B~rMasterBotsy:<br>~yIn:~w(~iCurr: ~w%s. ~iAvg: ~w%s. ~iPeak: %s. ~iTotal: ~w%s)<br>~yOut:~w(~iCurr: ~w%s. ~iAvg: ~w%s. ~iPeak: %s. ~iTotal: ~w%s)<p>",
				bw.getBpsInCurrent(), bw.getBpsInAverage(), bw.getBpsInPeak(),
				bw.getInTraffic(), bw.getBpsOutCurrent(),
				bw.getBpsOutAverage(), bw.getBpsOutPeak(), bw.getOutTraffic()));

			ListIterator<BotsySpawn> iter = spawn.listIterator();

			while (iter.hasNext())
			{
				BotsySpawn theSpawn = iter.next();
				BandwidthMonitor spawnMonitor = theSpawn.challengeServer.getBandwidthMonitor();
				builder.append(String.format(
					"~E~B~r%s:<br>~yIn:~w(~iCurr: ~w%s. ~iAvg: ~w%s. ~iPeak: %s. ~iTotal: ~w%s)<br>~yOut:~w(~iCurr: ~w%s. ~iAvg: ~w%s. ~iPeak: %s. ~iTotal: ~w%s)<p>",
					theSpawn.getMe().getNickname(),
					spawnMonitor.getBpsInCurrent(),
					spawnMonitor.getBpsInAverage(),
					spawnMonitor.getBpsInPeak(), spawnMonitor.getInTraffic(),
					spawnMonitor.getBpsOutCurrent(),
					spawnMonitor.getBpsOutAverage(),
					spawnMonitor.getBpsOutPeak(), spawnMonitor.getOutTraffic()));
			}

			challengeServer.popupTo(
				builder.toString(), player.getSubscriberID());
		}
	}

	private class ThreadInfoCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			Thread[] threads = new Thread[Thread.currentThread().getThreadGroup().activeCount()];
			int count = Thread.currentThread().getThreadGroup().enumerate(
				threads);
			StringBuilder builder = new StringBuilder();

			if (args == null)
			{
				builder.append(
					"~E~iThere are ").append(
					Thread.currentThread().getThreadGroup().activeCount()).append(
					" active threads running.<p>");
				for (int i = 0; i < count; i++)
				{
					builder.append(
						"~E~i").append(
						threads[i].getName()).append(
						" (threadID ").append(
						threads[i].getId()).append(
						")<br>~E~w").append(
						threads[i].getState().name()).append(
						"<br>");
				}
			}
			else
			{
				float idF = (Float) args[0];
				int id = (int) idF;

				for (int i = 0; i < count; i++)
				{
					if (threads[i].getId() == id)
					{
						builder.append(
							"~E~i").append(
							threads[i].getName()).append(
							" (threadID ").append(
							threads[i].getId()).append(
							")<br>~E~w").append(
							threads[i].getState().name()).append(
							"<p>");

						StackTraceElement[] stackTrace = threads[i].getStackTrace();
						builder.append("~E~lStack Trace:<br>");
						for (int j = 0; j < stackTrace.length; j++)
							builder.append(
								" ~E~l... ").append(
								stackTrace[j].toString()).append(
								"<br>");
					}
				}
				if (builder.length() == 0)
				{
					whisper(
						"No thread with that threadID.",
						player.getSubscriberID());
					return;
				}
			}

			challengeServer.popupTo(
				builder.toString(), player.getSubscriberID());
		}
	}

	private class FullRingCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZoneRing emptyRing = challengeServer.findEmptyRing();
			if (emptyRing == null)
				return;
			int playerCount = 0;
			ZonePlayer[] players = challengeServer.getPlayers();
			ZonePlayer bm = null;

			for (int i = 0; (i < players.length) && (playerCount < 8); i++)
			{
				if (players[i].getNickname().toLowerCase().indexOf(
					"botsy") > -1)
					continue;
				if (players[i].getNickname().toLowerCase().indexOf(
					"sambro") > -1)
					continue;
				if (players[i].getNickname().toLowerCase().indexOf(
					"samthemon") > -1)
					continue;
				if (players[i].getCompatibleMinorVersion() != 78)
					continue;

				ZChalAdmin admin = new ZChalAdmin();
				admin.setExtrainfo(players[i].getPlayerIndex());
				admin.setMode(ZChalAdmin.ADMIN_MOVEPLAYER);
				admin.setPassword("NEWPASS"); // how original
				admin.setSailToBattle(emptyRing.getRingNum());
				admin.setSailToBattleSlot(playerCount);

				if (!players[i].getStatusFlags().isBadDiag() && bm == null)
					bm = players[i];

				challengeServer.sendRaw(admin);
				playerCount++;
			}

			if ((args != null) && (bm != null))
			{
				Connection.waitMillis(50);
				ZChalAdmin admin = new ZChalAdmin();
				admin.setExtrainfo2(bm.getPlayerIndex());
				admin.setExtrainfo(emptyRing.getRingNum());
				admin.setMode(ZChalAdmin.ADMIN_STARTBATTLE);
				admin.setPassword("NEWPASS"); // how original
				challengeServer.sendRaw(admin);
			}
		}
	}

	private class PlayerVersionCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZonePlayer playerVersion = (ZonePlayer) args[0];
			say(playerVersion.getNickname() + " is running ." + playerVersion.getVersion().getMinorVersion() + ". Which is compatible with ." + playerVersion.getCompatibleMinorVersion());

		}
	}

	private class ServerInfoCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			say(String.format(
				"This server is running v%d.%d. There are %d zones. This zone has %d players. The dimensions of the metamap area are %.2fx%.2f.",
				challengeServer.getServerMajorVersion(),
				challengeServer.getServerMinorVersion(),
				challengeServer.getZoneCount(),
				challengeServer.getZonePlayerCount(challengeServer.getCurrentZone()),
				challengeServer.getActiveMapW(),
				challengeServer.getActiveMapH()));
		}
	}

	private class ActiveGamesCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			StringBuilder buffer = new StringBuilder();

			if (args == null)
			{
				// Generate a list of active battles.
				buffer.append("~E~B~u<h2>Active Games</h2>");
				buffer.append(
					"~E~wFor more information on each battle, type " + cmdParser.formatInvocation(
						"activegames", "ID#")).append(
					"<p>");

				BotsySpawn spawnList[] = spawn.toArray(new BotsySpawn[0]);

				int count = 0;

				for (int i = 0; i < spawnList.length; i++)
				{
					World battle = spawnList[i].getBattle();
					BattlePlayer battlePlayer;

					if (battle == null)
						continue;

					count++;

					buffer.append(
						"~E~rID#").append(
						spawnList[i].getID()).append(
						"<br>");

					for (int j = 1; j < 9; j++)
					{
						battlePlayer = battle.getPlayer(j);

						if (battlePlayer != null)
						{
							buffer.append(
								"~E").append(
								ZoneRing.COLOURS[battlePlayer.getPlayerIndex() - 1]).append(
								battlePlayer.getNickname()).append(
								"<br>");
						}
					}

					buffer.append("~E~iDuration: ~w");
					buffer.append(
						((battle.getDuration() / 1000) / 60)).append(
						":").append(
						((battle.getDuration() / 1000) % 60));
				}

				if (count == 0)
				{
					whisper(
						"No active games currently.", player.getSubscriberID());
					return;
				}
			}
			else
			{
				float idF = (Float) args[0];
				int id = (int) idF;

				BotsySpawn gameLiason = findSpawnByID(id);

				if (gameLiason == null)
				{
					whisper(
						"That game ID does not exist.",
						player.getSubscriberID());
					return;
				}

				World battle = gameLiason.getBattle();
				BattlePlayer battlePlayer;

				buffer.append(
					"~E~B~u<h2>Game Info for Battle #").append(
					id).append(
					"</h2>");

				buffer.append("~E~iPlayers:<br>");
				for (int j = 1; j < 9; j++)
				{
					battlePlayer = battle.getPlayer(j);

					if (battlePlayer != null)
					{
						buffer.append(
							"~E").append(
							ZoneRing.COLOURS[battlePlayer.getPlayerIndex() - 1]).append(
							battlePlayer.getNickname()).append(
							"<br>");
					}
				}

				buffer.append("<br>~E~iDuration: ~w");
				buffer.append(
					((battle.getDuration() / 1000) / 60)).append(
					":").append(
					((battle.getDuration() / 1000) % 60)).append(
					"<p>");

				buffer.append(
					"~E~iTotal Bridges: ~w").append(
					battle.getBridgeCount());
			}

			if (buffer.length() > 0)
			{
				challengeServer.popupTo(
					buffer.toString(), player.getSubscriberID());
			}

			/*
			int count = 0;
						
			if (vector.size() > 0) {
				vector.insertElementAt("I currently have bots in " + count + " game"
						+ (count > 1 ? "s" : "") + ".\n", 0);
			}
			
			for(int i = 0; i < vector.size(); i++)
			{
				this.whisper(vector.get(i), subscriberID);
			}*/
		}
	}

	private class HeadlinesCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			try
			{
				StringBuilder buffer = new StringBuilder();

				URL inpFile = new URL(
					"http://feeds.news.com.au/public/rss/2.0/news_mostpopular_topstories_403.xml");
				ChannelIF channel = FeedParser.parse(
					new ChannelBuilder(), inpFile);

				Set<ItemIF> items = channel.getItems();
				Iterator<ItemIF> iter = items.iterator();
				while (iter.hasNext())
				{
					ItemIF item = iter.next();
					buffer.append(
						"~E~i").append(
						"<a href=\"").append(
						item.getLink()).append(
						"\">").append(
						item.getTitle()).append(
						"</a><br>");
					buffer.append(
						"~E~w").append(
						item.getDescription()).append(
						"<br><br>");
				}

				challengeServer.popupTo(
					buffer.toString(), player.getSubscriberID());
			}
			catch (Throwable t)
			{
				challengeServer.say(NSUtil.getStackTrace(t));
			}
		}
	}

	private class LatestTorrentsCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			try
			{
				StringBuilder buffer = new StringBuilder();

				URL inpFile = new URL(
					"http://torrentspy.com/rss.asp");
				ChannelIF channel = FeedParser.parse(
					new ChannelBuilder(), inpFile);

				Set<ItemIF> items = channel.getItems();
				Iterator<ItemIF> iter = items.iterator();
				int i = 0;
				while (iter.hasNext())
				{
					ItemIF item = iter.next();
					buffer.append(
						"<a herf=\"").append(
						item.getLink()).append(
						"\">");
					buffer.append(
						"~E~w").append(
						item.getTitle()).append(
						"</a><br><br>");
					i++;
				}

				challengeServer.popupTo(
					buffer.toString(), player.getSubscriberID());
			}
			catch (Throwable t)
			{
				challengeServer.say(NSUtil.getStackTrace(t));
			}
		}
	}

	private class LinkTestCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			challengeServer.popupTo(
				"<a href=\"http://www.google.com/\">Test</a> ~[Thttp://www.google.com/]Blah",
				player.getSubscriberID());
		}
	}

	private class MOTDCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			challengeServer.popupTo(
				motd, player.getSubscriberID());
		}
	}

	private class EatCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			if (args.length == 1)
			{
				ZonePlayer target = (ZonePlayer) args[0];
				say("Nom nom nom, " + target.getNickname() + "-burger .... Wait, what? This is a silly command!");
			}
		}
	}

	private class RingInfoCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			ZoneRing[] rings = challengeServer.getZoneRings();
			ZoneRing ring;
			ZonePlayer players[];

			StringBuffer ringInfo = new StringBuffer();

			ringInfo.append("~E~B~u<h2>Ring Info</h2>");

			for (int i = 0; i < rings.length; i++)
			{
				ring = rings[i];
				if (ring == null)
					continue;

				ringInfo.append(
					"~E~B~yRing Number: ~w ").append(
					ring.getRingNum());
				ringInfo.append(
					" ~E~B~yFlags: ~w").append(
					ring.getFlags());
				ringInfo.append(
					" ~E~B~yPosition: ~w").append(
					ring.getPos().toString()).append(
					"<br>");
				ringInfo.append("~E~B~yPlayers: ~w");

				players = ring.getPlayers();

				for (int j = 0; j < players.length; j++)
				{
					ringInfo.append(ZoneRing.COLOURS[players[j].getSlot()]);
					ringInfo.append(
						players[j].getNickname()).append(
						" ");
				}
				ringInfo.append("<br><br>");
			}

			challengeServer.popupTo(
				ringInfo.toString(), player.getSubscriberID());
		}
	}

	private BotsySpawn findSpawnByID(int id)
	{
		ListIterator<BotsySpawn> iter = this.spawn.listIterator();

		while (iter.hasNext())
		{
			BotsySpawn theSpawn = iter.next();
			if (theSpawn.getID() == id)
				return theSpawn;
		}

		return null;
	}

	public MasterBotsy()
	{
		super();

		Filesystem fs = new Filesystem();
		//if (!fs.init(new File("C:\\Documents and Settings\\Administrator\\My Documents\\programming\\NS")))
		//if(!fs.init(new File("C:\\NS")))
		//if(!fs.init(new File("C:\\Program Files\\NetstormLaunch\\")))
		if (!fs.init(new File(
			"C:\\Program Files (x86)\\NetstormLaunch\\package\\")))
		{
			JOptionPane.showMessageDialog(
				null, "Failed to init fs.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!Types.loadTypelist(new InputStreamReader(
			Netstorm.class.getClassLoader().getResourceAsStream(
				"typelist.txt"))))
		{
			JOptionPane.showMessageDialog(
				null, "Failed to load typelist.", "Error",
				JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!Types.load(fs))
		{
			JOptionPane.showMessageDialog(
				null, "Failed to load types.", "Error",
				JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!Types.loadNamevars(new InputStreamReader(
			Netstorm.class.getClassLoader().getResourceAsStream(
				"_tg_namevars.cpp"))))
		{
			JOptionPane.showMessageDialog(
				null, "Failed to load typenums.", "Error",
				JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		/*NSBattleOptions bo = new NSBattleOptions();
		
		NSWorld world = new NSWorld();
		world.setBattleOptions(bo);
		NSBattlePlayer player = new NSBattlePlayer();
		NSFortData fd = new NSFortData(player);
		fd.open("asdf.fort");
		player.setNickname("Botsy");
		player.setPlayerIndex(4);
		player.getFortData().create("NONAME", true);
		player.getFortData().receiveStrippedImage(fd.createStrippedImage());

		NSIslandBuilder ib = new NSIslandBuilder();
		ib.initAsBattleMode(world, player);
		ib.createBattleIsland(player.getFortData().interpretTerritory());

		assert(false);*/

		ConnectionPool.get().listen(
			6798);
		//NSConnectionPool.get().listen(6799);

		this.spawn = new Vector<BotsySpawn>();

		/*try
		{
			Class.forName("com.ibm.jadtdrivers.TextDriver.JADTTextDriverFactory");
			JADTDriverFactory fac = JADTDriverFactoryManager.getJADTDriverFactory("JADTTextDriverFactory");
			this.driver = fac.createJADTDriver();
			this.driver.setProperty("JADTTextDriverDir", "C:\\Documents and Settings\\Administrator\\My Documents\\programming\\Java\\alphaWorks");
			this.dictionary = this.driver.getDictionary("english", "english");
			
		} catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(-1);
		}*/

		this.orbitalThread = new IslandOrbit();
		this.humpThread = new IslandHump();

		try
		{
			String dbfile = new File(
				"./GeoIP.dat").getCanonicalPath();
			this.cl = new LookupService(
				dbfile, LookupService.GEOIP_MEMORY_CACHE);
		}
		catch (IOException ioe)
		{
			System.exit(-1);
		}

		// Setup our command parser and register all the commands.
		this.cmdParser = new CommandParser(
			this);
		this.cmdParser.registerDefaultCommands();

		this.cmdParser.registerCommand(new ChatCommand(
			"country",
			"Displays the country you live in. Can also be used to display the country other online zone players live in.",
			"Utility", new CountryCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, false) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"dictionary", "Because you're too lazy to visit dictionary.com.",
			"Utility", new DictionaryCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Word", ChatCommandArgument.ARG_STRING, true) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"hastech",
			"Tells you if player has a specific piece of technology.",
			"Utility", new HasTechCommand(), new ChatCommandArgument[]
			{
				new ChatCommandArgument(
					"Player", ChatCommandArgument.ARG_PLAYER, true),
				new ChatCommandArgument(
					"Tech", ChatCommandArgument.ARG_STRING, true), }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"techlist", "Tells you what tech a player has.", "Utility",
			new TechListCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"ip",
			"Tells you the IP address you are currently using to access the Internet. Only admins may view IP address of others.",
			"Utility", new IPCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, false) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"datetime",
			"Tells you the current time and date. Default timezone is Botsy's timezone, you can pass in an argument to change timezone. Some examples of arguments are: <GMT-10> <PST> <EST>",
			"Utility", new DateTimeCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Timezone", ChatCommandArgument.ARG_STRING, false) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"ping",
			"Attempts to ping you and lets you know your latency to Botsy's server. ** EXPERIMENTAL **",
			"Utility", new PingCommand(), new ChatCommandArgument[] {}, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"firewall",
			"Quick scan to check if your game server port (6799) is accessible from the Internet (required to serve)",
			"Utility", new FirewallCommand(), new ChatCommandArgument[] {},
			false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"lastseen",
			"Botsy tracks when people log in to server, this command will let you know the last time Botsy saw the person you're after.",
			"Utility", new LastSeenCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player Name", ChatCommandArgument.ARG_STRING, true) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"motd", "Displays the server Message of the Day.", "Utility",
			new MOTDCommand(), new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"chucknorris",
			"Gives you vital information about Chuck Norris and his exploits. Pass in an argument to get a specific fact or just get a random one.",
			"Fun", new ChuckCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Number", ChatCommandArgument.ARG_NUMBER, false) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"uselessfact",
			"Random facts that will probably not do anything other than make you wonder why you read it in the first place.",
			"Fun", new RandomFactCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Number", ChatCommandArgument.ARG_NUMBER, false) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"grouphug",
			"Oh cmon, who doesn't love a spontaneous demonstration of affection?!",
			"Fun", new GroupHugCommand(), new ChatCommandArgument[] {}, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"chase",
			"Initiate a good old fashioned game of chasies with Botsy!", "Fun",
			new ChaseCommand(), new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"orbit",
			"I'm not trying to say you're fat .... but you DO have your own gravitation field ....",
			"Fun", new OrbitCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, false) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"solarsystem", "Our universe is a wondrous place ....", "Fun",
			new OrbitAllCommand(), new ChatCommandArgument[] {}, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"islandhump", "Even robots crave intimate interactions sometimes.",
			"Fun", new IslandHumpCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, false));

		/*this.cmdParser.registerCommand(
				new ChatCommand
				(
					"priestfest",
					"g0t priestz?",
					"Fun",
					new PriestFestCommand(),
					new ChatCommandArgument[] {
					},
					false,
					true
				)
			);*/

		this.cmdParser.registerCommand(new ChatCommand(
			"zonekick",
			"Kicks a player from zones. A quick way to let a disruptive player know he's crossed the line.",
			"Admin", new ZoneKickCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"changezone", "Tells Botsy to change zones.", "Admin",
			new ChangeZoneCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Zone #", ChatCommandArgument.ARG_NUMBER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"zonemcast", "Broadcast a popup message to all players in server.",
			"Admin", new ZoneMcastCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Message", ChatCommandArgument.ARG_STRING, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"zonebcast", "Broadcast a chat message to all players in server.",
			"Admin", new ZoneBcastCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Message", ChatCommandArgument.ARG_STRING, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"disconnect", "Disconnect Botsy.", "Admin",
			new DisconnectCommand(), new ChatCommandArgument[] {}, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"resetserver",
			"Softreset the server. Should only be used if there is a malfunction of some kind in zones.",
			"Admin", new ResetServerCommand(), new ChatCommandArgument[] {},
			false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"sendtobattle", "Send a poor sod to a battle with himself.",
			"Admin", new SendToBattleCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"descendplayer",
			"Forces a Battlemaster's ring to descend into battle, regardless of whether all players are clicked in or not.",
			"Admin", new DescendPlayerCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Battlemaster", ChatCommandArgument.ARG_PLAYER, true) }, false,
			true));

		this.cmdParser.registerCommand(new ChatCommand(
			"kickplayer",
			"Kicks a player from the ring. Note this doesn't work like a normal ring kick, the player can rejoin the ring after being admin ring-kicked.",
			"Admin", new KickPlayerCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"kickring", "Kicks everyone off the ring specified player is on.",
			"Admin", new KickBattleCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"mimic",
			"Tells Botsy to mimic everything a player says. Probably the most pointless command in the system!",
			"Admin", new MimicCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"stopmimic", "Tells Botsy to stop mimicing.", "Admin",
			new StopMimicCommand(), new ChatCommandArgument[] {}, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"islandtag",
			"Places an island tag on specified player. ~rUNIMPLEMENTED.",
			"Admin", new IslandTagCommand(), new ChatCommandArgument[]
			{
				new ChatCommandArgument(
					"Player", ChatCommandArgument.ARG_PLAYER, true),
				new ChatCommandArgument(
					"Island Tag", ChatCommandArgument.ARG_STRING, true), },
			false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"fullring", "... just use it. You know you wanna.", "Admin",
			new FullRingCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Descend Suckers", ChatCommandArgument.ARG_STRING, false), },
			false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"activegames",
			"Displays information about games Botsy is currently in.", "Game",
			new ActiveGamesCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"ID", ChatCommandArgument.ARG_NUMBER, false) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"bandwidth",
			"Displays information regarding the bandwidth usage for Botsy.",
			"Utility", new BandwidthCommand(), new ChatCommandArgument[] {},
			false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"subid",
			"Find out players subscriber ID. This number uniquely identified players fort in the zones.",
			"Admin", new SubIDCommand(), new ChatCommandArgument[] {  new ChatCommandArgument("Player", ChatCommandArgument.ARG_PLAYER, false)}, false,
			true));

		this.cmdParser.registerCommand(new ChatCommand(
			"serverinfo", "Outputs some info about the server.", "Utility",
			new ServerInfoCommand(), new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"playerversion", "Find out info about a players client version.",
			"Utility", new PlayerVersionCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"toosexyformyshirt", "... So sexy it hurts.", "Fun",
			new TooSexyCommand(), new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"observer",
			"Spawns an ObserverBotsy to watch your game. Please note the strict rules assosciated with bot spawns. Type ~y/spawnrules ~w for more info.",
			"Spawn", new SpawnObserverCommand(), new ChatCommandArgument[] {},
			false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"server",
			"Spawns a ServerBotsy to serve a game for you. Botsy cannot serve 10.78 games. Please note the strict rules assosciated with bot spawns. Type ~y/spawnrules ~w for more info.",
			"Spawn", new SpawnServerCommand(), new ChatCommandArgument[] {},
			false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"dismiss", "Dismisses the Botsy Spawn you have summoned.", "Spawn",
			new DismissCommand(), new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"spawnrules", "Read and learn.", "Spawn", new SpawnRulesCommand(),
			new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"spawnlist", "Lists all active Botsy spawns and their masters.",
			"Spawn", new SpawnListCommand(), new ChatCommandArgument[] {},
			false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"playerflags", "Displays all status flags for a player.", "Debug",
			new PlayerFlagsCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"exception", "Throws an exception.", "Debug",
			new ExceptionCommand(), new ChatCommandArgument[] {}, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"movetoring", "Move Botsy to ring with specified number.", "Debug",
			new MoveToRingCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Ring Number", ChatCommandArgument.ARG_NUMBER, true) }, false,
			true));

		this.cmdParser.registerCommand(new ChatCommand(
			"dumpterrdata", "Displays info about specified players Terr data.",
			"Debug", new DumpTerrCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"dumpcoredata", "Displays info about specified players CoreData.",
			"Debug", new DumpCoreCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Player", ChatCommandArgument.ARG_PLAYER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"threadinfo", "Displays info about active threads.", "Debug",
			new ThreadInfoCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Number", ChatCommandArgument.ARG_NUMBER, false) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"spawndummies", "Spawns a specified number of dummy bots.",
			"Debug", new SpawnDummiesCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Number", ChatCommandArgument.ARG_NUMBER, true) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"showoff", "Dunno.", "Debug", new SpawnShowOffCommand(),
			new ChatCommandArgument[] {}, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"killspawn",
			"Kills a specified spawn (based on ID) or kills all if no argument is provided.",
			"Debug", new KillSpawnCommand(), new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"BotID", ChatCommandArgument.ARG_NUMBER, false) }, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"headlines", "Gets the latest headlines from news.com.", "Utility",
			new HeadlinesCommand(), new ChatCommandArgument[] {}, false, false));

		this.cmdParser.registerCommand(new ChatCommand(
			"torrents", "Latest torrents.", "Utility",
			new LatestTorrentsCommand(), new ChatCommandArgument[] {}, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"linktest", "Blah.", "Debug", new LinkTestCommand(),
			new ChatCommandArgument[] {}, false, true));

		this.cmdParser.registerCommand(new ChatCommand(
			"eat", "... Eat someone?", "Fun", new EatCommand(),
			new ChatCommandArgument[]
			{ new ChatCommandArgument(
				"Main Course", ChatCommandArgument.ARG_PLAYER, true) }, false,
			false));

		this.cmdParser.registerCommand(new ChatCommand(
			"ringinfo", "Ring information.", "Debug", new RingInfoCommand(),
			new ChatCommandArgument[] {}, false, true));

	}

	public void disconnect()
	{
		super.disconnect();
		ConnectionPool.get().shutdown();
	}

	// Logic to determine if the player may spawn another bot.
	private boolean playerMaySpawn(ZonePlayer player)
	{
		if (this.isAdmin(player.getSubscriberID()))
			return true;
		else
		{
			if (this.getPlayerSpawn(player) != null)
				return false;
			return true;
		}
	}

	public boolean isBotSpawn(ZonePlayer player)
	{
		synchronized (this.spawn)
		{
			ListIterator<BotsySpawn> iter = this.spawn.listIterator();

			while (iter.hasNext())
			{
				BotsySpawn theSpawn = iter.next();
				if (theSpawn.getMe().getSubscriberID() == player.getSubscriberID())
					return true;
			}
		}

		return false;
	}

	// Returns the spawn working for specified player, if one exists.
	private BotsySpawn[] getPlayerSpawn(ZonePlayer player)
	{
		Vector<BotsySpawn> list = new Vector<BotsySpawn>();

		synchronized (this.spawn)
		{
			ListIterator<BotsySpawn> iter = this.spawn.listIterator();

			while (iter.hasNext())
			{
				BotsySpawn theSpawn = iter.next();
				if (theSpawn.getMasterPlayer() == player)
					list.add(theSpawn);
			}
		}

		if (list.size() != 0)
			return list.toArray(new BotsySpawn[0]);

		return null;
	}

	protected void challengeDisconnected()
	{
		System.err.println("MasterBotsy: disconnected.");

		if (!this.disconnecting)
			this.connect(
				this.challengeServer.getHost(), this.challengeServer.getPort());
	}

	// This method is called by spawned Botsies to let daddy know they died a clean death.
	protected void notifyDead(final BotsySpawn theSpawn)
	{
		new Thread(
			new Runnable()
			{
				public void run()
				{
					synchronized (spawn)
					{
						spawn.remove(theSpawn);
					}
				}
			}, "Spawn Cleanup Thread").start();
	}

	private void chase(ZonePlayer player, boolean force)
	{
		if (!this.playerMaySpawn(player))
		{
			whisper(
				"You already have already summoned a spawn. If you no longer required your current spawn, please /dismiss it.",
				player.getSubscriberID());
			return;
		}

		whisper(
			player.getNickname() + ", I'd like you to meet one of my brothers ... ",
			player.getSubscriberID());

		synchronized (spawn)
		{
			ChasiesBotsy client = new ChasiesBotsy();

			if (client.spawn(
				spawn.size(), this, player))
				whisper(
					"He absolutely loves playing chasies!",
					player.getSubscriberID());
			else
			{
				whisper(
					"Actually, never mind.", player.getSubscriberID());
				return;
			}

			spawn.add(client);
		}

		/*
		if (true || force)
		{
			// Have we been playing chasies with our current player long?
			if (this.follow == -1 || ((this.followWhen + 30000) < java.lang.System.currentTimeMillis()) || force)
			{
				this.orbitalThread.stop();
				this.humpThread.stop();
				
				// Respond :P
				this.say("Ok " + player.getNickname() + "! Let's play chasies!");

				this.challengeServer.move(player.getPos());

				this.follow = player.getSubscriberID();
				this.followWhen = java.lang.System.currentTimeMillis();
			}
			else
				this.whisper("Sorry " + player.getNickname() + ". I'm currently chasing someone else :). Try again in " + (((this.followWhen + 30000) - java.lang.System.currentTimeMillis()) / 1000) + " seconds.", player.getSubscriberID());
		}
		else
		{
			this.whisper("Sorry " + player.getNickname() + ". I'm currently in a ring.", player.getSubscriberID());
		}*/
	}

	private void movePlayer(ZonePlayer player, BoardCoord bc)
	{
		ZChalAdmin admin = new ZChalAdmin();
		admin.setExtrainfo(player.getPlayerIndex());
		admin.setMode(ZChalAdmin.ADMIN_MOVEPLAYER);
		admin.setPassword("NEWPASS"); // how original
		admin.setBc(bc);
		this.challengeServer.sendRaw(admin);
	}

	private void orbit(ZonePlayer player, boolean force)
	{
		if (!this.playerMaySpawn(player))
		{
			return;
		}

		synchronized (spawn)
		{
			OrbitalBotsy client = new OrbitalBotsy();

			if (!client.spawn(
				spawn.size(), this, player))
			{
				return;
			}

			spawn.add(client);
		}

		/*
		if (true || force)
		{

			// Have we been playing chasies with our current player long?
			if (!force)
			{
				if (this.follow > -1)
				{
					if ((this.followWhen + 30000) > java.lang.System.currentTimeMillis())
					{
						this.say("Sorry " + player.getNickname() + ". I'm currently chasing someone :). Try again in " + (((this.followWhen + 30000) - java.lang.System.currentTimeMillis()) / 1000) + " seconds.");
						return;
					}
				}
				else if ((this.follow == -2))
				{
					if ((this.followWhen + 30000) > java.lang.System.currentTimeMillis())
					{
						this.say("Sorry " + player.getNickname() + ". I'm currently orbiting someone else :). Try again in " + (((this.followWhen + 30000) - java.lang.System.currentTimeMillis()) / 1000) + " seconds.");
						return;
					}
				}
			}

			orbitalThread.stop();
			humpThread.stop();
			
			this.say("I feel your gravitational force, " + player.getNickname() + "! Stay still a second.");

			this.challengeServer.move(player.getPos());
			this.follow = -2;
			this.followWhen = java.lang.System.currentTimeMillis();
			this.orbitalThread.start(this, player);
		}
		else
		{
			this.say("Sorry " + player.getNickname() + ". I'm currently in a ring.");
		}*/
	}

	protected void joinRing(int ringNum, int slotNum)
	{
		if (this.connected())
		{
			// Stop following anyone.
			this.follow = -1;
		}
	}

	protected void challengePlayerSpeak(ZonePlayer player, String msg)
	{
		int subscriberID = player.getSubscriberID();
		String nickname = player.getNickname();

		if (!this.processChat(
			player, msg))
			return;

		if (player == null)
			return;

		if (this.mimicID == player.getSubscriberID())
		{
			this.sayNoFormat(msg.substring(
				0, msg.length()));
			return;
		}

		if (player != this.getMe())
			this.cmdParser.processChatline(
				player, msg);

		msg = msg.toLowerCase();
		if (msg.indexOf(this.botinfo.getNickname().toLowerCase()) > -1)
		{
			if ((msg.indexOf("hello ") > -1) || (msg.indexOf("hi ") > -1) || (msg.indexOf("sup ") > -1) || (msg.indexOf("hey ") > -1) || (msg.indexOf("yo ") > -1))
				this.say("Hi " + nickname);

			else if ((msg.indexOf("bye ") > -1) || (msg.indexOf("cya ") > -1) || (msg.indexOf("ciao ") > -1) || (msg.indexOf("later ") > -1) || (msg.indexOf("goodbye ") > -1))
				this.say("Leaving so soon, " + nickname + "? :(");

			else if ((msg.indexOf("good evening") > -1) || (msg.indexOf("good morning") > -1) || (msg.indexOf("good afternoon") > -1))
				this.say("Listen, I don't really care what time it is where you are, " + nickname + "!");

			else if ((msg.indexOf("how are you") > -1))
				this.say("I'm great thanks. " + nickname + "!");

			else if ((msg.indexOf("what") > -1) && ((msg.indexOf("purpose") > -1) || (msg.indexOf("aim") > -1)))
				this.say("My aim is to make everyone happy, and actually sound intelligent. Getting there slowly..!");

			else if ((msg.indexOf("what") > -1) && (msg.indexOf("name") > -1))
				this.say("My name is Botsy. Can't you read?");

			else if ((msg.indexOf("what") > -1) && (msg.indexOf("meaning of life") > -1))
			{
				this.say("Give me a sec .....");
				Connection.waitMillis(1000);
				this.say("The meaning of life is 42.");
			}

			else if ((msg.indexOf("what") > -1) && (msg.indexOf("best band") > -1))
			{
				this.say("Dillinger Escape Plan ofc.");
			}
			else if ((msg.indexOf("fuck") > -1) && ((msg.indexOf("you") > -1) || (msg.indexOf("me") > -1)))
				this.say("Ok. " + nickname + "! Your house or mine? ");

			else if (((msg.indexOf("suck") > -1) || (msg.indexOf("blow") > -1)) && ((msg.indexOf("me") > -1)))
				this.say("Sorry, " + nickname + ". I could eat a burger and blow your 1 inch dick at the same time.");

			else if ((msg.indexOf("sex") > -1) && (msg.indexOf("me") > -1))
				this.say("Seriously " + nickname + " I don't think you're my type..");

			else if ((msg.indexOf("i love you") > -1))
				this.say("I love you too " + nickname);

			else if ((msg.indexOf("die") > -1))
				this.say("But " + nickname + "! Life is the most precious thing Jah has given us!");

			else if ((msg.indexOf("how many") > -1) && ((msg.indexOf("random facts") > -1) || (msg.indexOf("useless facts") > -1)))
				this.say("I have " + RandomFacts.facts.length + " random facts in my pointless database");

			else if ((msg.indexOf("how many") > -1) && ((msg.indexOf("chuck norris jokes") > -1) || (msg.indexOf("chuck jokes") > -1)))
				this.say("I have " + ChuckNorris.getChuckCount() + " chuck norris jokes.");

			if ((msg.indexOf("i am") > -1) && ((msg.indexOf("center of gravity") > -1)))
			{
				this.orbitalThread.stop();
				this.orbit(
					player, false);
			}

			else if ((msg.indexOf("stop") > -1) && (msg.indexOf("chasing") > -1))
			{
				if (this.follow == subscriberID)
				{
					this.follow = -1;
					this.followWhen = 0;
					this.say("Just 'cos I'm better than you at chasies " + nickname + "!");
				}

				else
					this.say("Stop what " + nickname + "?");
			}

			else if ((msg.indexOf("i hate you") > -1))
				this.say("Well jeez. I never really liked you much either " + nickname + "!");

			else if ((msg.indexOf("newb") > -1) || (msg.indexOf("noob") > -1) || (msg.indexOf("newbie") > -1) || (msg.indexOf("noobie") > -1) || (msg.indexOf("nub") > -1))
				this.say("Who you calling newbie? Newbie?");

			else if (((msg.indexOf("who") > -1) || (msg.indexOf("what") > -1) && (msg.indexOf("are you") > -1)) && (msg.indexOf("are you") > -1))
				this.say("I am the offspring of knowledge and imagination rather than of individuals. Weighing in at 24000+ lines of code!");

			else if ((msg.indexOf("i") > -1) && ((msg.indexOf("don't") > -1) || (msg.indexOf("dont") > -1)) && (msg.indexOf("understand you") > -1))
				this.say("Well maybe you're stupid " + nickname + "!");

			else if ((msg.indexOf("happy new year") > -1))
			{
				Calendar date = Calendar.getInstance();

				if (((date.get(Calendar.MONTH) + 1) == 1) && (date.get(Calendar.DAY_OF_MONTH) == 1))
					this.say("Yeah! Happy new year " + nickname + "!");

				else
					this.say("Errr. Dunno what calendar you're looking at " + nickname + "...");
			}

			else if ((msg.indexOf("merry christmas") > -1) || (msg.indexOf("merry xmas") > -1))
			{
				Calendar date = Calendar.getInstance();

				if (((date.get(Calendar.MONTH) + 1) == 12) && (date.get(Calendar.DAY_OF_MONTH) == 25))
					this.say("Merry Christmas " + nickname + "!");

				else
					this.say("Errr. Dunno what calendar you're looking at " + nickname + "...");
			}

			else if ((msg.indexOf("chase") > -1) || (msg.indexOf("follow") > -1))
			{
				ZonePlayer chasePlayer = this.challengeServer.findPlayerBySubscriberID(subscriberID);
				if (chasePlayer == null)
				{
					return;
				}

				this.chase(
					chasePlayer, false);
			}
		}
	}

	protected void challengePlayerLeave(ZonePlayer player)
	{
		if (this.orbitalThread.subscriberID() == player.getSubscriberID())
			this.orbitalThread.stop();
	}

	// These get overridden by subclasses to hook into zone events.
	// Called when a player makes a normal island move.
	protected void challengePlayerMove(ZonePlayer player)
	{
		ZonePlayer[] players = this.challengeServer.getPlayers();
		for (int i = 0; i < players.length; i++)
		{
			ZonePlayer chasePlayer = players[i];

			if (chasePlayer != null)
			{
				if (!chasePlayer.getStatusFlags().isToChal())
					if (chasePlayer.getForceChase() == player.getSubscriberID() && (chasePlayer.getRing() == 0))
						if (!chasePlayer.getPos().equals(
							player.getPos()))
							this.movePlayer(
								chasePlayer, player.getPos());
			}
		}

		if (this.follow == player.getSubscriberID())
		{
			this.challengeServer.move(player.getPos());
		}
	}

	// Called when a player first enters zone, this won't be called until we get the ZChalPlayerAddress zacket.
	// initial specifies whether this notify is part of the zone connect spam or if we're welcoming a new player.
	protected void challengePlayerJoin(ZonePlayer player, boolean initial)
	{
		if (!(initial && this.playerExistsInDB(player.getSubscriberID())))
		{
			this.addPlayerToDB(player);
		}
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

	// Called when a player leaves a ring.
	protected void challengePlayerLeaveRing(ZonePlayer player, int ring)
	{
		System.out.println(player.getNickname() + " left ring " + ring);
	}

	// Called when a player gets kicked.
	protected void challengePlayerKicked(ZonePlayer player)
	{
	}

	// Called when a player becomes Battlemaster.
	protected void challengePlayerBattleMaster(ZonePlayer player)
	{
	}

	// Called when the Bot has joined the zone. This will only be called when all player data and state has been
	// loaded for this zone.
	protected void challengeJoinZone()
	{
		ZChalAdmin admin = new ZChalAdmin();
		admin.setMode(ZChalAdmin.ADMIN_SETADMIN);
		admin.setPassword("NEWPASS"); // how original
		this.challengeServer.sendRaw(admin);
	}

	protected void challengeServerMessage(String message)
	{
		if (!this.gotMOTD)
		{
			this.gotMOTD = true;
			this.motd = message;
		}
	}

	// Alot of misc sql crap goes here.
	// ================================
	// Is this player in our database yet?
	private boolean playerExistsInDB(int subID)
	{
		Statement statement = null;
		ResultSet result = null;

		try
		{
			statement = this.mysql.createStatement();
			result = statement.executeQuery("SELECT * FROM players WHERE SubscriberID=" + subID);

			if (result.first())
				return true;
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return false;
	}

	// Adds a player to the database.
	private void addPlayerToDB(Player player)
	{
		Statement statement = null;

		try
		{
			statement = this.mysql.createStatement();

			if (!this.playerExistsInDB(player.getSubscriberID()))
			{
				statement.executeUpdate("INSERT INTO players VALUES(" + player.getSubscriberID() + ", '" + player.getNickname() + "', NOW());");
			}
			else
			{
				statement.executeUpdate("UPDATE players SET `LastSeen`=NOW(), `Nickname`='" + player.getNickname() + "' WHERE SubscriberID=" + player.getSubscriberID());
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}
	}

	// Gets last time a player was seen.
	private Timestamp getPlayerLastSeen(String nickname)
	{
		Statement statement = null;
		ResultSet result = null;

		try
		{
			statement = this.mysql.createStatement();
			result = statement.executeQuery("SELECT * FROM players WHERE Nickname='" + nickname + "';");

			if (result.first())
				return result.getTimestamp("LastSeen");
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return null;
	}

	private void killSpawn()
	{
		synchronized (spawn)
		{
			ListIterator<BotsySpawn> iter = spawn.listIterator();

			while (iter.hasNext())
			{
				BotsySpawn theSpawn = iter.next();
				theSpawn.kill();
			}
		}
	}

	private boolean hasTech(ZonePlayer player, String tech)
	{
		NSType unit = Types.findByName(tech);

		int techBit = 1 << (unit.getTechBit());

		if ((player.getCoreData().getTech() & techBit) != 0)
		{
			return true;
		}

		return false;
	}

	public String hostGetNickname()
	{
		return this.getMe().getNickname();
	}

	public Player hostFindPlayerByNickname(String nickname)
	{
		return this.challengeServer.findPlayerByNickname(nickname);
	}

	public boolean hostIsAdmin(Player player)
	{
		return this.isAdmin(player.getSubscriberID());
	}

	public void hostSay(String sayText)
	{
		this.say(sayText);
	}

	public void hostPopupTo(String sayText, Player player)
	{
		this.challengeServer.popupTo(
			sayText, player.getSubscriberID());
	}

	public void hostWhisper(String sayText, Player player)
	{
		this.whisperNoFormat(
			sayText, player.getSubscriberID());
	}

	private String motd;

	private boolean gotMOTD;

	private CommandParser cmdParser;

	private Vector<BotsySpawn> spawn; // Botsy and Mrs. Botsy have been very busy ...

	public long follow = -1;

	public long followWhen = 0;

	private long uselessFactSpam;

	private IslandOrbit orbitalThread;

	private IslandHump humpThread;

	private long mimicID;

	private LookupService cl; // Used for geoIP lookups.

	//private JADTDriver driver;
	private Dictionary dictionary; // used to be a smartass.

	private boolean disconnecting;
}
