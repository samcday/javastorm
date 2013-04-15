import java.net.InetAddress;
import java.net.Socket;

import org.javastorm.NSUtil;
import org.javastorm.NSVersion;
import org.javastorm.Netstorm;
import org.javastorm.challenge.NSZonePlayer;
import org.javastorm.challenge.NSZoneRing;
import org.javastorm.network.NSCommandListener;
import org.javastorm.network.NSCommandProcessor;
import org.javastorm.network.NSConnection;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZBattleOptions;
import org.javastorm.network.commands.ZChalBattleAdd;
import org.javastorm.network.commands.ZChalBattleDel;
import org.javastorm.network.commands.ZChalBattleUpdate;
import org.javastorm.network.commands.ZChalLoginReply;
import org.javastorm.network.commands.ZChalPlayerAdd;
import org.javastorm.network.commands.ZChalPlayerAddress;
import org.javastorm.network.commands.ZChalPlayerUpdate;
import org.javastorm.network.commands.ZChalRequestChangeStatus;
import org.javastorm.network.commands.ZChalRequestLogin;
import org.javastorm.network.commands.ZChalRequestSailTo;
import org.javastorm.network.commands.ZChatLine;
import org.javastorm.network.commands.ZRootQuery;

public class NSChallengeServerClient implements NSCommandListener
{
	public NSChallengeServerClient(NSChallengeServer server)
	{
		this.processor = new NSCommandProcessor();
		this.client = new NSConnection();
		this.server = server;
		this.ip = "127.0.0.1";
		if (this.ip.equalsIgnoreCase("0.0.0.0"))
			this.ip = "127.0.0.1";

		// We initially assume this is a root query, unless the client sends us a ZChalRequestLogin.
		this.rootQuery = true;
	}

	public NSChallengeServerClient(NSChallengeServer server, Socket socket)
	{
		this(server);

		InetAddress addr = socket.getInetAddress();
		this.ip = addr.getHostAddress();
		if (this.ip.equalsIgnoreCase("0.0.0.0"))
			this.ip = "127.0.0.1";

		this.client.init(socket);
		this.processor.start(this.client, this);
	}

	public void onConnectionDead()
	{
		this.disconnect();
	}

	public void onException(Throwable t)
	{

	}

	public void disconnect()
	{
		this.client.disconnect();
		this.processor.stop();

		if (!this.rootQuery)
		{
			this.server.removeClient(this);
		}
	}

	public void onCommand(NSCommand command)
	{
		switch (command.getCommandID())
		{
			case NSConnection.ZRootQuery:
			{
				this.handleRootQuery((ZRootQuery) command);
				break;
			}
			case NSConnection.ZChalRequestLogin:
			{
				this.rootQuery = false;
				this.handleChalLogin((ZChalRequestLogin) command);
				break;
			}
			case NSConnection.ZChalRequestSailTo:
			{
				this.handleRequestSailTo((ZChalRequestSailTo) command);
				break;
			}
			case NSConnection.ZChatLine:
			{
				this.handleChatLine((ZChatLine) command);
				break;
			}
			case NSConnection.ZChalRequestChangeStatus:
			{
				this.handleRequestChangeStatus((ZChalRequestChangeStatus) command);
				break;
			}
			case NSConnection.ZChalBattleUpdate:
			{
				this.handleBattleUpdate((ZChalBattleUpdate) command);
				break;
			}
			case NSConnection.ZBattleOptions:
			{
				this.handleBattleOptions((ZBattleOptions) command);
				break;
			}
			default:
			{
				System.out.println("Unhandled command #" + command.getCommandID());
				break;
			}
		}
	}

	private void handleRootQuery(ZRootQuery zrq)
	{
		this.client.sendCommand(this.server.createRootReply());
	}

	private void handleChalLogin(ZChalRequestLogin zcrl)
	{
		// Create a NSZonePlayer to represent this new arrival.
		this.player = new NSZonePlayer(new NSVersion(Netstorm.MAJOR_VERSION, zcrl.getClientMajorVersion()));
		
		this.player.setIp(NSUtil.ipStringToArray(this.ip));
		this.player.setLanguageId(zcrl.getLanguageId());
		this.player.setSubscriberID(zcrl.getSubscriberID());
		this.player.setTerrain(zcrl.getTerr());
		this.player.setCoreData(zcrl.getPlayerData());
		this.player.setNickname(zcrl.getName());
		this.player.setPlayerIndex(this.server.getUnusedPlayerIndex());
		this.player.setFlags(zcrl.get_flags());
		this.player.getStatusFlags().setValid(true);
		this.player.getPos().moveTo(this.server.getPlayerStartingPosition(this.player.getPlayerIndex()));
		this.server.addClient(this);

		System.out.println(zcrl.getName() + " connected. Player Index: " + this.player.getPlayerIndex());

		// Send challenge login reply.
		this.sendChallengeReply();

		// Send the challenge server info.
		this.sendChallengeInfo();

		// Send ring info.
		this.sendInitialRings();

		// Send a list of all currently connected players.
		this.sendInitialPlayers();

		// Send me to everyone else.
		this.sendMe();
	}

