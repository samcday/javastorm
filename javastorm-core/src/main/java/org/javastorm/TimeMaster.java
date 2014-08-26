package org.javastorm;

// this class will handle sync and unsync cases
// basicly a mop bucket for the globals of fulltime.c
public class TimeMaster
{
	public long milsAtStart = 0;

	public long secsAtStart = 0;

	public int globalFrameNumber = 0;

	public int serverDiffMils = 0;

	public FullTime frameTime = new FullTime(0.0);

	public FullTime uncorrectedFrameTime = new FullTime(0.0);

	public FullTime lastFrameElapsedTime = new FullTime(0.0);

	public FullTime firstTime = new FullTime(0.0);

	public int timeStopped = 0;

	public FullTime whenStopped = new FullTime(0.0);

	public FullTime timeStoppedAccumulator = new FullTime(0.0);

	public double thisFPS = 0.0;

	public double avgFPS = 0.0;

	public double avgSPF = 0.0;

	public static final int FRAMES_TO_AVG = 10;

	public FullTime[] frameTimes = new FullTime[FRAMES_TO_AVG];

	// easyer realistic prototype 
	public SecsAndMils getSecsAndMils(int correct)
	{
		return getSecsAndMils(0, 0, correct);
	}

	// keep this for the function sake
	public SecsAndMils getSecsAndMils(long secs, long mils, int correct)
	{
		int _serverDiffMils = 0;
		if (correct == 1)
		{
			_serverDiffMils = serverDiffMils;
		}

		long _mils = System.currentTimeMillis() - milsAtStart;
		_mils -= _serverDiffMils;
		_mils += secsAtStart * 1000;
		secs = _mils / 1000;
		mils = _mils % 1000;

		SecsAndMils myretval = new SecsAndMils();
		myretval.secs = secs;
		myretval.mils = mils;
		return myretval;
	}

	public FullTime getRealTime()
	{
		//assert( serverDiffMils == 0.0 || timeStoppedAccumulator == 0.0 );
		// If the time synchronizer is active, you can not
		// use the time stopper.  Be sure to call resetStoppedTime()
		// before using the time synchronizer
		if (timeStopped == 1)
		{
			return whenStopped;
		}
		SecsAndMils mytime = new SecsAndMils();
		mytime = getSecsAndMils(1);
		return new FullTime(new FullTime((double) mytime.secs + (double) mytime.mils / 1000.0).time - timeStoppedAccumulator.time);
	}

	public FullTime getUncorrectedTime()
	{
		SecsAndMils mytime = new SecsAndMils();
		mytime = getSecsAndMils(0);
		return new FullTime((double) mytime.secs + (double) mytime.mils / 1000.0);
	}

	public int isTimeStopped()
	{
		if (timeStopped > 0)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	public void stopTime()
	{
		if (timeStopped == 0)
		{
			whenStopped = getRealTime();
		}
		++timeStopped;
	}

	public void resumeTime()
	{
		if (timeStopped == 1)
		{
			--timeStopped;
			if (timeStopped == 0)
			{
				timeStoppedAccumulator.time += getRealTime().time - whenStopped.time;
			}
		}
	}

	public void resetStoppedTime()
	{
		timeStoppedAccumulator.time = 0.0;
	}

	public void setFrameTime()
	{
		lastFrameElapsedTime = frameTime;
		// A temporary storage place

		// Set the frameTime and uncorrectedFrameTime
		frameTime = getRealTime();
		uncorrectedFrameTime = getUncorrectedTime();

		if (firstTime.time == 0.0)
		{
			firstTime = frameTime;
		}

		lastFrameElapsedTime.time = frameTime.time - lastFrameElapsedTime.time;

		// Stick uncorrected time into the rolling queue for averaging purposes

		frameTimes[globalFrameNumber % FRAMES_TO_AVG] = uncorrectedFrameTime;

		// Set the current fps as long as we have one sample
		if (globalFrameNumber > 0)
		{
			// For some reason, two frames were reporting identical times,
			// leading to a divide by zero.  We check for this now.
			double frameDelta = (uncorrectedFrameTime.time - frameTimes[(globalFrameNumber + FRAMES_TO_AVG - 1) % FRAMES_TO_AVG].time);

			// NOTE: -1.0 is a magic value used only when we would divide by zero
			thisFPS = -1.0;
			if (frameDelta > 0.0)
			{
				thisFPS = 1.0 / frameDelta;
			}
		}

		// Set the average as long as we have enough samples
		if (globalFrameNumber > FRAMES_TO_AVG)
		{
			double elapsed = uncorrectedFrameTime.time - frameTimes[(globalFrameNumber + 1) % FRAMES_TO_AVG].time;

			// NOTE: -1.0 is a magic value used only when we would divide by zero
			avgFPS = -1.0;
			if (elapsed > 0.0)
			{
				avgFPS = (double) FRAMES_TO_AVG / elapsed;
			}
			avgSPF = 1.0 / avgFPS;
		}

		globalFrameNumber++;
	}

	public void initFullTime()
	{
		SecsAndMils mytime = new SecsAndMils();
		mytime = getSecsAndMils(0);
		milsAtStart = mytime.mils;
		secsAtStart = mytime.secs;
		setFrameTime();
	}

	public void sleepMils(int mils)
	{
		try
		{
			Thread.sleep(mils);
		}
		catch (Throwable t)
		{
		}
	}

}
