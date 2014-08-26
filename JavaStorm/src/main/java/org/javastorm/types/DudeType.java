// NSDudeType Port
// By Fleet Admiral
package org.javastorm.types;

import org.javastorm.World;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.util.NSTrace;

public class DudeType extends MainSquid
{
	//Task Defines
	// these are used in carrier type
	public static final int taskMASK = 0xFFF0;

	public static final int taskHEADER = 0x5100;

	// should be dude only here
	public static final int taskUNKNOWN = 0x5101;

	public static final int taskTEND_MANA_PRODUCERS = 0x5102;

	public static final int taskGO_HOME = 0x5103;

	public static final int taskGO_TO = 0x5104;

	public static final int taskFALL = 0x5105;

	public static final int taskBLEED = 0x5106;

	public static final int taskRUN_BURNING = 0x5107;

	public static final int taskTOTAL = 0x5108;

	public DudeType(World world, int sid)
	{
		super(world, sid);
	}

	public int shouldSave()
	{
		return 0;
	}

	public void notifyPostPop(int popFlags)
	{
		if ((popFlags & BaseSquid.pfCREATED) > 0)
		{
			setQ(taskUNKNOWN);
			_container.dudeList.add(getSid());
			hide();
		}
		super.notifyPostPop(popFlags);
	}

	public void notifyPostDestroy(int popFlags)
	{
		if (!isAbstract() && !isAstral())
		{
			_container.dudeList.killAll(getSid());
		}
		super.notifyPostDestroy(popFlags);
	}

	public void dumpMyself()
	{
		super.dumpMyself();
		super._container.Trace.Trace(NSTrace.TL_TRACE, "~1~y     Task \"%s\"\n", taskNameList[Math.max(taskUNKNOWN, Math.min(getTask(), taskTOTAL - 1)) - taskUNKNOWN]);
	}

	int startFalling()
	{
		// NOT DONE
		setTask(taskFALL);
		//findProcess( getSid(), getPathProcessType( getType() ), 0, 1 );	// Kills the existing path process.
		return 1;
	}

	public void startBurning()
	{
		// NOT DONE
		/*
		if( !isDead() && getTask() != taskRUN_BURNING && getTask() != taskFALL ) {
			setTask( taskRUN_BURNING );
			extern int burningSystemProcessType;
			int burningFormSid = findProcess( getSid(), burningSystemProcessType );
			if( !burningFormSid ) {
				makeQuietBurning( getSid() );
			}
			walkTo( getNearestOwned( getPos(), homeList, getPlayerId() ), 1 );
			// Ken removed this because it was causing too many paths to be
			// calculated, as dudes are effected by multiple blowing-up things, and
			// the "am I already headed there?" test seems not to find an existing
			// walk process on them. So screw it. You just burn and keep doing what you
			// were doing.
		}
		*/
	}

	public int getTask()
	{
		return getQ();
	}

	public int shouldFall()
	{
		if (isDead())
		{
			return 0;
		}
		if (getTask() == taskFALL)
		{
			return 1;
		}
		//		NOT DONE
		/*
				NSBoardCoord bc = getPos();
				int spotFlags = getSpotFlags( bc.getX(), bc.getY() );

				// There is nothing below me, so fall...
				if( (spotFlags & (NSType.gSTANDABLE))==0 ) {
					if( startFalling()==1 ) {
						return 1;
					}
				}

				// Something landed on me, so bleed...
				if( (spotFlags & NSType.gWALK_BLOCKING)==1 ) {
					setTask( taskBLEED );
					//NOT DONE
					//findProcess( getSid(), getPathProcessType( getType() ), 0, 1 );	// Kills the existing path process.
					return 1;
				}
				*/

		return 0;
	}

	public int haltPath()
	{
		if (isDead() || shouldFall() == 1)
		{
			return 1;
			//			NOT DONE
			//return haltALREADY_DEAD;
		}
		//		NOT DONE
		return 0;
		//return haltNO;
	}

	public void setTask(int task)
	{
		setTask(task, 0);
	}

	public void setTask(int task, float startDelay)
	{

		if (getTask() == taskFALL)
		{
			if (task != taskFALL)
			{
				task = taskFALL;
			}
		}

		//		NOT DONE
		//RegularProcess *reg = findRegularProcessAnding( taskMASK, taskHEADER );
		//if( reg ) {
		//	reg->id = task;
		//	reg->counter = 0;
		//}
		//else {
		//	reg = new RegularProcess( task, getSid(), 5.0f );
		//}
		//reg->timer.start(startDelay);
		setQ(task);
	}

	public void hide()
	{
		//		NOT DONE
		//	updateFrame( getTypeStruct().getFirstFrame( 'I'-'A' ) );
	}

	int hiding()
	{
		if (getCluster() == 'I' - 'A')
		{
			return 1;
		}
		return 0;
	}

	void show()
	{
		if (hiding() == 1)
		{
			//			NOT DONE
			//updateFrame( getTypeStruct().getFirstFrame( 'A'-'A' ) );
		}
	}

	public static final String taskNameList[] =
	{ "TEND_MANA_PRODUCERS", "GO_HOME", "GO_TO", "FALL", "BLEED", "RunBurning" };

}
