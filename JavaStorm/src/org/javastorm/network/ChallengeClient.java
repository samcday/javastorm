package org.javastorm.network;

import org.javastorm.BoardCoord;
import org.javastorm.NSUtil;
import org.javastorm.battle.BattleOptions;
import org.javastorm.challenge.ZonePlayer;
import org.javastorm.challenge.ZoneRing;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZBattleOptions;
import org.javastorm.network.commands.ZChalAdmin;
import org.javastorm.network.commands.ZChalBattleAdd;
import org.javastorm.network.commands.ZChalBattleUpdate;
import org.javastorm.network.commands.ZChalLaunchClient;
import org.javastorm.network.commands.ZChalLaunchServer;
import org.javastorm.network.commands.ZChalLoginReply;
import org.javastorm.network.commands.ZChalPlayerAdd;
import org.javastorm.network.commands.ZChalPlayerAddress;
import org.javastorm.network.commands.ZChalPlayerDel;
import org.javastorm.network.commands.ZChalPlayerUpdate;
import org.javastorm.network.commands.ZChalRequestChangeStatus;
import org.javastorm.network.commands.ZChalRequestLogin;
import org.javastorm.network.commands.ZChalRequestSailTo;
import org.javastorm.network.commands.ZChalServerInfo;
import org.javastorm.network.commands.ZChatLine;
import org.javastorm.network.commands.ZRootQuery;
import org.javastorm.network.commands.ZRootReplyQuery;

// This is our entry point to join the zones.
public class ChallengeClient implements CommandListener
{
	public ChallengeClient(ChallengeListener eventSubscriber)
	{
		this.eventSubscriber = eventSubscriber;

		this.processor = new CommandProcessor();

		this.zoneRings = new ZoneRing[ZoneRing.MAX_RINGS];
		this.challengeServer = new Connection();
	}

	// Returns whether we are connected or not.
	public boolean connected()
	{
		return this.challengeServer.connected();
	}

	public String getHost()
	{
		return this.host;
	}

	public int getPort()
	{
		return this.port;
	}

	public boolean connect(ZonePlayer player, ChallengeClient existing)
	{
		this.zoneCount = existing.getZoneCount();
		this.zonePlayers = new int[zoneCount + 1];

		this.serverMajorVersion = existing.getServerMajorVersion();
		this.serverMinorVersion = existing.getServerMinorVersion();

		this.port = existing.getPort();
		this.host = existing.getHost();
		this.initPlayer = player;

		int zone = existing.getCurrentZone();

		return this.joinZone(zone);
	}

	public boolean connect(ZonePlayer player, ChallengeClient existing, int zone)
	{
		this.zoneCount = existing.getZoneCount();
		this.zonePlayers = new int[zoneCount + 1];

		this.serverMajorVersion = existing.getServerMajorVersion();
		this.serverMinorVersion = existing.getServerMinorVersion();

		this.port = existing.getPort();
		this.host = existing.getHost();
		this.initPlayer = player;

		return this.joinZone(zone);
	}

	//	Connects and authenticates with the specified Netstorm server.
	public boolean connect(ZonePlayer player, String host, int port)
	{
		this.initPlayer = player;

		this.port = port;
		this.host = host;

		// Connect to the specified host.
		int zone = 1;

		Connection root = new Connection();
		if (!root.connect(this.host, this.port, 5000))
		{
			System.err.println("Could not connect to rootserver " + this.host + " on port " + this.port + ".");
			return false;
		}

		// Connect to the root server and authenticate.
		ZRootQuery rootQuery = new ZRootQuery();
		rootQuery.setLevel(player.getCoreData().getLevel());
		root.sendCommand(rootQuery);

		ZRootReplyQuery reply = (ZRootReplyQuery) root.readCommand(true);

		this.serverMajorVersion = reply.getServerMajorVersion();
		this.serverMinorVersion = reply.getServerMinorVersion();

		zone = reply.getZone();

		this.zoneCount = reply.getZones().length;

		this.zonePlayers = new int[zoneCount + 1];

		// We're done with the root server.
		root.disconnect();

		if (!this.joinZone(zone))
		{
			System.err.println("Could not join zone number " + zone);
			return false;
		}

		return true;
	}

