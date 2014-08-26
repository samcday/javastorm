package org.javastorm.battle;

import org.javastorm.challenge.ZoneRing;
import org.javastorm.util.MyByteBuffer;

//Represents all the customizable options in a battle.
public class BattleOptions
{
	public BattleOptions()
	{
		this.sbo = new NSSimpleBattleOptions(this);

		this.setGameType(0);
		this.setBridgeSlots(2);
		this.setUnitRate(2);
		this.setGenRange(3);
		this.setMapMode(0);
		this.setMapSize(2);
		this.setMapDensity(1);
		this.setSpells(0);
		this.setIslandDynamics(0);
		this.setAlliances(1);
		this.setAlliancesBreakable(0);
		this.setAllowSend(1);
		this.setPublicObserverChat(0);
		this.setStartCash(1);
		this.setKillReward(2);
		this.setMoneyPerGeyser(2);
		this.setGeyserAmount(1);
		this.setGeyserPlacement(2);
		this.setGeyserRespawns(2);
		this.setResourceInjections(0);
		this.setInjectionValue(0);

		this.techExclude = new int[BattleOptions.MAX_EXCLUDED_TYPES];
		this.slotRanks = new int[8];
		this.slotSpectators = new boolean[8];
		this.slotTeams = new int[8];

		this.setBattleType(3);

		this.setSlotSpectator(ZoneRing.BLUE, false);
		this.setSlotSpectator(ZoneRing.RED, false);
		this.setSlotSpectator(ZoneRing.WHITE, false);
		this.setSlotSpectator(ZoneRing.GREEN, false);
		this.setSlotSpectator(ZoneRing.PURPLE, false);
		this.setSlotSpectator(ZoneRing.YELLOW, false);
		this.setSlotSpectator(ZoneRing.TEAL, false);
		this.setSlotSpectator(ZoneRing.ORANGE, false);

		this.setSlotTeam(ZoneRing.BLUE, ZoneRing.BLUE);
		this.setSlotTeam(ZoneRing.RED, ZoneRing.RED);
		this.setSlotTeam(ZoneRing.WHITE, ZoneRing.WHITE);
		this.setSlotTeam(ZoneRing.GREEN, ZoneRing.WHITE);
		this.setSlotTeam(ZoneRing.PURPLE, ZoneRing.RED);
		this.setSlotTeam(ZoneRing.YELLOW, ZoneRing.BLUE);
		this.setSlotTeam(ZoneRing.TEAL, ZoneRing.BLUE);
		this.setSlotTeam(ZoneRing.ORANGE, ZoneRing.RED);
	}

	// Reads from BUFFER into this object.
	public void read(int minorVersion, MyByteBuffer buffer)
	{
		int length = buffer.getShort();

		if (length == 0)
			return;

		this.setGameType(buffer.get()); // Game Type.
		this.setBridgeSlots(buffer.get()); // Bridge slots.
		this.setUnitRate(buffer.get()); // Unit rate.
		buffer.skip(1); // Knowledge
		this.setGenRange(buffer.get()); // Generator range.
		this.setMapMode(buffer.get()); // Map Mode.
		this.setMapSize(buffer.get()); // Map size.
		this.setMapDensity(buffer.get()); // Map density.
		this.setSpells(buffer.get()); // Spells
		this.setIslandDynamics(buffer.get()); // Island dynamics.
		buffer.skip(1); // Living world.
		this.setAlliances(buffer.get()); // Alliances.
		this.setAlliancesBreakable(buffer.get()); // Alliances breakable.
		this.setAllowSend(buffer.get()); // Allow Sending Money

		if (minorVersion > 78)
			this.setPublicObserverChat(buffer.get()); // Allow Observers to speak in public chat

		this.setStartCash(buffer.get()); // Starting cash.
		this.setKillReward(buffer.get()); // Kill Reward.
		this.setMoneyPerGeyser(buffer.get()); // Money per geyser.
		this.setGeyserAmount(buffer.get()); // Geyser amount.
		this.setGeyserPlacement(buffer.get()); // Geyser placement.
		this.setGeyserRespawns(buffer.get()); // Geyser respawns.
		this.setResourceInjections(buffer.get()); // Resource Injections
		this.setInjectionValue(buffer.get()); // Injection value.

		// Tech exclusion.
		buffer.skip(1);
		for (int j = 0; j < 40; j++)
		{
			int typeID = buffer.get();

			if (typeID != 0)
			{
				this.excludeTech(typeID);
			}
		}

		// These bytes specify whether or not a colour is spectating.
		for (int i = 0; i < 8; i++)
		{
			this.setSlotSpectator(i, buffer.get() != 0 ? true : false);
		}

		// These set each colour's team colour.
		for (int i = 0; i < 8; i++)
		{
			this.setSlotTeam(i, buffer.get());
		}

		// These set each colour's team colour.
		for (int i = 0; i < 8; i++)
		{
			this.setSlotRank(i, buffer.get());
		}
	}

