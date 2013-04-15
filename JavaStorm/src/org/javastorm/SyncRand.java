package org.javastorm;

public class SyncRand
{
	public SyncRand()
	{
		this.last = 0;
	}

	public SyncRand(int seed)
	{
		this.last = seed;
	}

	public int randomize(int seedWithTime)
	{
		// TODO.
		return 0;
	}

	public int rand()
	{
		if (last == 0)
		{
			last = 0x0bad0bad;
		}

		last = (last * 65539 + 3);
		return (last >> 16) & 0xFF;
	}

	public void seed(int seed)
	{
		this.last = seed;
	}

	public int get(int maxNumber)
	{
		return this.rand() % maxNumber;
	}

	public int get(int minNumber, int maxNumber)
	{
		return minNumber + this.get(maxNumber - minNumber);
	}

	private int last;
}
