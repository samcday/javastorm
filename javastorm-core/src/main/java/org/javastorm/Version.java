package org.javastorm;

public class Version
{
	public Version(Version version)
	{
		this(version.getMajorVersion(), version.getMinorVersion());
	}

	public Version(int major, int minor)
	{
		this.major = major;
		this.minor = minor;
	}

	public int getMajorVersion()
	{
		return this.major;
	}
	
	public int getMinorVersion()
	{
		return this.minor;
	}

	public String toString()
	{
		return major + "." + minor;
	}

	private int major;
	private int minor;
}
