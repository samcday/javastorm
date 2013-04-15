package org.javastorm;

public class IntList
{
	public IntList(int spaceToAllocate)
	{
		this.allocate(spaceToAllocate);
	}

	public void allocate(int spaceToAllocate)
	{
		this.list = new int[spaceToAllocate];
		this.maxAllowed = spaceToAllocate;
		this.total = 0;
	}

	public boolean isEmpty()
	{
		return this.total == 0;
	}

	public boolean isFull()
	{
		return this.total >= this.maxAllowed;
	}

	public void reset()
	{
		this.total = 0;
	}

	public void copy(IntList other)
	{
		for (int i = 0; i < other.getTotal(); i++)
		{
			this.add(other.get(i));
		}
	}

	public void add(int sid)
	{
		if (this.total < this.maxAllowed)
			this.list[this.total++] = sid;
	}

	public void addUnique(int sid)
	{
		if (this.total < this.maxAllowed)
		{
			if (!this.contains(sid))
			{
				this.list[this.total++] = sid;
			}
		}
	}

	public int get(int i)
	{
		return this.list[i % this.total];
	}

	public boolean contains(int sid)
	{
		for (int i = 0; i < this.total; i++)
		{
			if (this.list[i] == sid)
				return true;
		}

		return false;
	}

	public int count(int sid)
	{
		int count = 0;

		for (int i = 0; i < this.total; i++)
		{
			if (this.list[i] == sid)
				count++;
		}

		return count;
	}

	public int getRandom()
	{
		if (this.total == 0)
			return 0;

		return this.list[NSUtil.fastRandomInt(this.total)];
	}

	public int getRandom(SyncRand random)
	{
		if (this.total == 0)
			return 0;

		return this.list[random.get(this.total)];
	}

	public int killAll(int killMe)
	{
		int found = 0;

		for (int t = 0; t < this.total; ++t)
		{
			if (this.list[t] == killMe)
			{
				++found;
			}
			else
			{
				this.list[t - found] = this.list[t];
			}
		}

		this.total -= found;
		return found;
	}

	public int getTotal()
	{
		return this.total;
	}

	private int list[];

	private int maxAllowed;

	private int total;
}