	// Reads from THIS OBJECT into Buffer.
	public void write(int minorVersion, MyByteBuffer buffer)
	{
		buffer.put(this.getGameType()); // Game Type.
		buffer.put(this.getBridgeSlots()); // Bridge slots.
		buffer.put(this.getUnitRate()); // Unit rate.
		buffer.put(0); // Knoweledge. unimplemented as far as I know
		buffer.put(this.getGenRange()); // Generator range.
		buffer.put(this.getMapMode()); // Map Mode.
		buffer.put(this.getMapSize()); // Map size.
		buffer.put(this.getMapDensity()); // Map density.
		buffer.put(this.getSpells()); // Spells
		buffer.put(this.getIslandDynamics()); // Island dynamics.
		buffer.put(0); // Living World - unimplemented
		buffer.put(this.getAlliances()); // Alliances.
		buffer.put(this.getAlliancesBreakable()); // Alliances breakable.
		buffer.put(this.getAllowSend()); // Allow Sending Money

		if (minorVersion > 78)
			buffer.put(this.getPublicObserverChat());

		buffer.put(this.getStartCash()); // Starting cash.
		buffer.put(this.getKillReward()); // Kill Reward.
		buffer.put(this.getMoneyPerGeyser()); // Money per geyser.
		buffer.put(this.getGeyserAmount()); // Geyser amount.
		buffer.put(this.getGeyserPlacement()); // Geyser placement.
		buffer.put(this.getGeyserRespawns()); // Geyser respawns.
		buffer.put(this.getResourceInjections()); // Resource Injections
		buffer.put(this.getInjectionValue()); // Injection value.

		// Tech exclusions. First byte is count, then typeID of each exclusion.
		buffer.put(this.excludeCount);
		for (int j = 0; j < MAX_EXCLUDED_TYPES; j++)
		{
			if (this.techExclude[j] != -1)
				buffer.put(this.techExclude[j]);
			else
				buffer.put(0);
		}

		// These bytes specify whether or not a colour is spectating.
		buffer.put(this.getSlotSpectator(ZoneRing.BLUE) ? 1 : 0); // Blue spectator
		buffer.put(this.getSlotSpectator(ZoneRing.RED) ? 1 : 0); // Red spectator.
		buffer.put(this.getSlotSpectator(ZoneRing.WHITE) ? 1 : 0); // White spectator.
		buffer.put(this.getSlotSpectator(ZoneRing.GREEN) ? 1 : 0); // Green spectator.
		buffer.put(this.getSlotSpectator(ZoneRing.PURPLE) ? 1 : 0); // Purple spectator.
		buffer.put(this.getSlotSpectator(ZoneRing.YELLOW) ? 1 : 0); // Yellow spectator.
		buffer.put(this.getSlotSpectator(ZoneRing.TEAL) ? 1 : 0); // Teal spectator.
		buffer.put(this.getSlotSpectator(ZoneRing.ORANGE) ? 1 : 0); // Orange spectator.

		// These set each colour's team colour.
		buffer.put(this.getSlotTeam(ZoneRing.BLUE)); // Blue spectator
		buffer.put(this.getSlotTeam(ZoneRing.RED)); // Red spectator.
		buffer.put(this.getSlotTeam(ZoneRing.WHITE)); // White spectator.
		buffer.put(this.getSlotTeam(ZoneRing.GREEN)); // Green spectator.
		buffer.put(this.getSlotTeam(ZoneRing.PURPLE)); // Purple spectator.
		buffer.put(this.getSlotTeam(ZoneRing.YELLOW)); // Yellow spectator.
		buffer.put(this.getSlotTeam(ZoneRing.TEAL)); // Teal spectator.
		buffer.put(this.getSlotTeam(ZoneRing.ORANGE)); // Orange spectator.

		// This sets rank for each player.
		buffer.put(this.getSlotRank(ZoneRing.BLUE));
		buffer.put(this.getSlotRank(ZoneRing.RED));
		buffer.put(this.getSlotRank(ZoneRing.WHITE));
		buffer.put(this.getSlotRank(ZoneRing.GREEN));
		buffer.put(this.getSlotRank(ZoneRing.PURPLE));
		buffer.put(this.getSlotRank(ZoneRing.YELLOW));
		buffer.put(this.getSlotRank(ZoneRing.TEAL));
		buffer.put(this.getSlotRank(ZoneRing.ORANGE));
	}

