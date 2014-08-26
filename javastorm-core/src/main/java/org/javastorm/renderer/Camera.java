package org.javastorm.renderer;

public class Camera
{
	public void move(int bx, int by)
	{
		this.setX(this.x + bx);
		this.setY(this.y + by);
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
