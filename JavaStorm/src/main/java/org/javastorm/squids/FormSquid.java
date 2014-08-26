package org.javastorm.squids;

import org.javastorm.World;

public class FormSquid extends BaseSquid
{
	public FormSquid(World container, int sid)
	{
		super(container, sid);
	}

	public void pop(BaseSquid intoSquid, int popFlags)
	{
		this.popInto(intoSquid, popFlags);
	}

	public void push(int popFlags)
	{
		super.push(popFlags);

		if (this.getParent() != null)
		{
			this.unlinkContained();
		}
	}

	public void setParent(BaseSquid parent)
	{
		this.parentSquid = parent;
	}

	public BaseSquid getParent()
	{
		return this.parentSquid;
	}

	public void setNext(BaseSquid s)
	{
		this.next = s;
	}

	public void setPrev(BaseSquid s)
	{
		this.prev = s;
	}

	public BaseSquid getNext()
	{
		return this.next;
	}

	public BaseSquid getPrev()
	{
		return this.prev;
	}

	private BaseSquid prev, next;

	private BaseSquid parentSquid;
}
