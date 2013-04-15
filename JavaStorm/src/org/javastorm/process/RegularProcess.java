package org.javastorm.process;

import org.javastorm.SamTimer;
import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.types.Types;

public class RegularProcess extends BaseProcess
{
	public RegularProcess(World world, int id, BaseSquid attachToSid, float period)
	{
		super(world);
		this.id = id;
		this.period = period;
		this.counter = 0;
		this.timer = new SamTimer(0);
		this.start(Types.findByTypeName("regularProcess").getTypeNum(), attachToSid, 0, BaseProcess.CONCURRENT_PROCESS_DEFAULT_POP_FLAGS | BaseSquid.pfCLIENT_POP);
	}

	public void process()
	{
		MainSquid parent = this.getParent();

		if (parent == null || parent.isDead())
			return;

		if (this.timer.isDone() && !parent.isVoid())
		{
			float newPeriod = this.getParent().regularCall(this.id, this.counter, this.period);

			if (newPeriod <= regKILL)
			{
				// Anything less that zero means that this thing has been killed
				// by other means. Equal to zero means it should be killed.
				if (newPeriod == regKILL)
					this.suicide();

				return;
			}

			this.period = newPeriod;
			this.timer.start(newPeriod);
			this.counter++;
		}
	}

	private SamTimer timer;

	private int counter;

	private int id;

	private float period;

	private static final float regKILL = 0.0f;
}
