package org.javastorm.tarc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream
{
	public ByteBufferInputStream(ByteBuffer buf)
	{
		this.pos = 0;
		this.buf = buf;
	}

	public int read() throws IOException
	{
		this.pos++;
		return toUnsigned(buf.get());
	}

	public int available() throws IOException
	{
		return buf.capacity() - pos;
	}

	private static final int toUnsigned(byte b)
	{
		if (b < 0)
			return b + 256;
		else
			return b;
	}

	public void close() throws IOException
	{
		this.buf = null;
	}

	public long skip(long n) throws IOException
	{
		this.pos += (int) n;
		this.buf.position(this.pos);
		return n;
	}

	private int pos;

	private ByteBuffer buf;
}
