package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.FormSquid;
import org.javastorm.squids.SquidConstructor;

public class DependForm extends FormSquid
{
	public DependForm(World world, int sid)
	{
		super(world, sid);
	}
	
	public static class NSDependFormConstructor implements SquidConstructor
	{
		public BaseSquid construct(World container, int sid)
		{
			System.out.println("NSDependFormConstructor.");
			return new DependForm(container, sid);
		}
	}
}
