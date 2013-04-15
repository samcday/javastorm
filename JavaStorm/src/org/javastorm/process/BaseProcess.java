package org.javastorm.process;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.ProcessForm;
import org.javastorm.types.Types;

public class BaseProcess
{
	public BaseProcess(World world)
	{
		this.world = world;
		this.formSid = null;
		this.attachToSid = null;
	}

	public void start(int processType, BaseSquid attachToSid, int formSid, int popFlags)
	{
		assert Types.isProcessType(processType);

		this.attachToSid = attachToSid;

		if (attachToSid == null)
			return;
		if (attachToSid.isDead())
			return;

		assert attachToSid != null;
		assert !attachToSid.isDead();

		int pid = this.world.attachProcess(this);

		if (formSid == 0)
		{
			// We need to create a form. This happens mostly on the server,
			// but does happen on the client for client only processes like animations
			int allocFlags = World.alDEFAULT;
			if ((popFlags & BaseSquid.pfCLIENT_POP) > 0)
				allocFlags = World.alCLIENT;

			ProcessForm s = (ProcessForm) this.world.createSquid(Types.findByTypeName("processForm").getTypeNum(), allocFlags);
			this.formSid = s;
			s.setPid(pid);
			s.setFormTypeToMatchProcessType(processType);

			// this flag is used so that we can assert that this is the ONLY
			// time that processes send their attachToSids...
			s.pop(attachToSid, popFlags);
		}
		else
		{
			// This happens only on the client
			ProcessForm s = (ProcessForm) this.world.getSquid(formSid);
			if (s == null)
			{
				this.formSid = s = (ProcessForm) this.world.take(Types.findByTypeName("processForm").getTypeNum(), formSid);
				s.setType(processType);
				s.setPid(pid);
				s.pop(attachToSid, popFlags);
			}

			assert s.getParent() == attachToSid;
			assert s.getPid() == pid;
		}

		this.notifyPostStart();
	}

	public void suicide()
	{
		this.suicide(0);
	}

	public void suicide(int popFlags)
	{
		if (this.formSid.isDead())
			return;

		this.formSid.destroy(popFlags);
	}

	public MainSquid getParent()
	{
		return (MainSquid) this.attachToSid;
	}

	public ProcessForm getForm()
	{
		return this.formSid;
	}

	public int getProcessType()
	{
		return this.formSid.getType();
	}

	public boolean isRobust(boolean shouldBeAlive)
	{
		return true;
	}

	public void process()
	{
	}

	public void interrupt(int interruptType, int info1, int info2)
	{
	}

	public void notifyPostStart()
	{
	}

	public void notifyDestroy(int popFlags)
	{
	}

	public void notifyParentDeath(BaseSquid parent)
	{
	}

	protected World world;

	private BaseSquid attachToSid;

	private ProcessForm formSid;

	public static final int CONCURRENT_PROCESS_DEFAULT_POP_FLAGS = BaseSquid.pfDONT_TRANSMIT;
}