	// Disconnects from the server.
	public void disconnect()
	{
		if (this.connected())
		{
			this.challengeServer.disconnect();
		}
	}

	// Admin commands.

	// Moves another player to specified boardcoord pos.
	public void movePlayer(ZonePlayer player, BoardCoord bc)
	{
		ZChalAdmin admin = new ZChalAdmin();
		admin.setExtrainfo(player.getPlayerIndex());
		admin.setMode(ZChalAdmin.ADMIN_MOVEPLAYER);
		admin.setPassword("NEWPASS"); // how original
		admin.setSailToBattle(0);
		admin.setBc(bc);
		challengeServer.sendCommand(admin);
	}

	//	Moves bots island to specified x and y co-ords.
	public void move(BoardCoord bc)
	{
		if (this.connected())
		{
			ZChalRequestSailTo move = new ZChalRequestSailTo();
			move.setPos(bc);

			this.challengeServer.sendCommand(move);
		}
	}

	public ZonePlayer[] getPlayers()
	{
		ZonePlayer[] players = new ZonePlayer[this.zonePlayers[this.zoneCurrent]];
		int index = 0;

		for (int i = 0; i < this.zoneCurrentPlayers.length; i++)
		{
			if (this.zoneCurrentPlayers[i] != null)
				players[index++] = this.zoneCurrentPlayers[i];
		}
		return players;
	}

	public int getZoneCount()
	{
		return this.zoneCount;
	}

	public int getZonePlayerCount(int zone)
	{
		return this.zonePlayers[zone];
	}

	public int getCurrentZone()
	{
		return this.zoneCurrent;
	}

	public ZoneRing[] getZoneRings()
	{
		return this.zoneRings;
	}

	// Forces join of specified zone, instead of taking root server's hint.
	public void forceZone(int zone)
	{
		this.forceZone = zone;
	}

	// Sends my flags.
	private void sendStatusFlags()
	{
		ZChalRequestChangeStatus zcrcs = new ZChalRequestChangeStatus();
		zcrcs.setSubscriberID(this.me.getSubscriberID());
		zcrcs.setStatusFlags(this.me.getStatusFlags());
		this.challengeServer.sendCommand(zcrcs);
	}

	// Sends a raw command.
	public void sendRaw(NSCommand command)
	{
		this.challengeServer.sendCommand(command);
	}

	// Sends a raw command.
	public void sendRaws(NSCommand[] command)
	{
		this.challengeServer.sendCommands(command);
	}

	// Joins the specified zone number. Cleans up old connection if we're switching zones.
	public boolean joinZone(int zonenum)
	{
		if (this.forceZone > 0)
		{
			zonenum = this.forceZone;
		}

		this.zoneCurrent = zonenum;

		// Is there already a connection open?
		if (this.challengeServer.connected())
		{
			// Strange, the island fall animation doesn't fire unless we're moving when we dc to change zones.
			this.move(new BoardCoord(30, 30));

			this.me.getStatusFlags().setToChal(true);
			this.sendStatusFlags();

			Connection.waitMillis(1000);

			this.disconnect();
		}

		this.challengeServer = new Connection();
		this.challengeServer.connect(this.host, this.port, 5000);

		// Login to challenge server.
		ZChalRequestLogin chalLogin = new ZChalRequestLogin();
		chalLogin.setClientMajorVersion(10);
		chalLogin.setClientMinorVersion(this.initPlayer.getVersion().getMinorVersion());
		chalLogin.setCompatibleMinorVersion(this.initPlayer.getCompatibleMinorVersion());
		chalLogin.setLanguageId(0);
		chalLogin.setSubscriberID(this.initPlayer.getSubscriberID());
		chalLogin.set_flags(0);
		chalLogin.setChalStateId(zonenum);
		chalLogin.setTerr(this.initPlayer.getTerrain());
		chalLogin.setPlayerData(this.initPlayer.getCoreData());
		chalLogin.getStatusFlags().copy(this.initPlayer.getStatusFlags());
		chalLogin.setName(this.initPlayer.getNickname());
		this.challengeServer.sendCommand(chalLogin);

		// Block for the reply.
		ZChalLoginReply chalReply = (ZChalLoginReply) this.challengeServer.readCommand(true);

		// If it's anything but true, then we've failed.
		if (chalReply.getCode() != ZChalLoginReply.CLR_TRUE)
			return false;

		// Initialize player list.
		this.zoneCurrentPlayers = new ZonePlayer[255];

		for (int j = 0; j < this.zoneCurrentPlayers.length; j++)
			this.zoneCurrentPlayers[j] = null;

		this.inZone = false;

		// Success. Start running as a Thread to process any updates.
		this.processor.start(this.challengeServer, this);
		return true;
	}

