package org.javastorm.fort;

import org.javastorm.util.MyByteBuffer;

public class Section extends MyByteBuffer
{
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Section getNext()
	{
		return next;
	}

	public void setNext(Section next)
	{
		this.next = next;
	}

	public Section(String name)
	{
		this.name = name;
	}

	private String name;

	private Section next;
}
