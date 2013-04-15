package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidConstructor;

public class IsleType extends MainSquid
{
	public IsleType(World world, int sid)
	{
		super(world, sid);
	}

	public void notifyPostPop(int popFlags)
	{
		if ((popFlags & BaseSquid.pfCREATED) > 0)
		{
			/*Box s = new Box("Isle" + this.hashCode(), new Vector3f(0, 0, 0), 5f, 5f, 5f);
			s.setLocalTranslation(0, 0, 0);
			s.setModelBound(new BoundingBox());
			s.updateModelBound();
			this.attachChild(s);

			Vector3f newLocal = new Vector3f(this.localTranslation);
			newLocal.y -= 10;
			this.setLocalTranslation(newLocal);*/
		}
	}

	public static class NSIsleTypeConstructor implements SquidConstructor
	{
		public BaseSquid construct(World container, int sid)
		{
			return new IsleType(container, sid);
		}
	}
}
