package org.javastorm.graph;

public class Graph
{
	public Graph(int index)
	{
		this.init(index);
	}

	public void init()
	{
		this.init(this.index);
	}

	public void init(int index)
	{
		this.numSurfaces = 0;
		this.inUse = false;
		this.index = index;
	}

	public boolean everInUse()
	{
		return this.everInUse;
	}

	public boolean isInUse()
	{
		return this.inUse;
	}

	public void setInUse(boolean inUse)
	{
		this.inUse = inUse;
	}

	public int getNumSurfaces()
	{
		return this.numSurfaces;
	}

	public void setNumSurfaces(int numSurfaces)
	{
		this.numSurfaces = numSurfaces;
	}

	public int getIndex()
	{
		return this.index;
	}

	public void incNumSurfaces()
	{
		this.numSurfaces++;
	}

	public void decNumSurfaces()
	{
		this.numSurfaces--;
	}

	public void removeSurface()
	{
		if (!this.inUse || this.numSurfaces <= 0)
		{
			return;
		}

		this.numSurfaces--;

		if (this.numSurfaces == 0)
		{
			this.init(this.index);
		}
	}

	private boolean everInUse;

	private boolean inUse;

	private int numSurfaces;

	private int index;
}
