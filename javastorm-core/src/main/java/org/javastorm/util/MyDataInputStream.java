package org.javastorm.util;

import java.io.IOException;
import java.io.InputStream;

// Just a little inputstream I made, has a couple of things I needed.
// Also takes care of encoding.
public class MyDataInputStream extends InputStream
{
	public MyDataInputStream(InputStream is)
	{
		this.is = is;
	}

	public int available() throws IOException
	{
		return this.is.available();
	}

	public int tell()
	{
		return this.readCount;
	}

	public int read() throws IOException
	{
		int val = this.is.read();
		this.readCount++;

		if (this.encodeString != null)
		{
			val ^= this.encodeString[this.currentEncodeIndex++];

			if (this.currentEncodeIndex == this.encodeString.length)
				this.currentEncodeIndex = 0;
		}

		return val;
	}

	public long skip(long bytes) throws IOException
	{
		this.currentEncodeIndex += bytes;
		return super.skip(bytes);
	}

	public void close() throws IOException
	{
		this.is.close();
	}

	public int readShort() throws IOException
	{
		int low = 0;
		int high = 0;

		low = this.read();
		high = this.read();

		return ((high & 0xFF) << 8) | (low & 0xFF);
	}

	public int readInt() throws IOException
	{
		int lowlow = 0;
		int lowhigh = 0;
		int highlow = 0;
		int highhigh = 0;

		lowlow = this.read();
		lowhigh = this.read();
		highlow = this.read();
		highhigh = this.read();

		return (((highhigh & 0xFF) << 24) | ((highlow & 0xFF) << 16) | ((lowhigh & 0xFF) << 8) | (lowlow & 0xFF));
	}

	public float readFloat() throws IOException
	{
		int lowlow = 0;
		int lowhigh = 0;
		int highlow = 0;
		int highhigh = 0;

		lowlow = this.read();
		lowhigh = this.read();
		highlow = this.read();
		highhigh = this.read();

		return Float.intBitsToFloat(((highhigh & 0xFF) << 24) | ((highlow & 0xFF) << 16) | ((lowhigh & 0xFF) << 8) | (lowlow & 0xFF));
	}

	public String readString(int length) throws IOException
	{
		String str = "";

		int tempChar;
		int readCount = 0;

		while (((tempChar = this.read()) != 0) && readCount < length - 1)
		{
			str = str.concat(Character.toString((char) tempChar));
			readCount++;
		}

		return str;
	}

	public String readLine() throws IOException
	{
		String str = "";

		int tempChar;

		while ((tempChar = this.read()) != 0)
		{
			str = str.concat(Character.toString((char) tempChar));
		}

		return str;
	}

	public void setEncodeString(String encode)
	{
		this.encodeString = encode.toCharArray();
		this.currentEncodeIndex = 0;
	}

	public void resetEncode()
	{
		this.currentEncodeIndex = 0;
	}

	private int readCount;

	private InputStream is;

	private char[] encodeString = null;

	private int currentEncodeIndex = 0;
}
