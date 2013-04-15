package org.sambro.botsy;

import org.javastorm.Player;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.network.BattleClient;
import org.javastorm.network.BattleListener;
import org.javastorm.squids.MainSquid;

public class ObserverBattleClient extends BattleListener implements CommandParserHost
{
	private class MoneyCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			BattlePlayer caller = (BattlePlayer) player;
			if (!caller.isWatcher() && !observerMaster.isAdmin(player.getSubscriberID()))
			{
				hostWhisper("Hey! That would be cheating!", player);
				return;
			}

			if (args != null)
			{
				BattlePlayer battlePlayer = (BattlePlayer) args[0];
				client.whisper(battlePlayer.getNickname() + " has " + battlePlayer.getCurrentMoney() + "sp", player.getSubscriberID());
			}
			else
			{
				BattlePlayer[] players = client.getBattle().getAllPlayers();
				StringBuilder builder = new StringBuilder();
				builder.append("~E~B~u<h2>Current Cash</h2>");
				for (int i = 0; i < players.length; i++)
				{
					builder.append("~E~B~i").append(players[i].getNickname()).append("~w - ").append(players[i].getCurrentMoney()).append("<br>");
				}

				client.popupTo(builder.toString(), player.getSubscriberID());
			}
		}
	}

	private class DumpSquidCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			float sidF = (Float) args[0];
			MainSquid s = (MainSquid) client.getBattle().getSquid((int) sidF);

			if (s != null)
			{
				StringBuilder builder = new StringBuilder();

				builder.append("~E~B~u<h2>Squid Info</h2><br>");
				builder.append("~E~B~iType: ~w").append(s.getTypeStruct().getTypeName()).append(" #").append(s.getType()).append("<br>");
				builder.append("~E~B~iPosition: ~w").append(s.getPos().toString()).append("<br>");
				builder.append("~E~B~iq: ~w").append(s.getQ()).append("<br>");
				if (s.getPlayer() != null)
				{
					builder.append("~E~B~iPlayer: ~w").append(s.getPlayer().getNickname()).append("<br>");
				}
				builder.append("~E~B~iHP: ~w").append(s.getHitPoints()).append("/").append(s.getMaxHitPoints()).append("<br>");
				builder.append("~E~B~iFrame: ~w").append(s.getFrame()).append("<br>");
				builder.append("~E~B~iGenus: ~w").append(Integer.toBinaryString(s.getGenus()));

				hostPopupTo(builder.toString(), player);
			}
		}
	}

	private class DrawCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			client.draw();
		}
	}

	private class BridgeCountCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean whisper, Object... args)
		{
			BattlePlayer caller = (BattlePlayer) player;
			if (!caller.isWatcher() && !observerMaster.isAdmin(player.getSubscriberID()))
			{
				hostWhisper("Hey! That would be cheating!", player);
				return;
			}

			if (args != null)
			{
				BattlePlayer battlePlayer = (BattlePlayer) args[0];
				client.whisper(battlePlayer.getNickname() + " has built " + battlePlayer.getBridgeCount() + " bridge pieces.", player.getSubscriberID());
			}
			else
			{
				BattlePlayer[] players = client.getBattle().getAllPlayers();
				StringBuilder builder = new StringBuilder();
				builder.append("~E~B~u<h2>Bridge Count</h2>");
				for (int i = 0; i < players.length; i++)
				{
					builder.append("~E~B~i").append(players[i].getNickname()).append("~w - ").append(players[i].getBridgeCount()).append("<br>");
				}

				client.popupTo(builder.toString(), player.getSubscriberID());
			}
		}
	}

	public ObserverBattleClient(ObserverBotsy master, BattleClient client, BotAttributes botAttributes)
	{
		this.observerMaster = master;
		this.botAttributes = botAttributes;
		this.client = client;
		this.cmdParser = new CommandParser(this);

		this.cmdParser.registerCommand(new ChatCommand("money", "Displays how much money a player currently has.", "Information", new MoneyCommand(), new ChatCommandArgument[]
		{ new ChatCommandArgument("Player", ChatCommandArgument.ARG_PLAYER, false) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand("bridgecount", "How many bridges has a player built?", "Information", new BridgeCountCommand(), new ChatCommandArgument[]
		{ new ChatCommandArgument("Player", ChatCommandArgument.ARG_PLAYER, false) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand("dumpsquid", "Displays information on a squid.", "Debug", new DumpSquidCommand(), new ChatCommandArgument[]
		{ new ChatCommandArgument("sid", ChatCommandArgument.ARG_NUMBER, true) }, false, false));

		this.cmdParser.registerCommand(new ChatCommand("draw", "Tells Botsy to draw.", "Game", new DrawCommand(), new ChatCommandArgument[] {}, false, false));
	}

	public void battlePlayerSpeak(BattlePlayer player, String message)
	{
		//if(player.getSubscriberID() != client.getMe().getSubscriberID())
		this.cmdParser.processChatline(player, message);
	}

	public void battlePlayerBuild(MainSquid s)
	{
		// lawl spam
		if (!s.getTypeStruct().getName().equals("Bridge") && !s.getTypeStruct().getName().equals("Geyser"))
		{
			//NSBattlePlayer p = this.client.getBattle().getPlayer(s.getPlayerId());
			//if(p != null)
			//hostSay(p.getNickname() + " built " + s.getTypeStruct().getName());
		}
	}

	public Player hostFindPlayerByNickname(String nickname)
	{
		return this.client.getBattle().getPlayerByNickname(nickname);
	}

	public String hostGetNickname()
	{
		return this.botAttributes.getNickname();
	}

	public boolean hostIsAdmin(Player player)
	{
		return false;
	}

	public void hostPopupTo(String sayText, Player player)
	{
		this.client.popupTo(sayText, player.getSubscriberID());
	}

	public void hostSay(String sayText)
	{
		this.client.say(String.format("~[Iicon.D41]" + this.botAttributes.getChatFormat(), this.botAttributes.getNickname()) + sayText);
	}

	public void hostWhisper(String sayText, Player player)
	{
		this.client.whisper("~[I93.111]" + String.format(this.botAttributes.getWhisperFormat(), this.botAttributes.getNickname(), player.getNickname()) + sayText, player.getSubscriberID());
	}

	private ObserverBotsy observerMaster;

	private BotAttributes botAttributes;

	private BattleClient client;

	private CommandParser cmdParser;
}