	// Kicks a player off ring.
	public void kickOff(int subscriberID)
	{
		ZChalRequestChangeStatus kick = new ZChalRequestChangeStatus();
		kick.setSubscriberID(subscriberID);
		kick.getStatusFlags().setKicked(true);
		this.challengeServer.sendCommand(kick);
	}

	// Commands bot to whisper a supplied subscriberID
	public void whisper(String sayText, int subscriberID)
	{
		ZChatLine say = new ZChatLine();

		say.setSubscriberID(this.initPlayer.getSubscriberID());
		say.setMessage(sayText);
		say.setNickname(this.initPlayer.getNickname());
		say.addDestinationSubscriberID(subscriberID);

		this.challengeServer.sendCommand(say);
	}

	//	Commands bot to speak in zone.
	public void say(String sayText)
	{
		if (this.connected() && this.inZone == true)
		{
			ZChatLine say = new ZChatLine();
			say.setSubscriberID(this.initPlayer.getSubscriberID());
			say.setMessage(sayText);
			say.setNickname(this.initPlayer.getNickname());

			for (int i = 0; i < this.zoneCurrentPlayers.length; i++)
			{
				if (this.zoneCurrentPlayers[i] != null)
				{
					say.addDestinationSubscriberID(this.zoneCurrentPlayers[i].getSubscriberID());
				}
			}

			this.challengeServer.sendCommand(say);
		}
	}

	// Sends a popup message to specific player.
	public void popupTo(String sayText, int subID)
	{
		ZChatLine say = new ZChatLine();
		say.setMessage(sayText);
		say.setEncoding(ZChatLine.CHAT_ASCII | ZChatLine.CHAT_POPUP);
		say.setSubscriberID(0);
		say.setNickname("");
		say.addDestinationSubscriberID(subID);
		this.challengeServer.sendCommand(say);
	}

	// Returns the ring we are sitting on.
	public ZoneRing getRing()
	{
		return this.ring;
	}

	// Convenience method to find a empty ring in zone.
	public ZoneRing findEmptyRing()
	{
		for (int i = 0; i < this.zoneRings.length; i++)
		{
			if (this.zoneRings[i] != null && this.zoneRings[i].isEmpty())
			{
				return this.zoneRings[i];
			}
		}

		return null;
	}

	public ZoneRing getZoneRing(int ringNum)
	{
		return this.zoneRings[ringNum - 1];
	}

	public void joinRing(int ringNum, int slotNum)
	{
		if (this.connected())
		{
			ZChalRequestSailTo joinRing = new ZChalRequestSailTo();
			joinRing.setRing(ringNum);
			joinRing.setSlot(slotNum);
			this.challengeServer.sendCommand(joinRing);
		}
	}

	public void changeRingDescription(ZoneRing ring, String desc)
	{
		ZChalBattleUpdate zcbu = new ZChalBattleUpdate();
		zcbu.setRing(ring.getRingNum());
		zcbu.setExtra(0);
		zcbu.setDesc(desc);
		this.sendRaw(zcbu);
	}

	public void clickIn()
	{
		if (this.connected())
		{
			this.me.getStatusFlags().setAccepted(true);
			this.sendStatusFlags();
		}
	}

	public void clickOut()
	{
		if (this.connected())
		{
			this.me.getStatusFlags().setAccepted(false);
			this.sendStatusFlags();
		}
	}

	// Tells the Challenge Server we connected to Battlemaster just fine. This is what makes your little box
	// colours in nicely.
	public void acceptConnection()
	{
		this.me.getStatusFlags().setConnected(true);
		this.sendStatusFlags();
	}

