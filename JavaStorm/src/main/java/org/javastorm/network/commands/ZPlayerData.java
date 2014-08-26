package org.javastorm.network.commands;

import org.javastorm.battle.BattlePlayer;
import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZPlayerData extends NSCommand
{
	public int get_flags()
	{
		return _flags;
	}

	public void set_flags(int _flags)
	{
		this._flags = _flags;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
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

	public int getStormPowerAccumulated()
	{
		return stormPowerAccumulated;
	}

	public void setStormPowerAccumulated(int stormPowerAccumulated)
	{
		this.stormPowerAccumulated = stormPowerAccumulated;
	}

	public int getAllyBits()
	{
		return allyBits;
	}

	public void setAllyBits(int allyBits)
	{
		this.allyBits = allyBits;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getSubscriberID()
	{
		return subscriberID;
	}

	public void setSubscriberID(int subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	public int getCommandID()
	{
		return Connection.ZPlayerData;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	@Override
	public String[] outputDebug() {
		return new String[] {
			"allyBits = " + this.allyBits,
			"subscriberID = " + this.subscriberID,
			"_flags = " + this._flags,
			"stormPowerAccumulated = " + this.stormPowerAccumulated,
			"playerId = " + this.playerId,
			"loginOrder = " + this.loginOrder,
			"predictableCount = " + this.predictableCount,
			"lastTechGained = " + this.lastTechGained,
			"lastAltarLevelGained = " + this.lastAltarLevelGained,
			"lastRankGained = " + this.lastRankGained,
			"curAltarLevel = " + this.curAltarLevel,
			"nickname = " + this.name
		};
	}

	public void updatePlayer(BattlePlayer player)
	{
		player.setAllyBits(this.allyBits);
		player.setSubscriberID(this.subscriberID);
		player.getFlags().setRaw(this._flags);
		player.setStormPowerAccumulated(this.stormPowerAccumulated);
		player.setPlayerIndex(this.playerId);
		player.setLoginOrder(this.loginOrder);
		player.setPredictableCount(this.predictableCount);
		player.setLastTechGained(this.lastTechGained);
		player.setLastAltarLevelGained(this.lastAltarLevelGained);
		player.setLastRankGained(this.lastRankGained);
		player.setCurAltarLevel(this.curAltarLevel);
		player.setNickname(this.name);
	}

	public void setPlayer(BattlePlayer player)
	{
		this.allyBits = player.getAllyBits();
		this.subscriberID = player.getSubscriberID();
		this._flags = player.getFlags().getRaw();
		this.stormPowerAccumulated = player.getStormPowerAccumulated();
		this.playerId = player.getPlayerIndex();
		this.loginOrder = player.getLoginOrder();
		this.predictableCount = player.getPredictableCount();
		this.lastTechGained = player.getLastTechGained();
		this.lastAltarLevelGained = player.getLastAltarLevelGained();
		this.lastRankGained = player.getLastRankGained();
		this.curAltarLevel = player.getCurAltarLevel();
		this.name = player.getNickname();
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		buffer.setPosition(0);

		this.address = buffer.getString(30);

		buffer.skip(30 - this.address.length() - 1);

		this.allyBits = buffer.getInt();
		this.subscriberID = buffer.getInt();
		this._flags = buffer.getInt();
		this.stormPowerAccumulated = buffer.getInt();

		this.playerId = buffer.get();
		buffer.get();
		this.loginOrder = buffer.get();
		this.predictableCount = buffer.getShort();
		this.lastTechGained = buffer.get();
		this.lastAltarLevelGained = buffer.get();
		this.lastRankGained = buffer.get();
		this.curAltarLevel = buffer.get();
		this.name = buffer.getString();
	}

	public MyByteBuffer getCommandData()
	{
		MyByteBuffer buffer = new MyByteBuffer();
		buffer.allocate(55 + this.name.length() + 1);

		buffer.putStr(this.address, 30);
		buffer.putInt(this.allyBits);
		buffer.putInt(this.subscriberID);

		buffer.putInt(this._flags);
		buffer.putInt(this.stormPowerAccumulated);

		buffer.put(this.playerId);
		buffer.put(this.name.length() + 1);
		buffer.put(this.loginOrder);

		buffer.putShort(this.predictableCount);
		buffer.put(this.lastTechGained);
		buffer.put(this.lastAltarLevelGained);
		buffer.put(this.lastRankGained);
		buffer.put(this.curAltarLevel);

		buffer.putStr(this.name);
		buffer.put(0);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	private String address;

	private int allyBits;

	private int subscriberID;

	private int _flags;

	private int stormPowerAccumulated;

	private int playerId;

	private int loginOrder;

	private int predictableCount;

	private int lastTechGained;

	private int lastAltarLevelGained;

	private int lastRankGained;

	private int curAltarLevel;

	private String name;
}
