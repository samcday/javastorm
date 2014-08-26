package org.javastorm;

public class Event
{
	public Event()
	{
		this.key = -1;
	}

	public boolean isKey()
	{
		return this.key > -1;
	}

	public boolean isMouse()
	{
		return this.isMouse;
	}

	public void setMouse()
	{
		this.isMouse = true;
	}

	public boolean isLeftClick()
	{
		return this.isLeftClick;
	}

	public void setLeftClick()
	{
		this.isLeftClick = true;
	}

	public int getKey()
	{
		return this.key;
	}

	public void setKey(int key)
	{
		this.key = key;
	}

	public boolean isUp()
	{
		return !this.keyDown;
	}

	public boolean isKeyDown()
	{
		return this.keyDown;
	}

	public void setKeyDown(boolean down)
	{
		this.keyDown = down;
	}

	private boolean isMouse;

	private boolean isLeftClick;

	private int key;

	private boolean keyDown;
}
