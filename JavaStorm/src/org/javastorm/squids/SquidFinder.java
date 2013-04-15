package org.javastorm.squids;

import org.javastorm.BoardCoord;
import org.javastorm.World;

public class SquidFinder
{
	public SquidFinder(World world)
	{
		//this.world = world;
		//this.ignoreZeroLevel = this.considerHeight = false;
		this.invalidate();
	}

	public SquidFinder(World world, BoardCoord center, int xRadius, int yRadius)
	{
		/*center = new NSBoardCoord(center); 
		this.world = world;
		center.roundUp();
		NSBoardCoordRect rect = new NSBoardCoordRect(center, center);
		rect.extend(-xRadius, -yRadius, xRadius, yRadius);
		this.list = this.world.getSquidHash().get(rect);
		
		if(this.list == null) this.listLength = -1;
		else this.listLength = this.list.length;
		
		this.listIndex = 0;*/
		this.listIndex = 10000;
	}

	private void invalidate()
	{
		this.sid = null;
	}

	public void findNext()
	{
		this.listIndex++;
	}

	public boolean isValid()
	{
		return (this.listIndex < this.listLength);
	}

	public MainSquid s()
	{
		return this.list[this.listIndex];
	}

	public static final int findSquidGenus(World world, BoardCoord bc, int genus)
	{
		/*int flags;
		
		if((genus & NSType.gSTANDABLE) > 0)
			flags = 0;
		else
			flags = sfIGNORE_ZERO_LEVEL;*/

		SquidFinder finder = new SquidFinder(world);

		while (finder.isValid())
		{
			if ((finder.s().getGenus() & genus) > 0)
			{
				return finder.s().getSid();
			}

			finder.findNext();
		}

		return 0;
	}

	protected MainSquid sid;

	//private boolean ignoreZeroLevel, considerHeight;
	private int listLength;

	private MainSquid list[];

	private int listIndex;

	//private NSWorld world;

	public static final int sfIGNORE_ZERO_LEVEL = 1;

	public static final int sfCONSIDER_HEIGHT = 0x4;
}