	private void handleRequestSailTo(ZChalRequestSailTo zcrst)
	{
		int oldRing = this.player.getRing();
		int oldSlot = this.player.getSlot();

		// If the player is already on a ring, and is moving to a new one, remove him from old one first.
		if (this.player.getRing() > 0 && zcrst.getRing() != this.player.getRing())
		{
			NSZoneRing ring = this.server.getRing(this.player.getRing());
			ring.removePlayer(this.player);

			// If player was BM, clear that flag.
			if (this.player.getStatusFlags().isMaster())
			{
				this.player.getStatusFlags().setMaster(false);
			}

			// If the ring is now vacant, reset it to default values.
			if (ring.isEmpty())
			{
				ring.clear();
				this.sendToAll(ring.createUpdate(), true);
			}
		}

		this.player.getPos().moveTo(zcrst.getPos());
		this.player.setSlot(zcrst.getSlot());
		this.player.setRing(zcrst.getRing());
		this.player.getStatusFlags().setAccepted(false);

		// Attach player to new ring they're on, if applicable.
		if ((this.player.getRing() != oldRing) && this.player.getRing() > 0 && zcrst.getRing() == this.player.getRing())
		{
			NSZoneRing ring = this.server.getRing(this.player.getRing());

			// If there's an error joining new ring, restore original and exit.
			if (!ring.addPlayer(zcrst.getSlot(), this.player))
			{
				this.player.setRing(oldRing);
				this.player.setSlot(oldSlot);
				return;
			}

			// If player is only one on ring, we'll make them master (provided they're not firewalled).
			if (ring.getPlayerCount() == 1)
			{
				if (!this.player.getStatusFlags().isBadDiag())
				{
					this.player.getStatusFlags().setMaster(true);
				}
			}
			else
			{
				// Nope, just another client. Send them the battle options.
				ZBattleOptions zbo = new ZBattleOptions();
				zbo.setBattleOptions(ring.getBattleOptions());
				zbo.setRing(ring.getRingNum());
				this.sendCommand(zbo);
			}
		}

		ZChalPlayerUpdate zcpu = this.player.createUpdate();
		this.sendToAll(zcpu, true);
	}

	// Handles status change requests.
	public void handleRequestChangeStatus(ZChalRequestChangeStatus zcrcs)
	{
		boolean send = true;

		if (zcrcs.getStatusFlags().isChange())
		{
			zcrcs.getStatusFlags().setChange(false);
			zcrcs.getStatusFlags().setAccepted(false);
		}

		this.player.getStatusFlags().copyProtectSystemFlags(zcrcs.getStatusFlags());

		if (this.player.getStatusFlags().isMaster())
		{
			if (zcrcs.getStatusFlags().isLaunch())
			{
				this.server.startBattle(this.server.getRing(this.player.getRing()));
				send = false;
			}
		}

		if (send)
		{
			ZChalPlayerUpdate zcpu = this.player.createUpdate();
			this.sendToAll(zcpu, true);
		}
	}

	public void handleChatLine(ZChatLine zcl)
	{
		int[] dest = zcl.getDestinationSubscriberIDs();
		NSChallengeServerClient client;

		if (zcl.getMessage().indexOf(".lol") > -1)
		{
			NSZoneRing rings[] = this.server.getRings();
			for (NSZoneRing ring : rings)
			{
				ZChalBattleDel zcbd = new ZChalBattleDel();
				zcbd.setRingNumber(ring.getRingNum());
				this.sendToAll(zcbd, true);

				ZChalBattleAdd zcba = new ZChalBattleAdd();
				zcba.setRingNumber(ring.getRingNum());
				zcba.setPos(this.server.battlePosList[ring.getRingNum() - 1]);
				this.sendToAll(zcba, true);
			}

			return;
		}
		if (zcl.getMessage().indexOf(".zombies") > -1)
		{
			this.server.createZombies();
		}

		// If this is a CHAT_POPUP chatline, change the subId to CHAT_BROADCAST and remove the nickname.
		if ((zcl.getEncoding() & ZChatLine.CHAT_POPUP) > 0)
		{
			zcl.setSubscriberID(ZChatLine.CHAT_BROADCAST);
			zcl.setNickname(null);
		}

		// Now we fire the chat line off to all the recipients.
		// We remove the list of destination subids and just add the one subid to each person we send to.
		for (int i = 0; i < dest.length; i++)
		{
			zcl.clearDestinationSubscriberIDs();
			zcl.addDestinationSubscriberID(dest[i]);

			client = this.server.getClientBySubscriberID(dest[i]);

			if (client != null)
			{
				//System.out.println("Sent " + client.getPlayer().getNickname() + " a chatline with ");
				client.sendCommand(zcl);
			}
		}
	}

