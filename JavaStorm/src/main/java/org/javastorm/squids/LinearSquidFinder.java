package org.javastorm.squids;

import org.javastorm.World;

public class LinearSquidFinder
{
	public LinearSquidFinder(World world)
	{
		this.world = world;
		this.index = 1;
		this.numSquids = this.world.getNumSquids();
		this.findNext();
	}

	public void findNext()
	{
		this.index++;

		while (this.index < this.numSquids && this.world.getSquid(this.index) == null)
			index++;
	}

	public boolean isValid()
	{
		return this.index < this.numSquids;
	}

	public BaseSquid s()
	{
		return this.world.getSquid(this.index);
	}

	private World world;

	private int index;

	private int numSquids;
}
