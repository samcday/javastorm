package org.javastorm.squids;

import org.javastorm.BoardCoord;
import org.javastorm.NSUtil;
import org.javastorm.Netstorm;

// Maintains a hash list of all Squids with XY locations.
public class SquidXYHash
{
	public SquidXYHash()
	{
		// We'll make the Hashmap as large as every single BoardCoord location possible.
		this.hash = new MainSquid[4][];

		this.setLevel(HASH_LEVEL_DEFAULT);
		this.clear();
	}

	public void clear()
	{
		int oldLevel = this.currentLevel;
		for (int i = 0; i < HASH_LEVEL_COUNT; i++)
		{
			this.setLevel(i);

			if (this.hash[i] == null)
			{
				this.hash[i] = new MainSquid[this.getDim() * this.getDim()];
			}

			for (int j = 0; j < this.hash[i].length; j++)
				this.hash[i][j] = null;
		}

		this.setLevel(oldLevel);
	}

	public void setLevel(int level)
	{
		assert (level >= 0 && level < HASH_LEVEL_COUNT);
		this.currentLevel = level;
		this.currentDimInTiles = this.levelDimInTiles[level];
		this.currentHashDim = Netstorm.BOARDDIM_IN_TILES / this.currentDimInTiles;
	}

	public int getDim()
	{
		return this.currentHashDim;
	}

	public int getDim(int level)
	{
		return Netstorm.BOARDDIM_IN_TILES / this.levelDimInTiles[level];
	}

	public BoardCoord getHashCoord(BoardCoord bc)
	{
		return new BoardCoord(Math.min(this.currentHashDim - 1, NSUtil.roundToInt(bc.getX()) / this.currentDimInTiles), Math.min(this.currentHashDim - 1, NSUtil.roundToInt(bc.getY()) / this.currentDimInTiles));
	}

	public void clipHashCoord(BoardCoord hc)
	{
		int hashX = (int) hc.getX();
		int hashY = (int) hc.getY();
		if (hashX < 0)
			hc.setX(0);
		if (hashY < 0)
			hc.setY(0);
		if (hashX > this.currentHashDim - 1)
			hc.setX(this.currentHashDim - 1);
		if (hashY > this.currentHashDim - 1)
			hc.setY(this.currentHashDim - 1);
	}

	// Returns a list of Squids within a given BoardCoordRect.
	// The list returned will be in a order friendly to the 2d renderer.
	public MainSquid[] get(BoardCoord tl, BoardCoord br)
	{
		// Search out an extra co-ordinate in each direction.
		tl = this.getHashCoord(tl);
		br = this.getHashCoord(br);
		tl.moveBy(-2, -2);
		this.clipHashCoord(tl);
		br.moveBy(2, 2);
		this.clipHashCoord(br);
		BoardCoord scan = new BoardCoord(tl);

		MainSquid[] list = new MainSquid[(int) (br.getX() - tl.getX()) * (int) (br.getY() - tl.getY())];
		int listIndex = 0;
		int listCount = 0;

		for (; scan.getY() < br.getY(); scan.moveBy(0, 1))
		{
			scan.setX(tl.getX());

			for (; scan.getX() < br.getX(); scan.moveBy(1, 0))
			{
				MainSquid s = list[listIndex++] = this.get((int) scan.getX(), (int) scan.getY());
				while (s != null)
				{
					listCount++;
					s = s.getNext();
				}
			}
		}

		MainSquid totalList[] = new MainSquid[listCount];
		int totalListIndex = 0;
		for (int i = 0; i < listIndex; i++)
		{
			MainSquid s = list[i];
			while (s != null)
			{
				totalList[totalListIndex++] = s;
				s = s.getNext();
			}
		}

		return totalList;
	}