	// Updates the Battle Options for the ring we're serving.
	public void sendBattleOptions(BattleOptions bo)
	{
		ZBattleOptions update = new ZBattleOptions();
		update.setBattleOptions(bo);
		update.setRing(this.getRing().getRingNum());
		this.challengeServer.sendCommand(update);
	}

	public ZonePlayer findPlayerBySubscriberID(int subscriberID)
	{
		// Find the pIndex.
		for (int i = 0; i < this.zoneCurrentPlayers.length; i++)
		{
			if (this.zoneCurrentPlayers[i] != null)
			{
				if (this.zoneCurrentPlayers[i].getSubscriberID() == subscriberID)
				{
					return this.zoneCurrentPlayers[i];
				}
			}
		}

		return null;
	}

	public ZonePlayer findPlayerByPlayerID(int playerID)
	{
		// Find the pIndex.
		for (int i = 0; i < this.zoneCurrentPlayers.length; i++)
		{
			if (this.zoneCurrentPlayers[i] != null)
			{
				if (this.zoneCurrentPlayers[i].getPlayerIndex() == playerID)
				{
					return this.zoneCurrentPlayers[i];
				}
			}
		}

		return null;
	}

	public ZonePlayer findPlayerByNickname(String nickname)
	{
		// Find the pIndex.
		for (int i = 0; i < this.zoneCurrentPlayers.length; i++)
		{
			if (this.zoneCurrentPlayers[i] != null)
			{
				if (this.zoneCurrentPlayers[i].getNickname().equalsIgnoreCase(nickname))
				{
					return this.zoneCurrentPlayers[i];
				}
			}
		}

		return null;
	}

	public void startGame()
	{
		if (this.connected())
		{
			this.me.getStatusFlags().setLaunch(true);
			this.sendStatusFlags();
		}
	}

	public int getServerMajorVersion()
	{
		return serverMajorVersion;
	}

	public void setServerMajorVersion(int serverMajorVersion)
	{
		this.serverMajorVersion = serverMajorVersion;
	}

	public int getServerMinorVersion()
	{
		return serverMinorVersion;
	}

	public void setServerMinorVersion(int serverMinorVersion)
	{
		this.serverMinorVersion = serverMinorVersion;
	}

	public BandwidthMonitor getBandwidthMonitor()
	{
		return this.challengeServer.getBandwidthMonitor();
	}

	public void onConnectionDead()
	{
		if (this.eventSubscriber != null)
			this.eventSubscriber.challengeDisconnected();
	}

	public void onException(Throwable t)
	{
		this.say("Shit. Gone and crashed.");
		this.say(NSUtil.getStackTrace(t));
	}

