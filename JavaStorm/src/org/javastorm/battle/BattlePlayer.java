package org.javastorm.battle;

import org.javastorm.Player;
import org.javastorm.Version;
import org.javastorm.fort.FortData;
import org.javastorm.network.commands.ZPlayerData;
import org.javastorm.squids.BaseSquid;

public class BattlePlayer extends Player
{
	public BattlePlayer(Player otherPlayer)
	{
		super(otherPlayer);
		
		this.flags = new BattlePlayerFlags();
		this.fortData = new FortData(this);
	}

	public BattlePlayer(Version version)
	{
		super(version);

		this.flags = new BattlePlayerFlags();
		this.fortData = new FortData(this);
	}

	public int getCurrentMoney()
	{
		return currentMoney;
	}

	public void setCurrentMoney(int currentMoney)
	{
		this.currentMoney = currentMoney;
	}

	public void incrementCurrentMoney(int by)
	{
		this.currentMoney += by;
	}

	public void decrementCurrentMoney(int by)
	{
		this.currentMoney -= by;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public BaseSquid getPriest()
	{
		return priest;
	}

	public void setPriest(BaseSquid priest)
	{
		this.priest = priest;
	}

	public BattlePlayerFlags getFlags()
	{
		return this.flags;
	}

	public int getAllyBits()
	{
		return allyBits;
	}

	public void setAllyBits(int allyBits)
	{
		this.allyBits = allyBits;
	}

	public int getStormPowerAccumulated()
	{
		return stormPowerAccumulated;
	}

	public void setStormPowerAccumulated(int stormPowerAccumulated)
	{
		this.stormPowerAccumulated = stormPowerAccumulated;
	}

	public int getCurAltarLevel()
	{
		return curAltarLevel;
	}

	public void setCurAltarLevel(int curAltarLevel)
	{
		this.curAltarLevel = curAltarLevel;
	}

	public int getLastAltarLevelGained()
	{
		return lastAltarLevelGained;
	}

	public void setLastAltarLevelGained(int lastAltarLevelGained)
	{
		this.lastAltarLevelGained = lastAltarLevelGained;
	}

	public int getLastRankGained()
	{
		return lastRankGained;
	}

	public void setLastRankGained(int lastRankGained)
	{
		this.lastRankGained = lastRankGained;
	}

	public int getLastTechGained()
	{
		return lastTechGained;
	}

	public void setLastTechGained(int lastTechGained)
	{
		this.lastTechGained = lastTechGained;
	}

	public int getLoginOrder()
	{
		return loginOrder;
	}

	public void setLoginOrder(int loginOrder)
	{
		this.loginOrder = loginOrder;
	}

	public int getPredictableCount()
	{
		return predictableCount;
	}

	public void setPredictableCount(int predictableCount)
	{
		this.predictableCount = predictableCount;
	}

	public boolean isWatcher()
	{
		return watcher;
	}

	public void setWatcher(boolean watcher)
	{
		this.watcher = watcher;
	}
	
	public void setFortData(FortData fortData) {
		this.fortData = fortData;
	}

	public FortData getFortData()
	{
		return fortData;
	}

	public boolean isDrawed()
	{
		return drawed;
	}

	public void setDrawed(boolean drawed)
	{
		this.drawed = drawed;
	}

	public void setBridgeCount(int bridgeCount)
	{
		this.bridgeCount = bridgeCount;
	}

	public int getBridgeCount()
	{
		return bridgeCount;
	}

	public ZPlayerData getPlayerData()
	{
		ZPlayerData zpd = new ZPlayerData();

		zpd.setPlayerId(this.getPlayerIndex());
		zpd.setName(this.getNickname());
		zpd.setSubscriberID(this.getSubscriberID());
		zpd.setAddress(this.getAddress());
		zpd.set_flags(this.getFlags().getRaw());
		zpd.setAllyBits(this.getAllyBits());

		return zpd;
	}

	private BaseSquid priest; // Quick references to this player's priest.

	private String address = "";

	private boolean drawed; // Has this player declared draw?\

	private FortData fortData; // Fort data of this player.

	private int currentMoney;

	private int allyBits;

	private BattlePlayerFlags flags;

	private int stormPowerAccumulated;

	private int loginOrder;

	private int predictableCount;

	private int lastTechGained;

	private int lastAltarLevelGained;

	private int lastRankGained;

	private int curAltarLevel;

	private boolean watcher;

	private int bridgeCount; // How many bridge pieces has this player laid?

}