	// Excludes a specific tech.
	public void excludeTech(int typeID)
	{
		for (int i = 0; i < this.techExclude.length; i++)
		{
			if (this.techExclude[i] == -1)
			{
				this.techExclude[i] = typeID;
				this.excludeCount++;
				return;
			}
		}
	}

	// Negates a previously set tech exclusion.
	public void allowTech(int typeID)
	{
		for (int i = 0; i < this.techExclude.length; i++)
		{
			if (this.techExclude[i] == typeID)
			{
				this.techExclude[i] = -1;
				this.excludeCount--;
				return;
			}
		}
	}

	public void setGameType(int gameType)
	{
		this.gameType = gameType;
	}

	public int getGameType()
	{
		return this.gameType;
	}

	public void setBridgeSlots(int bridgeSlots)
	{
		this.bridgeSlots = bridgeSlots;
	}

	public int getBridgeSlots()
	{
		return this.bridgeSlots;
	}

	public void setSlotTeam(int slot, int team)
	{
		this.slotTeams[slot] = team;
	}

	public int getSlotTeam(int slot)
	{
		return this.slotTeams[slot];
	}

	public void setSlotSpectator(int slot, boolean spectator)
	{
		this.slotSpectators[slot] = spectator;
	}

	public boolean getSlotSpectator(int slot)
	{
		return this.slotSpectators[slot];
	}

	public void setSlotRank(int slot, int rank)
	{
		this.slotRanks[slot] = rank;
	}

	public int getSlotRank(int slot)
	{
		return this.slotRanks[slot];
	}

	public void setBattleType(int battleType)
	{
		this.battleType = battleType;
	}

	public int getBattleType()
	{
		return this.battleType;
	}

	public void setInjectionValue(int injectionValue)
	{
		this.injectionValue = injectionValue;
	}

	public int getInjectionValue()
	{
		return this.injectionValue;
	}

	public void setResourceInjections(int resourceInjections)
	{
		this.resourceInjections = resourceInjections;
	}

	public int getResourceInjections()
	{
		return this.resourceInjections;
	}

	public void setGeyserRespawns(int geyserRespawns)
	{
		this.geyserRespawns = geyserRespawns;
	}

	public int getGeyserRespawns()
	{
		return this.geyserRespawns;
	}

	public void setGeyserPlacement(int geyserPlacement)
	{
		this.geyserPlacement = geyserPlacement;
	}

	public int getGeyserPlacement()
	{
		return this.geyserPlacement;
	}

	public void setGeyserAmount(int geyserAmount)
	{
		this.geyserAmount = geyserAmount;
	}

	public int getGeyserAmount()
	{
		return this.geyserAmount;
	}

	public void setMoneyPerGeyser(int moneyPerGeyser)
	{
		this.moneyPerGeyser = moneyPerGeyser;
	}

	public int getMoneyPerGeyser()
	{
		return this.moneyPerGeyser;
	}

	public void setKillReward(int killReward)
	{
		this.killReward = killReward;
	}

	public int getKillReward()
	{
		return this.killReward;
	}

	public void setStartCash(int startCash)
	{
		this.startCash = startCash;
	}

	public int getStartCash()
	{
		return this.startCash;
	}

	public void setAllowSend(int allowSend)
	{
		this.allowSend = allowSend;
	}

	public int getAllowSend()
	{
		return this.allowSend;
	}

	public int getPublicObserverChat()
	{
		return publicObserverChat;
	}

	public void setPublicObserverChat(int publicObserverChat)
	{
		this.publicObserverChat = publicObserverChat;
	}

	public void setAlliancesBreakable(int alliancesBreakable)
	{
		this.alliancesBreakable = alliancesBreakable;
	}

