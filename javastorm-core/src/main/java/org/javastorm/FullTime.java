package org.javastorm;

public class FullTime
{
	public static final double ftINVALID = 0.0;

	public double time;

	public FullTime()
	{
		time = ftINVALID;
	}

	public FullTime(double f)
	{
		time = f;
	}

	// Not all machines use the same floating point encodings
	// so it may sometimes be necessary for portability to
	// convert a full time to fixed point before transmitting
	public int getFixedPoint()
	{
		int t = (int) time;
		return (((int) (time - t)) * 2 * 2 * 2) | (t << 3);
	}

	public void setFixedPoint(int t)
	{
		time = (double) (t >> 3) + (double) (t & 0x7) / 8.0;
	}

}
