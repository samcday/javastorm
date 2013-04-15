package org.javastorm.network.commands;

import org.javastorm.network.Connection;
import org.javastorm.util.MyByteBuffer;

public class ZSendBucks extends NSCommand
{
	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public boolean isRefund()
	{
		return isRefund;
	}

	public void setIsRefund(boolean isRefund)
	{
		this.isRefund = isRefund;
	}

	public int getSourcePlayerId()
	{
		return sourcePlayerId;
	}

	public void setSourcePlayerId(int sourcePlayerId)
	{
		this.sourcePlayerId = sourcePlayerId;
	}

	public int getTargetPlayerId()
	{
		return targetPlayerId;
	}

	public void setTargetPlayerId(int targetPlayerId)
	{
		this.targetPlayerId = targetPlayerId;
	}

	public MyByteBuffer getCommandData()
	{
		return null;
	}

	public int getCommandID()
	{
		return Connection.ZSendBucks;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.targetPlayerId = buffer.get();
		this.sourcePlayerId = buffer.get();
		this.isRefund = buffer.get() == 1 ? true : false;
		this.amount = buffer.getInt();
	}

	private int targetPlayerId;

	private int sourcePlayerId;

	private boolean isRefund;

	private int amount;
}
