package org.javastorm.battle;

import org.javastorm.Flags;

public class BattlePlayerFlags extends Flags
{
	public BattlePlayerFlags()
	{

	}

	public void setCurrentlyLoggedIn(boolean toggle)
	{
		this.setFlag(PF_CURRENTLY_LOGGED_IN, toggle);
	}
	
	public boolean isCurrentlyLoggedIn()
	{
		return this.test(PF_CURRENTLY_LOGGED_IN);
	}
	
	public void setEverLoggedIn(boolean toggle)
	{
		this.setFlag(PF_EVER_LOGGED_IN, toggle);
	}
	
	public boolean hasEverLoggedIn()
	{
		return this.test(PF_EVER_LOGGED_IN);
	}
	
	public void setCompletedCleanShutdown(boolean toggle)
	{
		this.setFlag(PF_COMPLETED_CLEAN_SHUTDOWN, toggle);
	}
	
	public boolean hasCompletedCleanShutdown()
	{
		return this.test(PF_COMPLETED_CLEAN_SHUTDOWN);
	}

	public void setServer(boolean toggle)
	{
		this.setFlag(PF_SERVER, toggle);
	}
	
	public boolean isServer()
	{
		return this.test(PF_SERVER);
	}

	public void setTookOver(boolean toggle)
	{
		this.setFlag(PF_TOOKOVER, toggle);	
	}
	
	public boolean hasTookOver()
	{
		return this.test(PF_TOOKOVER);
	}
	
	public void setIsAI(boolean toggle)
	{
		this.setFlag(PF_IS_AI, toggle);
	}
	
	public boolean isAI()
	{
		return this.test(PF_IS_AI);
	}
	
	public void setReadyToRock(boolean toggle)
	{
		this.setFlag(PF_READY_TO_ROCK, toggle);
	}
	
	public boolean isReadyToRock()
	{
		return this.test(PF_READY_TO_ROCK);
	}
	
	public void setDrawDeclared(boolean toggle)
	{
		this.setFlag(PF_DRAW_DECLARED, toggle);
	}
	
	public boolean isDrawDeclared()
	{
		return this.test(PF_DRAW_DECLARED);
	}
	
	public void setDisconnected(boolean toggle)
	{
		this.setFlag(PF_DISCONNECTED, toggle);
	}
	
	public boolean isDisconnected()
	{
		return this.test(PF_DISCONNECTED);
	}
	
	public void setFirewalled(boolean toggle)
	{
		this.setFlag(PF_FIREWALLED, toggle);
	}
	
	public boolean isFirewalled()
	{
		return this.test(PF_FIREWALLED);
	}
	
	public void setCheater(boolean toggle)
	{
		this.setFlag(PF_CHEATER, toggle);
	}
	
	public boolean isCheater()
	{
		return this.test(PF_CHEATER);
	}

	private static final int PF_CURRENTLY_LOGGED_IN      = (0x01);
	private static final int PF_EVER_LOGGED_IN           = (0x02);
	private static final int PF_COMPLETED_CLEAN_SHUTDOWN = (0x04);
	private static final int PF_SERVER                   = (0x08);
	private static final int PF_TOOKOVER                 = (0x10);
	private static final int PF_IS_AI                    = (0x20);
	private static final int PF_READY_TO_ROCK            = (0x40);
	private static final int PF_DRAW_DECLARED            = (0x80);
	private static final int PF_DISCONNECTED             = (0x100);
	private static final int PF_FIREWALLED               = (0x200);
	private static final int PF_CHEATER                  = (0x400);
	
	public static final int PF_DONT_CLEAR_ON_RESTART = PF_EVER_LOGGED_IN | PF_DISCONNECTED | PF_FIREWALLED | PF_CHEATER;
}
