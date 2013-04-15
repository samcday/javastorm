package org.javastorm.squids;

import org.javastorm.BoardCoord;
import org.javastorm.BoardCoordRect;
import org.javastorm.World;
import org.javastorm.types.Types.NSType;

public class ConnectionFinder extends SquidFinder
{
	public ConnectionFinder(World world, MainSquid s, int flags)
	{
		super(world);

		this.foundSids = new MainSquid[MAX_CONNECTIONS];
		this.world = world;
		this.flags = flags;

		if ((flags & CF_OPERATE_ON_SID_GRID) > 0)
		{
			numFound = 0;
		}

		this.interior = s.getRect();

		this.centerTypeStruct = s.getTypeStruct();
		this.centerFrame = s.getFrame();
		this.center = s.getCenter();

		this.start();
	}

	public void start()
	{
		BoardCoord ctl = new BoardCoord(this.interior.getTl().getX() - 1.0f, this.interior.getTl().getY() - 1.0f);
		BoardCoord cbr = new BoardCoord(this.interior.getBr().getX() + 1.0f, this.interior.getBr().getY() + 1.0f);

		if ((NSType.gWALK_BLOCKING & this.world.spotFlags().andSpotFlagsRect(this.interior.getTl().getX(), this.interior.getTl().getY(), this.interior.getBr().getX(), this.interior.getBr().getY())) > 0)
		{
			this.sid = null;
			return;
		}
		if ((this.flags & CF_OPERATE_ON_SID_GRID) > 0)
		{
			this.l = (int) ctl.getX();
			this.t = (int) ctl.getY();
			this.r = (int) cbr.getX();
			this.b = (int) cbr.getY();
			this.i = this.l;
			this.j = this.t;
			findInSidGrid();
		}
		else
		{
			//findFirst((int)ctl.getX(), (int)ctl.getY(), (int)cbr.getX(), (int)cbr.getY()); 
		}
	}

	public void findInSidGrid()
	{
		this.sid = null;

		while (true)
		{
			this.i++;
			boolean inCenterRows = true;
			if (this.j == this.t || this.j == this.b)
				inCenterRows = false;

			if (inCenterRows)
			{
				if (this.i > this.r)
				{
					this.j++;
					if (this.j == this.b)
					{
						this.i = this.l + 1;
					}
					else
					{
						this.i = this.l;
					}
				}
			}
			else
			{
				if (i >= r)
				{
					j++;
					if (j > b)
					{
						return;
					}
					i = l;
				}
			}

			this.sid = this.world.getSquidHash().getGridSid(new BoardCoord(i, j)); // pull sids out of the sid grid
			if (this.sid != null)
			{
				MainSquid s = this.sid;

				if (this.interior.intersects(s.getPos()) || !this.test(s))
				{
					this.sid = null;
				}
				else
				{
					for (int k = 0; k < numFound; k++) // and if they have not been found yet
					{
						if (foundSids[k] == sid)
						{
							this.sid = null;
							break;
						}
					}

					if (this.sid != null) // mark them as found
					{
						this.foundSids[this.numFound++] = this.sid;
						return; // and let the man play with them
					}
				}
			}
		}
	}

	private boolean test(MainSquid s)
	{
		BoardCoord pos = s.getPos();
		int spotFlags = this.world.spotFlags().getSpotFlags(pos);

		if ((s.isSurface() || ((flags & CF_CONSIDER_NON_SURFACES) > 0)) && (((flags & CF_OPERATE_ON_SID_GRID) > 0) || interior.connectsOrthagonally(s.getRect())) && ((((flags & CF_IGNORE_ORIENTATION) > 0) || canConnect(s.getTypeStruct(), s.getFrame(), centerTypeStruct, centerFrame, s.getCenter().getDirTo(center)))) && !s.isDead() && (!((flags & CF_IGNORE_ABSTRACT) > 0) || !s.isAbstract()) && !((spotFlags & NSType.gWALK_BLOCKING) > 0))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static final boolean canConnect(NSType aType, int frameA, NSType bType, int frameB, int dirAtoB)
	{

		int orientA, orientB;

		if (BoardCoord.dirIsCardinal(dirAtoB))
		{
			orientA = aType.getOrientation(frameA);
			orientB = bType.getOrientation(frameB);
		}
		else
		{
			orientA = aType.getCornerOrientation(frameA);
			orientB = bType.getCornerOrientation(frameB);
		}

		if ((aType.getGenus() & NSType.gEMPLACEMENT) > 0)
		{
			orientA = 'P';
		}
		if ((bType.getGenus() & NSType.gEMPLACEMENT) > 0)
		{
			orientB = 'P';
		}

		if (((aType.getGenus() & (NSType.gBRIDGE | NSType.gBOMB)) > 0) && ((bType.getGenus() & (NSType.gEMPLACEMENT | NSType.gISLAND | NSType.gISLAND3x3 | NSType.gBUILDING)) > 0))
		{
			orientB = 'A';
		}
		else if (((bType.getGenus() & (NSType.gBRIDGE | NSType.gBOMB)) > 0) && ((aType.getGenus() & (NSType.gEMPLACEMENT | NSType.gISLAND | NSType.gISLAND3x3 | NSType.gBUILDING)) > 0))
		{
			orientA = 'A';
		}

		int maskA = BoardCoord.dirMasks[dirAtoB];
		int maskB = BoardCoord.dirMasks[BoardCoord.oppositeDir[dirAtoB]];

		orientA -= 'A';
		orientB -= 'A';
		if (((orientConnections[orientA] & maskA) > 0) && ((orientConnections[orientB] & maskB) > 0))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static final int orientConnections[] =
	{ BoardCoord.NORTH_MASK | BoardCoord.EAST_MASK | BoardCoord.SOUTH_MASK | BoardCoord.WEST_MASK, BoardCoord.NORTH_MASK | BoardCoord.EAST_MASK | BoardCoord.SOUTH_MASK, BoardCoord.EAST_MASK | BoardCoord.SOUTH_MASK | BoardCoord.WEST_MASK, BoardCoord.NORTH_MASK | BoardCoord.SOUTH_MASK | BoardCoord.WEST_MASK, BoardCoord.NORTH_MASK | BoardCoord.EAST_MASK | BoardCoord.WEST_MASK, BoardCoord.EAST_MASK | BoardCoord.SOUTH_MASK, BoardCoord.SOUTH_MASK | BoardCoord.WEST_MASK, BoardCoord.WEST_MASK | BoardCoord.NORTH_MASK, BoardCoord.NORTH_MASK | BoardCoord.EAST_MASK, BoardCoord.NORTH_MASK | BoardCoord.SOUTH_MASK, BoardCoord.EAST_MASK | BoardCoord.WEST_MASK, BoardCoord.SOUTH_MASK, BoardCoord.WEST_MASK, BoardCoord.NORTH_MASK, BoardCoord.EAST_MASK, 0 };

	private MainSquid foundSids[];

	private World world;

	private BoardCoord center;

	private NSType centerTypeStruct;

	private int flags;

	private int centerFrame;

	private int numFound, i, j, r, t, l, b;

	private BoardCoordRect interior;

	public static final int MAX_CONNECTIONS = 64;

	public static final int CF_IGNORE_ORIENTATION = (0x0001);

	public static final int CF_CONSIDER_NON_SURFACES = (0x0002);

	public static final int CF_IGNORE_ABSTRACT = (0x0004);

	public static final int CF_OPERATE_ON_SID_GRID = (0x0008);
}
