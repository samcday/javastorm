package org.javastorm.challenge;

import org.javastorm.Flags;

public class ZonePlayerStatusFlags extends Flags
{
	public ZonePlayerStatusFlags()
	{

	}

	public ZonePlayerStatusFlags(int flags)
	{
		super(flags);
	}

	public void setValid(boolean valid)
	{
		this.setFlag(CPF_VALID, valid);
	}

	public boolean isValid()
	{
		return this.test(CPF_VALID);
	}

	public void setAccepted(boolean accepted)
	{
		this.setFlag(CPF_ACCEPTED, accepted);
	}

	public boolean isAccepted()
	{
		return this.test(CPF_ACCEPTED);
	}

	public void setToChal(boolean toChal)
	{
		this.setFlag(CPF_TOCHAL, toChal);
	}

	public boolean isToChal()
	{
		return this.test(CPF_TOCHAL);
	}

	public void setLaunch(boolean launch)
	{
		this.setFlag(CPF_LAUNCH, launch);
	}

	public boolean isLaunch()
	{
		return this.test(CPF_LAUNCH);
	}

	public void setChange(boolean change)
	{
		this.setFlag(CPF_CHANGE, change);
	}

	public boolean isChange()
	{
		return this.test(CPF_CHANGE);
	}

	public void setKicked(boolean kicked)
	{
		this.setFlag(CPF_KICKED, kicked);
	}

	public boolean isKicked()
	{
		return this.test(CPF_KICKED);
	}

	public void setConnected(boolean connected)
	{
		this.setFlag(CPF_CONNECTED, connected);
	}

	public boolean isConnected()
	{
		return this.test(CPF_CONNECTED);
	}

	public void setInitial(boolean initial)
	{
		this.setFlag(CPF_INITIAL, initial);
	}

	public boolean isInitial()
	{
		return this.test(CPF_INITIAL);
	}

	public void setBadDiag(boolean badDiag)
	{
		this.setFlag(CPF_BADDIAG, badDiag);
	}

	public boolean isBadDiag()
	{
		return this.test(CPF_BADDIAG);
	}

	public void setMaster(boolean master)
	{
		this.setFlag(CPF_MASTER, master);
	}

	public boolean isMaster()
	{
		return this.test(CPF_MASTER);
	}

	public void setToBattle(boolean toBattle)
	{
		this.setFlag(CPF_TOBATTLE, toBattle);
	}

	public boolean isToBattle()
	{
		return this.test(CPF_TOBATTLE);
	}

	public void setWatcher(boolean watcher)
	{
		this.setFlag(CPF_WATCH, watcher);
	}

	public boolean isWatcher()
	{
		return this.test(CPF_WATCH);
	}

	public void copyProtectSystemFlags(ZonePlayerStatusFlags flags)
	{
		this.copy(flags, CPF_SYSTEM_FLAGS);
	}

	public void copy(ZonePlayerStatusFlags flags)
	{
		this.copy(flags, 0);
	}

	public void copy(ZonePlayerStatusFlags flags, int mask)
	{
		super.copy(flags, mask);
	}

	private static final int CPF_VALID = (0x00000001); // player info is valid
	private static final int CPF_ACCEPTED = (0x00000002); // player has accepted in current battle
	private static final int CPF_MASTER = (0x00000004); // player is master of current battle
	private static final int CPF_KICKED = (0x00000008); // player was kicked off of current battle
	private static final int CPF_LAUNCH = (0x00000010); // launch battle request = (valid for master only);
	private static final int CPF_TOBATTLE = (0x00000020); // player decending to battle at disconnect
	private static final int CPF_INITIAL = (0x00000040); // used to distinguish updates sent to a new player from those sent to everyone
	private static final int CPF_TOCHAL = (0x00000080); // player is decending to challenge at disconnect
	private static final int CPF_DEMO = (0x00000100); // player is using a demo copy of the game
	private static final int CPF_PIRATE = (0x00000200); // player is using a pirated copy of the game = (i.e. without a cd);
	private static final int CPF_BADVER = (0x00000400); // player has a bad version. Cannot join a battle.
	private static final int CPF_BADDIAG = (0x00000800); // player failed the diagnostics, might be firewalled.
	private static final int CPF_BETA = (0x00001000); // player has a beta version
	private static final int CPF_CONNECTED = (0x00002000); // player has a beta version
	private static final int CPF_CHANGE = (0x00004000);
	private static final int CPF_WATCH = (0x00008000); // is player a watcher	

	private static final int CPF_SYSTEM_FLAGS = (CPF_VALID | CPF_MASTER | CPF_KICKED | CPF_TOBATTLE | CPF_INITIAL | CPF_DEMO | CPF_PIRATE | CPF_BADVER | CPF_BETA | CPF_BADDIAG);
	//private static final int	CPF_LOGIN_FLAGS  = (CPF_DEMO|CPF_PIRATE|CPF_BADDIAG|CPF_BETA);
}