	// Processes commands from server.
	public void onCommand(NSCommand command)
	{
		// Information about the zone.
		if (command.getCommandID() == Connection.ZChalServerInfo)
		{
			ZChalServerInfo info = (ZChalServerInfo) command;

			this.activeMapW = info.getActiveMapW();
			this.activeMapH = info.getActiveMapH();
			if (info.getContact().length > 0)
				this.chalCenter = new BoardCoord(info.getContact()[0], info.getContact()[1]);
		}

		// Player spoke.
		else if (command.getCommandID() == Connection.ZChatLine)
		{
			ZChatLine chat = (ZChatLine) command;

			// It's too risky to process chat lines when our zone state is not completely loaded. Just drop them.
			if (!this.inZone)
				return;

			if (!chat.isServerMessage())
			{
				ZonePlayer player = this.findPlayerBySubscriberID(chat.getSubscriberID());
				if (player == null)
					return;

				this.eventSubscriber.challengePlayerSpeak(player, chat.getMessage());
			}
			else
			{
				// TODO PROCESS SERVER CHAT
				this.eventSubscriber.challengeServerMessage(chat.getMessage());
			}
		}

		// Player joined.
		else if (command.getCommandID() == Connection.ZChalPlayerAdd)
		{
			ZChalPlayerAdd commandPlayerJoin = (ZChalPlayerAdd) command;
			ZonePlayer player = commandPlayerJoin.getNSPlayer();
			if (player == null)
				return;

			// Create this player in our list.
			this.zoneCurrentPlayers[commandPlayerJoin.getNSPlayer().getPlayerIndex()] = player;
			this.zonePlayers[this.zoneCurrent]++;

			// Mmmm since this is initial player data, I'm just gonna trust it to be valid.
			if (player.getRing() > 0)
				this.zoneRings[player.getRing() - 1].addPlayer(player.getSlot(), player);

			if (player.getSubscriberID() == this.initPlayer.getSubscriberID())
			{
				this.me = player;
				// We always get our ZChalPlayerAdd sent to us last. We can assume we're "in the zone" now.
				this.inZone = true;
				this.eventSubscriber.challengeJoinZone();
			}
		}

		// Player left.
		else if (command.getCommandID() == Connection.ZChalPlayerDel)
		{
			ZChalPlayerDel del = (ZChalPlayerDel) command;
			int[] ids = del.getPlayerIDs();

			for (int i = 0; i < del.getCount(); i++)
			{
				ZonePlayer player = this.zoneCurrentPlayers[ids[i]];
				if (player == null)
					continue;

				this.eventSubscriber.challengePlayerLeave(player);

				if (player.getStatusFlags().isToBattle())
					this.eventSubscriber.challengePlayerBattle(player);
				else if (player.getStatusFlags().isToChal())
					this.eventSubscriber.challengePlayerChangeZone(player);
				else
					this.eventSubscriber.challengePlayerDisconnect(player);

				this.zonePlayers[this.zoneCurrent]--;
				this.zoneCurrentPlayers[ids[i]] = null;
			}
		}

		// We get the IP address of a player seperate to the main packet, no clue why really ....
		else if (command.getCommandID() == Connection.ZChalPlayerAddress)
		{
			ZChalPlayerAddress commandPlayerInfo = (ZChalPlayerAddress) command;
			int playerIndex = commandPlayerInfo.getPlayerIndex();
			ZonePlayer player = this.findPlayerByPlayerID(playerIndex);
			if (player == null)
				return;

			commandPlayerInfo.updatePlayer(this.zoneCurrentPlayers[playerIndex]);

			boolean initial = false;
			if (player.getStatusFlags().isInitial())
			{
				initial = true;
				player.getStatusFlags().setInitial(false);
			}

			this.eventSubscriber.challengePlayerJoin(player, initial);
		}

		// New ring approaching.
		else if (command.getCommandID() == Connection.ZChalBattleAdd)
		{
			ZChalBattleAdd zcba = (ZChalBattleAdd) command;

			ZoneRing newRing = new ZoneRing(zcba.getRingNumber());
			newRing.setPos(zcba.getPos());
			newRing.setFlags(zcba.getFlags());

			this.zoneRings[zcba.getRingNumber() - 1] = newRing;
		}

		else if (command.getCommandID() == Connection.ZChalBattleUpdate)
		{
			ZChalBattleUpdate battleUpdate = (ZChalBattleUpdate) command;
			this.zoneRings[battleUpdate.getRing() - 1].setDescription(battleUpdate.getDesc());
		}

		// Ring has descended into battle.
		else if (command.getCommandID() == Connection.ZChalBattleDel)
		{
			//ZChalBattleDel ringDescended = (ZChalBattleDel)command;
		}

		// Player action.
		else if (command.getCommandID() == Connection.ZChalPlayerUpdate)
		{
			ZChalPlayerUpdate playerAction = (ZChalPlayerUpdate) command;
			int playerIndex = playerAction.getPlayerIndex();
			ZonePlayer player = this.zoneCurrentPlayers[playerIndex];
			if (player == null)
				return;
			int oldRing = player.getRing();
			int oldSlot = player.getSlot();
			boolean previouslyClicked = player.getStatusFlags().isAccepted();
			boolean previouslyBM = player.getStatusFlags().isMaster();

			playerAction.updatePlayer(player);

			// NOTE: the CPF_KICKED flag is temporary and MUST not be saved, so clear it
			// before proceeding.
			this.me.getStatusFlags().setKicked(false);

			int slot = player.getSlot();
			int ring = player.getRing();

			this.eventSubscriber.challengePlayerMove(player);

			if (player == this.me)
			{
				if (ring > 0)
					this.ring = this.zoneRings[ring - 1];
				else
					this.ring = null;
			}

			// Has the player changed rings?
			if (oldRing != ring)
			{
				// K this is sorta messy. If we try to add the player and fail, restore the original ring and slot.
				boolean ringChanged = true;

				if (ring > 0)
				{
					if (!this.zoneRings[ring - 1].addPlayer(slot, player))
					{
						ringChanged = false;
						player.setRing(oldRing);
						player.setSlot(oldSlot);
					}
					else
					{
						this.eventSubscriber.challengePlayerJoinRing(player);
					}
				}

				if (oldRing > 0 && ringChanged)
				{
					this.zoneRings[oldRing - 1].removePlayer(player);
					this.eventSubscriber.challengePlayerLeaveRing(player, oldRing);
				}
			}

			if (playerAction.getStatusFlags().isMaster() && (!previouslyBM || (oldSlot != slot)))
			{
				this.eventSubscriber.challengePlayerBattleMaster(player);
			}

			if (previouslyClicked && !playerAction.getStatusFlags().isAccepted())
			{
				this.eventSubscriber.challengePlayerClickOut(player);
			}

			if (!previouslyClicked && playerAction.getStatusFlags().isAccepted())
			{
				this.eventSubscriber.challengePlayerClickIn(player);
			}

			if ((oldRing == ring) && (oldRing != 0))
			{
				if (oldSlot != slot)
					this.eventSubscriber.challengePlayerChangeSlot(player, oldSlot);
			}

			if (playerAction.getStatusFlags().isKicked())
			{
				if (player == this.me)
				{
					this.ring = null;
				}

				this.eventSubscriber.challengePlayerKicked(player);
			}
		}

		else if (command.getCommandID() == Connection.ZChalLaunchClient)
		{
			ZChalLaunchClient launch = (ZChalLaunchClient) command;
			this.eventSubscriber.challengeLaunchClient(launch.getGameID(), launch.getIp());
		}

		else if (command.getCommandID() == Connection.ZChalLaunchServer)
		{
			ZChalLaunchServer launch = (ZChalLaunchServer) command;
			this.eventSubscriber.challengeLaunchServer(launch.getGameID(), launch.getBountyTerr(), launch.getClients());
		}

		/*else if(command.getCommandID() == NSConnection.ZBattleOptions)
		{
			ZBattleOptions battleOptions = (ZBattleOptions)command;
		}*/

		else
		{
			System.out.println("NSChallengeClient: Unrecognized command #" + command.getCommandID());
		}
	}

