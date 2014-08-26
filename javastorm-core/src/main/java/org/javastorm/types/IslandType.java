package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidConstructor;

public class IslandType extends MainSquid
{
	public IslandType(World world, int sid)
	{
		super(world, sid);
	}

	public void notifyPostPop(int popFlags)
	{
		if ((popFlags & BaseSquid.pfCREATED) > 0)
		{
			System.out.println("...");
			System.exit(-1);
			/*Box s = new Box("Isle" + this.hashCode(), new Vector3f(0, 0, 0), .5f, .5f, .5f);
			s.setLocalTranslation(0, 0, 0);
			s.setModelBound(new BoundingBox());
			s.updateModelBound();
			this.attachChild(s);

			Vector3f newLocal = new Vector3f(this.localTranslation);
			newLocal.y --;
			this.setLocalTranslation(newLocal);*/

			/*NSType islandStalag = NSTypes.findByTypeName("islandStalag");
			NSMainSquid s = (NSMainSquid)this._container.createSquid(islandStalag.getTypeNum(), NSWorld.alCLIENT);
			s.setPlayerId(this.getPlayerId());
			s.setPos(this.getPos());
			s.pop();*/
		}
	}

	public static class NSIslandTypeConstructor implements SquidConstructor
	{
		public BaseSquid construct(World container, int sid)
		{
			return new IslandType(container, sid);
		}
	}
}
