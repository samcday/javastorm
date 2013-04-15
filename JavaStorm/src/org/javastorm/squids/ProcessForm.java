package org.javastorm.squids;

import org.javastorm.World;
import org.javastorm.process.BaseProcess;

public class ProcessForm extends FormSquid
{
	public ProcessForm(World container, int sid)
	{
		super(container, sid);
	}

	public void setFormTypeToMatchProcessType(int pType)
	{
		this.setType(pType);
	}

	public void popInto(BaseSquid parent, int popFlags)
	{
		super.popInto(parent, popFlags);
	}

	public void notifyDestroy(int popFlags)
	{
		this._container.getProcess(this.getPid()).notifyDestroy(popFlags);
		this._container.removeProcess(this.getPid());
		super.notifyDestroy(popFlags);
	}

	public void notifyParentDeath(BaseSquid parent)
	{
		BaseProcess p = this._container.getProcess(this.getPid());
		if (p != null)
			p.notifyParentDeath(parent);

		super.notifyParentDeath(parent);
	}

	public void setPid(int pid)
	{
		this.pid = pid;
	}

	public int getPid()
	{
		return this.pid;
	}

	private int pid;

	public static class NSProcessFormConstructor implements SquidConstructor
	{
		public BaseSquid construct(World container, int sid)
		{
			return new ProcessForm(container, sid);
		}
	}
}
