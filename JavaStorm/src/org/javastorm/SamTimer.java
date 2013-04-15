package org.javastorm;

public class SamTimer
{
	public SamTimer()
	{

	}

	public SamTimer(int countDown)
	{
		this.start(countDown);
	}

	public void start(float countDown)
	{
		this.timeDone = System.currentTimeMillis() + (int) (countDown * 1000);
	}

	public void start(int countDown)
	{
		this.timeDone = System.currentTimeMillis() + (countDown * 1000);
	}

	public void stop()
	{
		this.timeDone = 0;
	}

	public boolean isRunning()
	{
		return this.timeDone > 0;
	}

	public boolean isDone()
	{
		return System.currentTimeMillis() > this.timeDone;
	}

	private long timeDone;
}