	private void handleBattleUpdate(ZChalBattleUpdate zcbu)
	{
		NSZoneRing ring = this.server.getRing(zcbu.getRing());
		ring.setDescription(zcbu.getDesc());

		this.sendToAll(ring.createUpdate(), true);
	}

	private void handleBattleOptions(ZBattleOptions zbo)
	{
		NSZoneRing ring = this.server.getRing(zbo.getRing());
		ring.setBattleOptions(zbo.getBattleOptions());
		this.server.sendToRing(ring, zbo);

		// Click everyone out on this ring.
		NSZonePlayer[] players = ring.getPlayers();
		for (NSZonePlayer player : players)
		{
			player.getStatusFlags().setAccepted(false);
			this.server.sendToRing(ring, player.createUpdate());
		}
	}

	public void sendChallengeReply()
	{
		ZChalLoginReply zclr;

		// Send the Challenge Login reply.
		zclr = new ZChalLoginReply();

		zclr.setCode(ZChalLoginReply.CLR_TRUE);
		zclr.setServerMajorVersion(10);
		zclr.setServerMinorVersion(this.player.getVersion().getMinorVersion());
		zclr.setSubId(this.player.getSubscriberID());

		this.client.sendCommand(zclr);
	}

	public void sendChallengeInfo()
	{
		this.client.sendCommand(this.server.createServerInfo());
	}

	// Sends state of all current rings.
	private void sendInitialRings()
	{
		NSZoneRing[] rings = this.server.getRings();
		NSZoneRing ring;

		for (int i = 0; i < rings.length; i++)
		{
			ring = rings[i];

			ZChalBattleAdd zcba = new ZChalBattleAdd();
			zcba.setPos(ring.getPos());
			zcba.setRingNumber(ring.getRingNum());
			zcba.setFlags(ring.getFlags() | NSZoneRing.CBF_INITIAL); // Same deal as with initial player xmits.
			this.client.sendCommand(zcba);

			ZChalBattleUpdate zcbu = new ZChalBattleUpdate();
			zcbu.setDesc(ring.getDescription());
			zcbu.setRing(ring.getRingNum());
			zcbu.setExtra(0);
			this.client.sendCommand(zcbu);
		}
	}

	// Sends all the current players to client.
	public void sendInitialPlayers()
	{
		NSChallengeServerClient clients[] = this.server.getConnectedClients();
		ZChalPlayerAdd zcpa;
		ZChalPlayerAddress zcpaddr;

		NSZonePlayer player;

		for (NSChallengeServerClient client : clients)
		{
			player = client.getPlayer();
			if (player == null)
				continue;

			zcpa = new ZChalPlayerAdd();

			// This ensures clients don't play the falling animation for players who were in zone before them.
			if (player != this.player)
				player.getStatusFlags().setInitial(true);

			zcpa.setNSPlayer(client.getPlayer());
			this.client.sendCommand(zcpa);

			zcpaddr = new ZChalPlayerAddress();
			zcpaddr.setPlayerIndex(player.getPlayerIndex());
			zcpaddr.setSubscriberID(player.getSubscriberID());
			zcpaddr.setIp(this.ip);
			this.client.sendCommand(zcpaddr);

			player.getStatusFlags().setInitial(false);
		}
	}

	// Sends me to everyone else connected to server.
	public void sendMe()
	{
		ZChalPlayerAdd zcpa = new ZChalPlayerAdd();
		zcpa.setNSPlayer(this.player);
		this.sendToAll(zcpa);

		ZChalPlayerAddress zcpaddr = new ZChalPlayerAddress();
		zcpaddr.setPlayerIndex(this.player.getPlayerIndex());
		zcpaddr.setSubscriberID(this.player.getSubscriberID());
		zcpaddr.setIp(this.ip);
		this.sendToAll(zcpaddr);
	}

	public void sendCommand(NSCommand command)
	{
		if (this.client != null)
			this.client.sendCommand(command);
	}

	public void sendToAll(NSCommand command)
	{
		this.sendToAll(command, false);
	}

	public void sendToAll(NSCommand command, boolean sendToMyself)
	{
		this.server.sendToAll(command, !sendToMyself ? this : null);
	}

	public NSZonePlayer getPlayer()
	{
		return this.player;
	}

	private String ip;

	private boolean rootQuery; // Is this connection a root query?

	private NSCommandProcessor processor;

	private NSZonePlayer player;

	private NSChallengeServer server;

	private NSConnection client;
}
