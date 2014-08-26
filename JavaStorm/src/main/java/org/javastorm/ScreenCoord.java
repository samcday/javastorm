package org.javastorm;

public class ScreenCoord
{
	public ScreenCoord()
	{
		this.x = this.y = 0;
	}

	public ScreenCoord(ScreenCoord sc)
	{
		this.x = sc.getX();
		this.y = sc.getY();
	}

	public ScreenCoord(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void translate(int bx, int by)
	{
		this.x += bx;
		this.y += by;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	private int x;

	private int y;
}