	public int getAlliancesBreakable()
	{
		return this.alliancesBreakable;
	}

	public void setAlliances(int alliances)
	{
		this.alliances = alliances;
	}

	public int getAlliances()
	{
		return this.alliances;
	}

	public void setIslandDynamics(int islandDynamics)
	{
		this.islandDynamics = islandDynamics;
	}

	public int getIslandDynamics()
	{
		return this.islandDynamics;
	}

	public void setSpells(int spells)
	{
		this.spells = spells;
	}

	public int getSpells()
	{
		return this.spells;
	}

	public void setMapDensity(int mapDensity)
	{
		this.mapDensity = mapDensity;
	}

	public int getMapDensity()
	{
		return this.mapDensity;
	}

	public void setMapSize(int mapSize)
	{
		this.mapSize = mapSize;
	}

	public int getMapSize()
	{
		return this.mapSize;
	}

	public void setMapMode(int mapMode)
	{
		this.mapMode = mapMode;
	}

	public int getMapMode()
	{
		return this.mapMode;
	}

	public void setGenRange(int genRange)
	{
		this.genRange = genRange;
	}

	public int getGenRange()
	{
		return this.genRange;
	}

	public void setUnitRate(int unitRate)
	{
		this.unitRate = unitRate;
	}

	public int getUnitRate()
	{
		return this.unitRate;
	}

	public void set1v1()
	{
		this.setSlotSpectator(ZoneRing.BLUE, false);
		this.setSlotSpectator(ZoneRing.RED, false);
		this.setSlotSpectator(ZoneRing.WHITE, true);
		this.setSlotSpectator(ZoneRing.GREEN, true);
		this.setSlotSpectator(ZoneRing.PURPLE, true);
		this.setSlotSpectator(ZoneRing.YELLOW, true);
		this.setSlotSpectator(ZoneRing.TEAL, true);
		this.setSlotSpectator(ZoneRing.ORANGE, true);
	}

	public void set2v2()
	{
		this.setSlotSpectator(ZoneRing.BLUE, false);
		this.setSlotSpectator(ZoneRing.RED, false);
		this.setSlotSpectator(ZoneRing.WHITE, true);
		this.setSlotSpectator(ZoneRing.GREEN, true);
		this.setSlotSpectator(ZoneRing.PURPLE, false);
		this.setSlotSpectator(ZoneRing.YELLOW, false);
		this.setSlotSpectator(ZoneRing.TEAL, true);
		this.setSlotSpectator(ZoneRing.ORANGE, true);
	}

	public void set3v3()
	{
		this.setSlotSpectator(ZoneRing.BLUE, false);
		this.setSlotSpectator(ZoneRing.RED, false);
		this.setSlotSpectator(ZoneRing.WHITE, true);
		this.setSlotSpectator(ZoneRing.GREEN, true);
		this.setSlotSpectator(ZoneRing.PURPLE, false);
		this.setSlotSpectator(ZoneRing.YELLOW, false);
		this.setSlotSpectator(ZoneRing.TEAL, false);
		this.setSlotSpectator(ZoneRing.ORANGE, false);
	}

	public NSSimpleBattleOptions getSimpleBattleOptions()
	{
		return this.sbo;
	}

	public class NSSimpleBattleOptions
	{
		private NSSimpleBattleOptions(BattleOptions bo)
		{
			this.bo = bo;
		}

		public int getMapSize()
		{
			int localMapSize[] =
			{ 3, 4, 5, 6 };
			return localMapSize[this.bo.getMapSize()];
		}

		private BattleOptions bo;
	}

	private NSSimpleBattleOptions sbo;

	private int gameType;

	private int bridgeSlots;

	private int unitRate;

	private int genRange;

	private int mapMode;

	private int mapSize;

	private int mapDensity;

	private int spells;

	private int islandDynamics;

	private int alliances;

	private int alliancesBreakable;

	private int allowSend;

	private int publicObserverChat;

	private int startCash;

	private int killReward;

	private int moneyPerGeyser;

	private int geyserAmount;

	private int geyserPlacement;

	private int geyserRespawns;

	private int resourceInjections;

	private int injectionValue;

	private int battleType;

	private int[] techExclude;

	private int excludeCount;

	private boolean[] slotSpectators;

	private int[] slotTeams;

	private int[] slotRanks;

	private static int MAX_EXCLUDED_TYPES = 40;
}