package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidConstructor;

public class OutpostType extends MainSquid
{
	public OutpostType(World world, int sid)
	{
		super(world, sid);
	}

	public void notifyPostPop(int popFlags)
	{
		if ((popFlags & BaseSquid.pfCREATED) > 0)
		{
			//NSMainSquid under = this._container.getSquidHash().getGridSid(this.getPos());
			//NSFlooder flooder = new NSFlooder(this._container);
			//flooder.startFloodPlayerId(under.getPos(), this.getPlayerId());
		}
	}

	public static class NSOutpostConstructor implements SquidConstructor
	{
		public BaseSquid construct(World container, int sid)
		{
			return new OutpostType(container, sid);
		}
	}
}