	// Returns a reference to the NSZonePlayer that belongs to the client using this object.
	public ZonePlayer getMe()
	{
		return this.me;
	}

	public float getActiveMapH()
	{
		return activeMapH;
	}

	public float getActiveMapW()
	{
		return activeMapW;
	}

	public BoardCoord getChalCenter()
	{
		return chalCenter;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	private Connection challengeServer;

	private ChallengeListener eventSubscriber; // This is who we palm the events off to.

	private CommandProcessor processor;

	private int serverMajorVersion; // Major and minor version

	private int serverMinorVersion; // of the challenge server.

	private int zoneCount; // How many zones in this server.

	private int[] zonePlayers; // How many players in each zone.

	private int zoneCurrent = -1; // The zone we're in

	private int forceZone = -1; // Forces this connection to join specified zone.

	private ZonePlayer me; // A quick reference to our Player.

	private ZonePlayer zoneCurrentPlayers[]; // Up to date list of players in the zone.

	private ZoneRing zoneRings[]; // These NSZoneRings reflect current ring status in zones.

	private ZoneRing ring; // A quick handy reference to the ring we are sitting on.

	private String host; // Server we're connected to.

	private int port;

	protected boolean inZone; // Have we completely loaded this zone's state yet?

	private ZonePlayer initPlayer; // Client passes this in for us to use for connection.

	private float activeMapW, activeMapH; // Width and height of the zone map area.

	private BoardCoord chalCenter; // Exact center of map.

	public void setInitPlayer(ZonePlayer initPlayer)
	{
		this.initPlayer = initPlayer;
	}
}
