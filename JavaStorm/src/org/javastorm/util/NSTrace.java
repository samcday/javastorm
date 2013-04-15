package org.javastorm.util;

public class NSTrace
{
	public void Trace(int _traceLevel, String arg0, Object... arg1)
	{
		if (_traceLevel < 0)
		{
			return;
		}
		if (_traceLevel <= TraceLevel)
		{
			System.out.format(arg0, arg1);
		}
	}

	public int TraceLevel = TL_TRACE;

	public static final int TL_ALWAYS = 0;

	public static final int TL_ERROR = 9;

	public static final int TL_WARNING = 19;

	public static final int TL_TRACE = 29;

	public static final int TL_DEBUG = 39;
}
