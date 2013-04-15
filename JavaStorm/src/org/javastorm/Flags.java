package org.javastorm;

public class Flags
{
	public Flags()
	{

	}

	public Flags(int flags)
	{
		this.flags = flags;
	}

	public Flags(Flags other)
	{
		this.flags = other.getRaw();
	}

	public void setRaw(int flags)
	{
		this.flags = flags;
	}

	public int getRaw()
	{
		return this.flags;
	}

	protected boolean test(int flag)
	{
		return (this.flags & flag) > 0;
	}

	protected void copy(Flags flags, int mask)
	{
		this.flags &= mask;
		this.flags |= flags.getRaw() & ~mask;
	}

	protected void setFlag(int flag, boolean toggle)
	{
		if (toggle)
			this.flags |= flag;
		else
			this.flags &= ~flag;
	}

	private int flags;
}
