package org.javastorm;

public class Rect
{
	public Rect()
	{

	}

	public Rect(int l, int t, int r, int b)
	{
		this.l = l;
		this.t = t;
		this.r = r;
		this.b = b;
	}

	public void clipTo(int l, int t, int r, int b)
	{
		if (this.l < l)
			this.l = l;
		if (this.t < t)
			this.t = t;
		if (this.r > r)
			this.r = r;
		if (this.b > b)
			this.b = b;
	}

	public boolean isValid()
	{
		return (this.l < this.r) && (this.t < this.b);
	}

	public int getWidth()
	{
		return (int) (this.r - this.l);
	}

	public int getHeight()
	{
		return (int) (this.b - this.t);
	}

	public int getB()
	{
		return b;
	}

	public void setB(int b)
	{
		this.b = b;
	}

	public int getL()
	{
		return l;
	}

	public void setL(int l)
	{
		this.l = l;
	}

	public int getR()
	{
		return r;
	}

	public void setR(int r)
	{
		this.r = r;
	}

	public int getT()
	{
		return t;
	}

	public void setT(int t)
	{
		this.t = t;
	}

	private int t, l, b, r;
}
