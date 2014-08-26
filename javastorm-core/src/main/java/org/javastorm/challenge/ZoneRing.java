package org.javastorm.challenge;

import java.util.Vector;

import org.javastorm.BoardCoord;
import org.javastorm.battle.BattleOptions;
import org.javastorm.network.commands.ZChalBattleUpdate;

public class ZoneRing
{
	public ZoneRing(int ringNum)
	{
		this.description = "";
		this.players = new ZonePlayer[8];
		this.playerCount = 0;
		this.ringNum = ringNum;
		this.kickList = new Vector<ZonePlayer>();
	}

	public int getFlags()
	{
		return flags;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public BoardCoord getPos()
	{
		return pos;
	}

	public void setPos(BoardCoord pos)
	{
		this.pos = pos;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean addPlayer(int slot, ZonePlayer player)
	{
		if (!this.canJoin(slot, player))
			return false;

		this.players[slot] = player;
		this.playerCount++;
		return true;
	}

	public void removePlayer(ZonePlayer player)
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] == player)
			{
				this.players[i] = null;
				this.playerCount--;
				break;
			}
		}
	}

	public void kickPlayer(ZonePlayer player)
	{
		ZonePlayer ringPlayer = this.getPlayer(player.getSubscriberID());
		if (ringPlayer == null)
			return;

		this.kickList.add(player);
		this.removePlayer(player);
	}

	public boolean canJoin(int slot, ZonePlayer player)
	{
		if (!this.slotAvailable(slot))
			return false;

		if (this.kickList.contains(player))
			return false;

		return true;
	}

	public boolean slotAvailable(int slot)
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
			{
				if (this.players[i].getSlot() == slot)
					return false;
			}
		}
		return true;
	}

	public ZonePlayer getBattleMaster()
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
				if (this.players[i].getStatusFlags().isMaster())
				{
					return this.players[i];
				}
		}

		return null;
	}

	// Gets all players on this ring.
	public ZonePlayer[] getPlayers()
	{
		ZonePlayer[] players = new ZonePlayer[this.playerCount];
		int index = 0;

		for (int i = 0; i < this.players.length; i++)
			if (this.players[i] != null)
				players[index++] = this.players[i];

		return players;
	}

	// Gets player with specified subscriberID on this ring.
	public ZonePlayer getPlayer(int subscriberID)
	{
		for (int i = 0; i < this.players.length; i++)
			if (this.players[i] != null)
				if (this.players[i].getSubscriberID() == subscriberID)
					return this.players[i];

		return null;
	}

	public int getPlayerCount()
	{
		return this.playerCount;
	}

	public int getRingNum()
	{
		return this.ringNum;
	}

	// Does this ring have any players on it?
	public boolean isEmpty()
	{
		for (int i = 0; i < this.players.length; i++)
		{
			if (this.players[i] != null)
			{
				return false;
			}
		}

		return true;
	}

	// Has everyone on this ring clicked in? (excluding battle master)
	public boolean allClicked()
	{
		for (int i = 0; i < this.players.length; i++)
			if (this.players[i] != null)
				if (!this.players[i].getStatusFlags().isMaster())
					if (!this.players[i].getStatusFlags().isAccepted())
					{
						return false;
					}
		return true;
	}

	public void clear()
	{
		this.players = new ZonePlayer[8];
		this.playerCount = 0;
		this.description = " ";
		this.bo = null;
		this.kickList.clear();
	}

	public ZChalBattleUpdate createUpdate()
	{
		ZChalBattleUpdate zcbu = new ZChalBattleUpdate();
		zcbu.setRing(this.ringNum);
		zcbu.setDesc(this.description);
		zcbu.setExtra(0); // TODO?

		return zcbu;
	}

	public BattleOptions getBattleOptions()
	{
		return this.bo;
	}

	public void setBattleOptions(BattleOptions bo)
	{
		this.bo = bo;
	}

	private Vector<ZonePlayer> kickList; // A list of players kicked off this ring.

	private ZonePlayer players[]; // References to all players on this ring.

	private int playerCount; // How many players are on this ring?

	private int ringNum; // What number ring is this in zones?

	private String description; // What description on this ring?

	private BoardCoord pos;

	private int flags;

	private BattleOptions bo; // The battle options in effect on this ring.

	public static int BLUE = 0;

	public static int RED = 1;

	public static int WHITE = 2;

	public static int GREEN = 3;

	public static int PURPLE = 4;

	public static int YELLOW = 5;

	public static int TEAL = 6;

	public static int ORANGE = 7;

	public static String[] SLOTS =
	{ "Blue", "Red", "White", "Green", "Purple", "Yellow", "Teal", "Orange" };

	public static String[] COLOURS =
	{ "~b", "~r", "~w", "~g", "~m", "~y", "~[C.6]", "~o" };

	public static final int MAX_RINGS = 10;

	public static String[] SLOTCOLOURS =
	{ COLOURS[0] + SLOTS[0], COLOURS[1] + SLOTS[1], COLOURS[2] + SLOTS[2], COLOURS[3] + SLOTS[3], COLOURS[4] + SLOTS[4], COLOURS[5] + SLOTS[5], COLOURS[6] + SLOTS[6], COLOURS[7] + SLOTS[7] };

	public static final int CBF_VALID = 1;

	public static final int CBF_INITIAL = 2;
}
