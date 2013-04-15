package org.javastorm.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// Like the MyInputStream, this subclass of bufferedouputstream has a few utility functions I find
// most helpful. Also encodes.
public class MyOutputStream extends BufferedOutputStream
{
	public MyOutputStream(OutputStream stream)
	{
		super(stream);
	}

	// Destroys all data written to this stream since the last flush()
	// use this if you failed midway during a write.
	public void clean()
	{
		this.count = 0;
	}

	public void writeShort(int data) throws IOException
	{
		// Little endian.
		this.write(data & 0xFF);
		this.write((data & 0xFF00) >> 8);
	}

	public void writeLong(long data) throws IOException
	{
		// Write on order of least byte up to highest.
		this.write((int) data & 0xFF);
		this.write((int) (data & 0xFF00) >> 8);
		this.write((int) (data & 0xFF0000) >> 16);
		this.write((int) (data & 0xFF000000) >> 24);
	}

	public void writeStr(String str) throws IOException
	{
		for (int i = 0; i < str.length(); i++)
		{
			this.write((int) str.charAt(i));
		}
	}

	public void writeFloat(float data) throws IOException
	{
		// Get the raw float data in an int.
		int dataInt = Float.floatToRawIntBits(data);

		// Write on order of least byte up to highest.
		this.write((int) dataInt & 0xFF);
		this.write((int) (dataInt & 0xFF00) >> 8);
		this.write((int) (dataInt & 0xFF0000) >> 16);
		this.write((int) (dataInt & 0xFF000000) >> 24);
	}
}