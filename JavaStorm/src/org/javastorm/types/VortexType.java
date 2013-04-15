package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;

public class VortexType extends MainSquid
{
	public VortexType(World world, int sid)
	{
		super(world, sid);
	}

	public float regularCall(int id, int counter, float period)
	{
		switch (id)
		{
			case trcLOOP:
			{
				if (this.getFrame() > 13)
				{
					this.setFrame(1);
				}
				else
				{
					this.setFrame(this.getFrame() + 1);
				}

				return period;
			}
		}

		return super.regularCall(id, counter, period);
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

	public static final int trcLOOP = 0;
}
