package org.javastorm;

public class UCTimer
{
	private TimeMaster gtime;

	public UCTimer(TimeMaster _gtime)
	{
		gtime = _gtime;
		stop();
	}

	public FullTime timeDone;

	public void start(FullTime countDown)
	{
		timeDone = new FullTime(gtime.getUncorrectedTime().time + countDown.time);
	}

	public void start(float countDown)
	{
		timeDone = new FullTime(gtime.getUncorrectedTime().time + (double) countDown);
	}

	public void start(double countDown)
	{
		timeDone = new FullTime(gtime.getUncorrectedTime().time + countDown);
	}

	public void start(int countDown)
	{
		timeDone = new FullTime(gtime.getUncorrectedTime().time + (double) countDown);
	}

	public UCTimer(TimeMaster _gtime, FullTime countDown)
	{
		gtime = _gtime;
		start(countDown);
	}

	public UCTimer(TimeMaster _gtime, float countDown)
	{
		gtime = _gtime;
		start(countDown);
	}

	public UCTimer(TimeMaster _gtime, double countDown)
	{
		gtime = _gtime;
		start(countDown);
	}

	public UCTimer(TimeMaster _gtime, int countDown)
	{
		gtime = _gtime;
		start(countDown);
	}

	public int isDone()
	{
		if (gtime.getUncorrectedTime().time >= timeDone.time)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	public int isRunning()
	{
		if (timeDone.time != 0.0)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	public void stop()
	{

		timeDone.time = 0.0;
	}
}
