package org.javastorm;

public class StaticSidList extends IntList
{
	private World world;

	public StaticSidList(World myworld, int spaceToAllocate)
	{
		super(spaceToAllocate);
		world = myworld;
		world.registerSidList(this);
	}
}