	/*
		public NSMainSquid[] get(NSBoardCoordRect rect)
		{		
			NSBoardCoordRect extendedRect = new NSBoardCoordRect(rect);
			extendedRect.extend(0, 0, 10, 10);
			extendedRect.round();
			NSBoardCoord tl = new NSBoardCoord(extendedRect.getTl()), br = new NSBoardCoord(extendedRect.getBr());

			NSBoardCoord scan = new NSBoardCoord(tl);

			// Ok so first up, we get all the BoardCoord lists in a big array.
			int listCount = extendedRect.area();
			//rect.extend(1, 1, -9, -9);
			
			if(listCount == 0) return null;
			NSMainSquid[] lists = new NSMainSquid[listCount];
			NSMainSquid lookAhead;
			NSMainSquid sortedList[];

			int current = 0;
			int count = 0;
			int currentZOrder = -101;

			for(; scan.getY() < br.getY(); scan.moveBy(0, 1))
			{
				scan.setX(tl.getX());
				for(; scan.getX() < br.getX(); scan.moveBy(1, 0))
				{
					lookAhead = this.get(scan);

					//try
					//{
						lists[current++] = lookAhead;
					//} catch(ArrayIndexOutOfBoundsException aioobe) { aioobe.printStackTrace(); }

					// Quickly scan ahead and see how many elements are in this list.
					while(lookAhead != null)
					{
						if(lookAhead.getZOrder() > currentZOrder) currentZOrder = lookAhead.getZOrder();
						// Note we perform this check twice, this first time is just to determine if we should increment count or not.
						if(lookAhead.getBoundingBox().intersects(rect))
							count++;

						lookAhead = lookAhead.getNext();
					}
				}
			}

			// Ok, now we shuffle all the squids into one big list, we not only sort by zOrder, but make sure they're in order of left-right
			// and top - bottom.
			sortedList = new NSMainSquid[count];
			int sortedListIndex = 0;
			boolean allDone = false;
			while(!allDone)
			{
				allDone = true;
				
				for(int i = 0; i < listCount; i++)
				{
					if(lists[i] != null)
					{
						allDone = false;
						
						while((lists[i] != null) && (lists[i].getZOrder() == currentZOrder))
						{
							if(lists[i].getBoundingBox().intersects(rect))
								sortedList[sortedListIndex++] = lists[i];
							lists[i] = lists[i].getNext();
						}
					}
				}

				currentZOrder--;
			}

			return sortedList;
		}*/

	// Retrieves the first Squid located at given BoardCoord on the current hash level.
	public MainSquid get(BoardCoord bc)
	{
		int x = (int) bc.getX() / this.currentDimInTiles;
		int y = (int) bc.getY() / this.currentDimInTiles;

		return this.hash[this.currentLevel][y * this.currentHashDim + x];
	}

	public MainSquid get(int x, int y)
	{
		return this.hash[this.currentLevel][y * this.currentHashDim + x];
	}

	// Adds a Squid to this hash.
	public void put(MainSquid s)
	{
		int hashLevel = s.getHashLevel();
		BoardCoord bc = s.getPos();
		int x = (int) (bc.getX()) / this.levelDimInTiles[hashLevel];
		int y = (int) (bc.getY()) / this.levelDimInTiles[hashLevel];
		int hashPos = y * this.getDim(hashLevel) + x;

		// First we'll check if there's already a squid at this location.
		MainSquid inHash = this.hash[hashLevel][hashPos];

		// If this list is empty, just place it at the head.
		if (inHash == null)
		{
			this.hash[hashLevel][hashPos] = s;
			return;
		}

		// If there's already a Squid at this location, we insert this new Squid sorted by zOrder.
		while (inHash != null)
		{
			// If this item has a higher zOrder than what we're about to insert, we'll insert just before it.
			if (inHash.getZOrder() < s.getZOrder())
			{
				s.setNext(inHash);
				s.setPrev(inHash.getPrev());

				// If we're inserting at head of list then we have to add ourselves to hash.
				if (inHash.getPrev() == null)
				{
					this.hash[hashLevel][hashPos] = s;
				}

				inHash.setPrev(s);

				return;
			}

			// If we're about to hit end of list, then just tack it on end.
			if (inHash.getNext() == null)
			{
				s.setPrev(inHash);
				inHash.setNext(s);
				return;
			}

			inHash = inHash.getNext();
		}
	}

	public MainSquid getGridSid(BoardCoord bc)
	{
		this.clipHashCoord(bc);
		int hx = NSUtil.ceil(bc.getX());
		int hy = NSUtil.ceil(bc.getY());
		return this.hash[0][hy * Netstorm.BOARDDIM_IN_TILES + hx];
	}

	// Removes a squid from the hash. 
	public void remove(MainSquid s)
	{
		// This is straightforward enough, tell the next squid in line that their previous is our previous. =)
		if (s.getNext() != null)
		{
			s.getNext().setPrev(s.getPrev());
		}

		// If this squid is not at the head of the list, it makes our job easier.
		if (s.getPrev() != null)
		{
			s.getPrev().setNext(s.getNext());
		}
		// If we were at head of list, we just add the next in line to the head.
		else
		{
			MainSquid next = s.getNext();
			if (next != null)
			{
				int x = NSUtil.roundToInt(next.getPos().getX());
				int y = NSUtil.roundToInt(next.getPos().getY());
				int hashPos = y * this.currentHashDim + x;
				this.hash[this.currentLevel][hashPos] = next;
			}
		}
	}

	private int currentLevel; // the current level at which the hash is operating

	private int currentDimInTiles; // the dimensions a single cell of the current level, in tiles

	private int currentHashDim; // width and height of hash table for the current level

	private MainSquid hash[][];

	private int levelDimInTiles[] =
	{ 1, 2, 4, 16 };

	public static final int HASH_LEVEL_COUNT = 4;

	public static final int HASH_LEVEL_DEFAULT = 2;
}
