package org.javastorm.squids;

import org.javastorm.World;

public interface SquidConstructor
{
	public BaseSquid construct(World container, int sid);
}
