package org.javastorm.squids;

import org.javastorm.World;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.util.NSTrace;

public class BaseSquid
{
	public BaseSquid(World container, int sid)
	{
		super();

		this._container = container;
		this.sid = sid;
		this.islandId = -1;
	}

	public int getGenus()
	{
		return this.typeStruct.getGenus();
	}

	public int getQ()
	{
		return q;
	}

	public void setQ(int q)
	{
		this.q = q;
	}

	boolean isAlive()
	{
		return !freeFlag && !deadFlag && !voidFlag;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public boolean isXmitJustCreated()
	{
		return xmitJustCreatedFlag;
	}

	public void setXmitJustCreated(boolean xmitJustCreatedFlag)
	{
		this.xmitJustCreatedFlag = xmitJustCreatedFlag;
	}

	public boolean isContained()
	{
		return containedFlag;
	}

	public void setContained(boolean containedFlag)
	{
		this.containedFlag = containedFlag;
	}

	public boolean isForm()
	{
		// TODO.
		return false;
	}

	public boolean isFree()
	{
		return freeFlag;
	}

	public void setFree(boolean freeFlag)
	{
		this.freeFlag = freeFlag;
	}

	public boolean isDead()
	{
		return deadFlag;
	}

	public void setDead(boolean deadFlag)
	{
		this.deadFlag = deadFlag;
	}

	public boolean isVoid()
	{
		return voidFlag;
	}

	public void setVoid(boolean voidFlag)
	{
		this.voidFlag = voidFlag;
	}

	public int getSid()
	{
		return sid;
	}

	public NSType getTypeStruct()
	{
		return this.typeStruct;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		// We may as well just resolve the type now.
		this.typeStruct = Types.findByTypeNum(type);
		this.type = type;
	}

	public void setIslandId(int islandId)
	{
		this.islandId = islandId;
	}

	public int getIslandId()
	{
		return this.islandId;
	}

	public void push()
	{
		this.push(0);
	}

	public void push(int popFlags)
	{
		this.setVoid(true);
		//this.removeFromParent();
	}

	public void pop()
	{
		this.pop(0);
	}

	public void pop(int popFlags)
	{
		// Attach ourselves to the NSWorld spatial. 
		//this._container.attachChild(this);

		this.setVoid(false);

		if ((popFlags & (pfDONT_TRANSMIT | pfCLIENT_POP)) == 0)
		{
			if ((popFlags & pfANY_CREATION) > 0)
				this.xmitJustCreatedFlag = true;

			this._container.sendToAll(this.transmitMyself());

			this.xmitJustCreatedFlag = false;
		}

		// TODO: when we implement server side.
		if (false)
		{
			
		}
		else
		{
			if ((popFlags & pfSERVER_POP) == 0)
			{
				// pfSERVER_POP gets set in handleUpdateSquid 
				// if we didn't come from there, then this must be a client-initiated
				// pop
				popFlags |= pfCLIENT_POP;
			}
		}

		this.notifyPostPop(popFlags);
	}

	public NSCommand transmitMyself()
	{
		return null;
	}

	public void transmitTo(int who, int message, int popFlags)
	{
		NSCommand cmd;

		if(message == TRANSMIT_MESSAGE_DESTROY)
		{
			// TODO:
			cmd = null;
			//cmd = this.transitDestroyMyself();
		}
		else
		{
			cmd = this.transmitMyself();
		}

		if(who == TRANSMIT_TO_EVERYONE)
		{
		}
		else
		{
			this._container.sendTo(who, cmd);
		}
	}

	public void popInto(BaseSquid parent, int popFlags)
	{
		this.setParent(parent);

		BaseSquid head = parent.getHead();
		this.setNext(head);
		if (head != null)
		{
			head.setPrev(this);
		}

		parent.setHead(this);
		this.setPrev(null);
		this.setContained(true);

		this.pop(popFlags | BaseSquid.pfPOPPED_INTO);
	}

	public void unlinkContained()
	{
		BaseSquid parent = this.getParent();

		if (parent == null)
			return;

		if (parent.isDead())
		{
			return;
		}

		BaseSquid prev = this.getPrev();
		BaseSquid next = this.getNext();

		if (prev != null)
		{
			prev.setNext(next);
		}
		if (next != null)
		{
			next.setPrev(prev);
		}

		if (this == parent.getHead())
		{
			parent.setHead(next);
		}

		// We're now completely uncontained.
		this.setContained(false);

		if (!this.isForm() || this.typeStruct.getTypeName().equalsIgnoreCase("contentForm"))
		{
			parent.notifyContentsChanged(this, CC_REMOVED);
		}
	}

	public void setParent(BaseSquid s)
	{
	}

	public BaseSquid getParent()
	{
		return null;
	}

	public void setNext(BaseSquid s)
	{
	}

	public void setPrev(BaseSquid s)
	{
	}

	public void setHead(BaseSquid s)
	{
		this.head = s;
	}

	public BaseSquid getNext()
	{
		return null;
	}

	public BaseSquid getPrev()
	{
		return null;
	}

	public BaseSquid getHead()
	{
		return this.head;
	}

	public void destroy(int popFlags)
	{
		// Mark that it is being destroyed.  Don't allow it to reenter
		if (this.isDead())
			return;

		this.setDead(true);

		// I moved this void check from before the notify Destroy
		if (!this.isVoid())
			this.push();

		// traverse any attached forms and kill them
		BaseSquid head = this.getHead();
		while (head != null)
		{
			head.notifyParentDeath(this);
			head.destroy(popFlags | BaseSquid.pfDONT_TRANSMIT);
			head = head.getNext();
		}

		this._container.freeSquid(this.getSid());
		this.notifyDestroy(popFlags);
	}

	// These are place holders that are implemented further up the line.

	// This is called after a pop, with the flags that were passed to the pop
	public void notifyPostPop(int popFlags)
	{

	}

	public void notifyCreate()
	{

	}

	// These are called by the static creation to allow for initialization
	// For example, mainsquid sets some type flags and zorder.
	// It is probably not overloaded much beyond that.
	public void notifyTake()
	{

	}

	// This is called by the shared destroy code.  The destroy performs
	// a renttrant check and won't call this is it renenters
	// This message is posted before the squid is actually freed, 
	// and before it is pushed onto the void.
	public void notifyDestroy(int popFlags)
	{

	}

	// This is called by the shared destroy code.  
	// This message is posted before the squid is actually freed, 
	// and AFTER it is pushed onto the void.
	public void notifyPostDestroy(int popFlags)
	{

	}

	// When something is popped into you, you will hear about it
	// once you already contain it. When its leaving you, you'll
	// hear about it once it has already left. Not called for forms
	// except the special contentform
	public void notifyContentsChanged(BaseSquid squid, int flags)
	{

	}

	public void notifyParentDeath(BaseSquid parent)
	{

	}

	public float regularCall(int id, int counter, float period)
	{
		return 1f;
	}

	public void dumpMyself()
	{
		NSType ty = Types.findByTypeNum(getType());

		// we no longer have heads or next in squids so make dummy to account for this
		String nextInfo = new String();
		String headInfo = new String();

		// ready when sam gets his squids updated!
		/*
			if( getNext() && !_container.suppressExtraDumpInfo ) 
			{ 
				nextInfo=String.format("next=~y%04d~n ", getNext() );
			}
			if( getHead() && !_container.suppressExtraDumpInfo ) 
			{
				headInfo=String.format("head=~y%04d~n ", getHead() );
			}
		*/

		boolean printflag = false;
		if (!ty.getTypeName().equals(""))
		{
			printflag = true;
		}

		this._container.Trace.Trace(NSTrace.TL_TRACE, "~1~y%04d~n Base type=%d%s%s%s %s%s%s%s%s%s\n", getSid(), getType(), printflag ? "/~y" : "", ty.getTypeName(), printflag ? "~n" : "", nextInfo, headInfo, isFree() ? "~rFREE~n " : "", isDead() ? "~rDEAD~n " : "", isVoid() ? "~rVOID~n " : "", isContained() ? "CONTAINED " : "");
	}

	int getR()
	{
		return r;
	}

	void setR(int n)
	{
		r = n;
	}

	public int getBaseFlags()
	{
		int baseFlags = 0;

		if (this.freeFlag)
			baseFlags |= 1;
		if (this.deadFlag)
			baseFlags |= 1 << 1;
		if (this.voidFlag)
			baseFlags |= 1 << 2;
		if (this.containedFlag)
			baseFlags |= 1 << 3;
		if (this.xmitJustCreatedFlag)
			baseFlags |= 1 << 4;

		baseFlags |= (this.level & 7) << 5;

		return baseFlags;
	}

	protected World _container;

	private BaseSquid head;

	private int islandId;

	private int r;

	private int sid;

	private int type;

	private NSType typeStruct;

	// Base Flags.
	private boolean voidFlag;

	private boolean freeFlag;

	private boolean deadFlag;

	private boolean containedFlag;

	// Warn Ken about q if you decide to shorten it to a char.
	// Zack moved this to base squid so that contentForms could use the
	// same macros as always as set up in the q.h
	private int q;

	// This is used to avoid having to pass popFlags into all the varieties
	// of squid transmission. Only MainSquids care about being new to
	// world on the server, so the server uses this bit as a secret code
	// allowing the client to know that this mainSquid was new to the world
	// the server should clear it after transmission and the client
	// should never set it. 
	// If we need this bit back we can just pass a parameter into all of
	// the transmission code. -jfg 10/15
	private boolean xmitJustCreatedFlag;

	private int level;

	public static final int pfDEFAULT = (0x00000000);

	public static final int pfCREATED = (0x00000001);

	public static final int pfCREATED_ABSTRACT = (0x00000002);

	public static final int pfCREATED_ASTRAL = (0x00000004);

	public static final int pfANY_CREATION = (pfCREATED | pfCREATED_ABSTRACT | pfCREATED_ASTRAL);

	public static final int pfSERVER_POP = (0x00000008);

	// Set for server pops. Client sets it in handleUpdateSquid. It is
	// also set when the client has been authorized to destroy a squid
	// by the server.

	public static final int pfCLIENT_POP = (0x00000010);

	// Set in BaseSquid::pop if pfSERVER_POP isn't set.

	public static final int pfDIE_SALVAGE = (0x00200000);

	public static final int pfWHO_KILLED_MASK = (0x000F0000);

	public static final int pfSERVER_DESTROY_SHIFT = (16);

	public static final int pfDONT_RECALC_GRAPH_CONNECTIONS = (0x02000000);

	public static final int pfDONT_TRANSMIT = (0x00000040);

	public static final int pfPREDICTABLE_POP = (0x00000020);

	public static final int pfPOPPED_INTO = (0x00000100);

	public static final int pfFROM_FORT_DATA = (pfCREATED | pfSERVER_POP);

	public static final int CC_ADDED = (1);

	public static final int CC_REMOVED = (2);

	public static final int CC_FLAG_CHANGED = (4);
	
	
	public static final int TRANSMIT_TO_EVERYONE = (-1);
	public static final int TRANSMIT_MESSAGE_POP = (-1);
	public static final int TRANSMIT_MESSAGE_DESTROY = (-2);
	public static final int TRANSMIT_MESSAGE_UPDATE  = (-3);
}
