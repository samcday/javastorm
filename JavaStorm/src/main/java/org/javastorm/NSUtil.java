package org.javastorm;

import java.util.StringTokenizer;

import org.javastorm.renderer.Renderer;
import org.javastorm.squids.BaseSquid;

public class NSUtil
{
	public static final int[] ipStringToArray(String ipString)
	{
		StringTokenizer iptokens = new StringTokenizer(ipString, ".");
		int[] ip = new int[4];
		int ipindex = 0;

		while (iptokens.hasMoreTokens())
		{
			ip[ipindex++] = Integer.valueOf(iptokens.nextToken()).intValue();
		}

		return ip;
	}

	public static final BoardCoord screenToBoard(ScreenCoord sc)
	{
		return new BoardCoord((float) sc.getX() / Renderer.X_TILE_DIM, (float) sc.getY() / Renderer.Y_TILE_DIM);
	}

	public static final ScreenCoord boardToScreen(BoardCoord bc)
	{
		return new ScreenCoord((int) (bc.getX() * (float) Renderer.X_TILE_DIM), (int) (bc.getY() * (float) Renderer.Y_TILE_DIM));
	}

	// RANDOM NUMBER STUFF.
	private static int lastRandomNumber = 0;

	public static final int fastRandomInt(int minInt, int maxInt)
	{
		int rand = getRand();
		return minInt + (rand % (maxInt - minInt));
	}

	public static final int fastRandomInt(int maxInt)
	{
		return getRand() % maxInt;
	}

	public static final int getRand()
	{
		if (lastRandomNumber == 0)
			lastRandomNumber = 0x0bad0bad;

		lastRandomNumber = (lastRandomNumber * 65539 + 3);
		return (lastRandomNumber >> 16) & 0xFF;
	}

	public static final int roundToInt(float num)
	{
		return (int) (num + 0.5f);
	}

	public static final int ceil(float num)
	{
		return (int) (num + 0.999999999999999f);
	}

	public static final int floor(float num)
	{
		return (int) (num - 0.999999999999999f);
	}

	//	 returns the corner orientation that would sanely arise from your side
	//	 orientation

	//	 e.g. If you are an 'F' you connect south and east. This function will
	//	      return corner orienation 'E' which connects to the southeast
	public static final int getSaneCornerOrientation(int sideOrientation)
	{
		sideOrientation -= 'A';
		int sideMask = BoardCoord.orientationToMask[sideOrientation];
		int cornerMask = 0;

		for (int dir = BoardCoord.NORTH; dir <= BoardCoord.WEST; dir += 2)
		{
			int nextDir = (dir + 2) % 8;
			if (((sideMask & BoardCoord.dirMasks[dir]) > 0) && ((sideMask & BoardCoord.dirMasks[nextDir])) > 0)
			{
				cornerMask |= BoardCoord.dirMasks[nextDir];
			}
		}

		return BoardCoord.maskToOrientation[cornerMask];
	}

	//	 The converse of the above function.
	//	 Returns a side orientation such that
	//	 For each corner that is connected, connect the two adjacent sides
	//	 e.g. if you are an 'E' corner orientation, you connect to the southeast.
	//	      This function will return a side orientation of 'F' which connects
	//	      to the south and east.
	public static final int getSaneSideOrientation(int cornerOrientation)
	{
		cornerOrientation -= 'A';
		int cornerMask = BoardCoord.orientationToMask[cornerOrientation];
		int sideMask = 0;

		for (int dir = BoardCoord.NORTH_EAST; dir <= BoardCoord.NORTH_WEST; dir += 2)
		{
			if ((cornerMask & BoardCoord.dirMasks[dir]) > 0)
			{
				sideMask |= BoardCoord.dirMasks[NSUtil.getPriorDir(dir)];
				sideMask |= BoardCoord.dirMasks[NSUtil.getNextDir(dir)];
			}
		}

		return BoardCoord.maskToOrientation[sideMask];
	}

	//	 Tries to make a sane orientation out of insane inputs. The algorith:

	//	   -- take the old corner orientation and chop off any corners that aren't connected
	//	      to adjacent sides
	//	   -- turn on the sides that are next to the newly trimmed corners

	//	 that turns this
	//	  **.
	//	  .0*
	//	  .**

	//	 into this
	//	 ...
	//	 .0*
	//	 .**
	public static final int[] trimOrientation(int oldSide, int oldCorner)
	{
		int newCorner, newSide;

		int cornerMask = BoardCoord.orientationToMask[oldCorner - 'A'];
		cornerMask &= BoardCoord.orientationToMask[NSUtil.getSaneCornerOrientation(oldSide) - 'A'];
		newCorner = BoardCoord.maskToOrientation[cornerMask];
		newSide = NSUtil.getSaneSideOrientation(newCorner);

		return new int[]
		{ newSide, newCorner };
	}

	public static final int getNextDir(int curDir)
	{
		return (((curDir) + 1) & 7);
	}

	public static final int getPriorDir(int curDir)
	{
		return (((curDir) + 7) & 7);
	}

	public static String getStackTrace(Throwable t)
	{
		StringBuilder stackTrace = new StringBuilder();
		StackTraceElement[] elements = t.getStackTrace();

		stackTrace.append(t.toString()).append("\n");
		for (int i = 0; i < elements.length; i++)
			stackTrace.append("   at ").append(elements[i].toString()).append("\n");

		return stackTrace.toString();
	}

	public static final void dumpMyself(BaseSquid s)
	{
		s.dumpMyself();
		BaseSquid head = s.getHead();
		boolean anyPrinted = head != null;
		while (head != null)
		{
			head.dumpMyself();
			head = head.getNext();
		}
		if (anyPrinted)
			System.out.println("END " + s.getSid());
	}
}
