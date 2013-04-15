package org.javastorm.util;

import org.javastorm.BoardCoord;

//Byte buffer (uses ints to represent bytes to get unsigned bytes.)
public class MyByteBuffer
{
	public void truncate()
	{
		int newData[] = new int[this.position];
		for (int i = 0; i < newData.length; i++)
			newData[i] = this.buffer[i];

		this.buffer = newData;
	}

	public int getPosition()
	{
		return position;
	}

	// Allocates a new buffer.
	public void allocate(int size)
	{
		this.buffer = new int[size];
		this.position = 0;
	}

	// Moves internal pointer to new position.
	public void setPosition(int pos)
	{
		this.position = pos;
	}

	// Returns current position of internal pointer.
	public int position()
	{
		return this.position;
	}

	// Skips position by specified amount.
	public void skip(int amount)
	{
		this.position += amount;
	}

	// Stores a byte.
	public void put(int store)
	{
		this.buffer[this.position++] = store;
	}

	public void putShort(int position, int store)
	{
		int oldPos = this.position;
		this.position = position;
		this.putShort(store);
		this.position = oldPos;
	}

	// Stores a short.
	public void putShort(int store)
	{
		this.put(store & 0xFF);
		this.put((store & 0xFF00) >> 8);
	}

	// Stores a long.
	public void putInt(long store)
	{
		store &= 0xFFFFFFFF;
		this.putShort((int) store & 0xFFFF);
		this.putShort((int) (store & 0xFFFF0000) >> 16);
	}

	// Stores a float.
	public void putFloat(float store)
	{
		int storeInt = Float.floatToRawIntBits(store);

		this.putInt(storeInt);
	}

	public void put(MyByteBuffer buff)
	{
		this.put(buff, buff.size());
	}

	public void put(MyByteBuffer buff, int length)
	{
		for (int i = 0; i < length; i++)
		{
			this.put(buff.get());
		}
	}

	// Stores a long .
	public void putLong(long store)
	{
		long lowmask = ((long) 1 << 32) - 1;

		this.putInt((int) (store & lowmask));
		this.putInt((int) ((store >> 32) & lowmask));
	}

	// Stores a double.
	public void putDouble(double store)
	{
		long storeLong = Double.doubleToLongBits(store);

		this.putLong(storeLong);
	}

	// Stores a string.
	public void putStr(String string)
	{
		if (string == null)
			return;

		for (int i = 0; i < string.length(); i++)
		{
			this.put((int) string.charAt(i));
		}
	}

	// Stores a 0x00 padded string.
	public void putStr(String string, int length)
	{
		int strLength = 0;
		if (string != null)
		{
			strLength = string.length();
			for (int i = 0; i < string.length(); i++)
			{
				this.put((int) string.charAt(i));
			}
		}

		for (int i = 0; i < (length - strLength); i++)
			this.put(0);
	}

	// Stores a BoardCoord.
	public void putBC(BoardCoord bc)
	{
		this.putFloat(bc.getX());
		this.putFloat(bc.getY());
	}

	// Retrieves byte from current position.
	public int get()
	{
		assert (this.buffer != null);

		int data = this.buffer[this.position];

		this.position++;

		if (this.position > this.buffer.length)
			this.position = 0;

		return data;
	}

	public int getShort(int position)
	{
		int oldPos = this.position;
		this.position = position;
		int val = this.getShort();
		this.position = oldPos;
		return val;
	}

	// Retrives short from current position.
	public int getShort()
	{
		// Make sure enough bytes left.
		if ((this.position + 2) > this.buffer.length)
			return 0;

		int low = this.get();
		int high = this.get();

		return ((high & 0xFF) << 8) | (low & 0xFF);
	}

	// Retrives long from current position.
	public int getInt()
	{
		// Make sure enough bytes left.
		if ((this.position + 4) > this.buffer.length)
			return 0;

		int low = this.getShort();
		int high = this.getShort();

		return (high << 16) | (low);
	}

	// Retrives long from current position.
	public long getUnsignedInt()
	{
		// Make sure enough bytes left.
		if ((this.position + 4) > this.buffer.length)
			return 0;

		long low = this.getShort();
		long high = this.getShort();

		return (high << 16) | (low);
	}

	public long getLong()
	{
		long low = this.getUnsignedInt();
		long high = this.getUnsignedInt();

		return (high << 32) | (low);
	}

	// Retrieves a string from current position.
	public String getString()
	{
		String str = "";

		while (this.buffer[this.position] != 0 && this.position <= this.buffer.length)
		{
			str = str.concat(Character.toString((char) this.buffer[this.position++]));
		}

		this.position++;

		return str;
	}

	// Retrieves a string from current position.
	public String getString(int len)
	{
		String str = "";
		int read = 0;

		while ((this.position <= this.buffer.length) && (read < len))
		{
			if (this.buffer[this.position] == 0)
				break;

			str = str.concat(Character.toString((char) this.buffer[this.position++]));
			read++;
		}

		this.position++;

		return str;
	}

	// Retrieves a string from current position.
	public String getPaddedString(int len)
	{
		String str = "";
		int read = 0;

		while ((this.buffer[this.position] != 0 && this.position <= this.buffer.length) && (read < len))
		{
			str = str.concat(Character.toString((char) this.buffer[this.position++]));
			read++;
		}

		this.skip(len - read);

		return str;
	}

	// Retrives float from current position.
	public float getShortFloat()
	{
		// Make sure enough bytes left.
		if ((this.position + 4) > this.buffer.length)
			return 0;

		return Float.intBitsToFloat(this.getShort());
	}

	public float getFloat()
	{
		// Make sure enough bytes left.
		if ((this.position + 4) > this.buffer.length)
			return 0;

		return Float.intBitsToFloat(this.getInt());
	}

	public double getDouble()
	{
		// Make sure enough bytes left.
		if ((this.position + 8) > this.buffer.length)
			return 0;

		long value = this.getLong();
		return Double.longBitsToDouble(value);
	}

	// Returns size of buffer.
	public int size()
	{
		if (this.buffer == null)
			return 0;

		return this.buffer.length;
	}

	// Returns how many bytes available from current position.
	public int available()
	{
		return this.buffer.length - position;
	}

	private int position;

	private int buffer[];
}