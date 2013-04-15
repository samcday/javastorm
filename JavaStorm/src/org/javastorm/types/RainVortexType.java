package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.process.RegularProcess;
import org.javastorm.renderer.Renderer;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.SquidConstructor;

public class RainVortexType extends VortexType
{
	public RainVortexType(World world, int sid)
	{
		super(world, sid);
		this.setOverloadDraw(true);
	}

	public void notifyPostPop(int popFlags)
	{
		super.notifyPostPop(popFlags);

		if ((popFlags & BaseSquid.pfCREATED) > 0)
		{
			new RegularProcess(this._container, VortexType.trcLOOP, this, 0.08f);
		}
	}

	public void draw(Renderer renderer, int x, int y)
	{
		renderer.draw(this, x, y, 0);
		renderer.draw(this, x, y, this.getFrame());
	}

	public static class NSRainVortexConstructor implements SquidConstructor
	{
		public BaseSquid construct(World container, int sid)
		{
			return new RainVortexType(container, sid);
		}
	}
}
