package org.javastorm.util;

public class MarkedByteBuffer extends MyByteBuffer
{
	public MarkedByteBuffer()
	{
		this.mark = -1;
	}

	public void setMark()
	{
		this.endMark();
		this.mark = this.position();
		this.putShort(0);
	}

	public int getMark()
	{
		assert this.mark != -1;
		return this.getShort(this.mark);
	}

	public void verifyMark()
	{
		this.mark = this.position();
		this.skip(2);
	}

	public void advanceMark(int reps)
	{
		while (reps > 0)
		{
			this.setPosition(this.mark + this.getMark());
			this.mark = this.position();
			reps--;
		}

		this.skip(2);
	}

	public void endMark()
	{
		if (this.mark != -1)
		{
			this.putShort(this.mark, this.position() - this.mark);
		}
	}

	private int mark;
}
