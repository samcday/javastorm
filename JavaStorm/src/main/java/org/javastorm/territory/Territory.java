package org.javastorm.territory;

import org.javastorm.BoardCoord;
import org.javastorm.CanonDecoder;
import org.javastorm.Rect;
import org.javastorm.types.Types;
import org.javastorm.util.MyByteBuffer;

// Well that's an interesting mystery solved.
// The : operator in a struct is saying how many bits that field takes up
// talk about hacky hacky.
public class Territory
{
	public Territory()
	{
		this(0, 0, false, false, false, false, 0, 0, 0, 0, 0, 0);
	}

	public Territory(int canon, int rot, boolean exists, boolean notPlacedInPuzzleYet, boolean takenToBattle, boolean decorated, int reserved, int xChunkCoord, int yChunkCoord, int randSeed, int reserved2, int reserved3)
	{
		this.canon = canon;
		this.rot = rot;
		this.exists = exists;
		this.notPlacedInPuzzleYet = notPlacedInPuzzleYet;
		this.takenToBattle = takenToBattle;
		this.decorated = decorated;
		this.reserved = reserved;
		this.xChunkCoord = xChunkCoord;
		this.yChunkCoord = yChunkCoord;
		this.randSeed = randSeed;
		this.reserved2 = reserved2;
		this.reserved3 = reserved3;
	}

	public Territory(Territory otherTerr)
	{
		this.canon = otherTerr.canon;
		this.rot = otherTerr.rot;
		this.exists = otherTerr.exists;
		this.notPlacedInPuzzleYet = otherTerr.notPlacedInPuzzleYet;
		this.takenToBattle = otherTerr.takenToBattle;
		this.decorated = otherTerr.decorated;
		this.reserved = otherTerr.reserved;
		this.xChunkCoord = otherTerr.xChunkCoord;
		this.yChunkCoord = otherTerr.yChunkCoord;
		this.randSeed = otherTerr.randSeed;
		this.reserved2 = otherTerr.reserved2;
		this.reserved3 = otherTerr.reserved3;
	}

	public ChunkCoord getDim()
	{
		CanonDecoder c = new CanonDecoder(Types.findByTypeName("puzzlePiece"), this.canon, this.rot * 2, new BoardCoord(0, 0));

		Rect r = c.getFullRect();

		return new ChunkCoord(r.getWidth(), r.getHeight());
	}

	private static final boolean verbose = false;

	// Man bitfields look disgusting in Java.
	// Updated this 4/4/09 to mask each value to fit inside the bitsize it's supposed to be.
	// Otherwise for example, if canon is set to like 300, without masking it it will put shit all over the bitfield.
	// Must remember to be careful of this in future.
	public void write(MyByteBuffer buffer)
	{
		this.write(buffer, verbose);
	}

	public void write(MyByteBuffer buffer, boolean verbose)
	{
		int temp;

		if (verbose)
			System.err.print("NSTerritory: write. ");
		temp = 0;
		{
			// Canon is 6 bits.
			temp = this.canon & 0x3F;

			// Rot is 3 bits.
			temp |= (this.rot & 0x03) << 6;
		}
		if (verbose)
			System.err.print(temp + " ");
		buffer.put(temp);

		temp = 0;
		{
			// Exists is 1bit.
			temp = (this.exists ? 1 : 0) & 0x01;

			// notplacedinpuzzleyet is 1 bit.
			temp |= ((this.notPlacedInPuzzleYet ? 1 : 0) & 0x01) << 1;

			// Takentobattle is 1bit.
			temp |= ((this.takenToBattle ? 1 : 0) & 0x01) << 2;

			// Decorated is 1bit
			temp |= ((this.decorated ? 1 : 0) & 0x01) << 3;

			// Reserved is 4 bits.
			temp |= (this.reserved & 0x0F) << 4;
		}
		if (verbose)
			System.err.print(temp + " ");
		buffer.put(temp);

		temp = 0;
		{
			// xchunkcoord is 4 bits.
			temp = this.xChunkCoord & 0x0F;

			// ychunkcoord is 4bits.
			temp |= (this.yChunkCoord & 0x0F) << 4;
		}
		if (verbose)
			System.err.print(temp + " ");
		buffer.put(temp);

		// the following are all 1 byte large.
		temp = this.randSeed & 0xFF;
		buffer.put(temp);
		if (verbose)
			System.err.print(temp + " ");

		temp = this.reserved2 & 0xFF;
		buffer.put(temp);
		if (verbose)
			System.err.print(temp + " ");

		temp = this.reserved3 & 0xFF;
		buffer.put(temp);
		if (verbose)
			System.err.print(temp + " ");

		if (verbose)
			System.err.println();
	}

	public void read(MyByteBuffer buffer)
	{
		this.read(buffer, verbose);
	}

	public void read(MyByteBuffer buffer, boolean verbose)
	{
		int temp;

		if (verbose)
			System.err.print("NSTerritory: read. ");

		temp = buffer.get();
		if (verbose)
			System.err.print(temp + " ");
		{
			this.setCanon(temp & 0x3F);
			this.setRot((temp & 0xC0) >> 6);
		}

		temp = buffer.get();
		if (verbose)
			System.err.print(temp + " ");
		{
			this.setReserved((temp & 0xF0) >> 4);
			this.setDecorated(((temp & 0x08) >> 3) == 1 ? true : false);
			this.setTakenToBattle(((temp & 0x04) >> 2) == 1 ? true : false);
			this.setNotPlacedInPuzzleYet(((temp & 0x02) >> 1) == 1 ? true : false);
			this.setExists((temp & 0x01) == 1 ? true : false);
		}

		temp = buffer.get();
		if (verbose)
			System.err.print(temp + " ");
		{
			this.setXChunkCoord(temp & 0x0F);
			this.setYChunkCoord((temp & 0xF0) >> 4);
		}

		this.setRandSeed(buffer.get());
		if (verbose)
			System.err.print(this.randSeed + " ");
		this.setReserved2(buffer.get());
		if (verbose)
			System.err.print(this.reserved2 + " ");
		this.setReserved3(buffer.get());
		if (verbose)
			System.err.print(this.reserved3 + " ");

		if (verbose)
			System.err.println();
	}

	public int getCanon()
	{
		return canon;
	}

	public void setCanon(int canon)
	{
		assert ((canon & 0x3F) == canon);

		this.canon = canon;
	}

	public boolean isDecorated()
	{
		return decorated;
	}

	public void setDecorated(boolean decorated)
	{
		this.decorated = decorated;
	}

	public boolean isExists()
	{
		return exists;
	}

	public void setExists(boolean exists)
	{
		this.exists = exists;
	}

	public boolean isNotPlacedInPuzzleYet()
	{
		return notPlacedInPuzzleYet;
	}

	public void setNotPlacedInPuzzleYet(boolean notPlacedInPuzzleYet)
	{
		this.notPlacedInPuzzleYet = notPlacedInPuzzleYet;
	}

	public int getRandSeed()
	{
		return randSeed;
	}

	public void setRandSeed(int randSeed)
	{
		assert ((randSeed & 0xFF) == randSeed);

		this.randSeed = randSeed;
	}

	public int getReserved()
	{
		return reserved;
	}

	public void setReserved(int reserved)
	{
		assert ((reserved & 0x0F) == reserved);

		this.reserved = reserved;
	}

	public int getReserved2()
	{
		return reserved2;
	}

	public void setReserved2(int reserved2)
	{
		assert ((reserved2 & 0xFF) == reserved2);

		this.reserved2 = reserved2;
	}

	public int getReserved3()
	{
		return reserved3;
	}

	public void setReserved3(int reserved3)
	{
		assert ((reserved3 & 0xFF) == reserved3);

		this.reserved3 = reserved3;
	}

	public int getRot()
	{
		return rot;
	}

	public void setRot(int rot)
	{
		assert ((rot & 0x03) == rot);

		this.rot = rot;
	}

	public boolean isTakenToBattle()
	{
		return takenToBattle;
	}

	public void setTakenToBattle(boolean takenToBattle)
	{
		this.takenToBattle = takenToBattle;
	}

	public int getXChunkCoord()
	{
		return xChunkCoord;
	}

	public void setXChunkCoord(int chunkCoord)
	{
		assert ((chunkCoord & 0x0F) == chunkCoord);

		xChunkCoord = chunkCoord;
	}

	public int getYChunkCoord()
	{
		return yChunkCoord;
	}

	public void setYChunkCoord(int chunkCoord)
	{
		assert ((chunkCoord & 0x0F) == chunkCoord);

		yChunkCoord = chunkCoord;
	}

	public String toString()
	{
		return "NSTerritory: Canon=" + this.canon + ". Rot=" + this.rot + ". Exists=" + this.exists + ". notPlacedInPuzzleYet=" + this.notPlacedInPuzzleYet + ". TakenToBattle=" + this.takenToBattle + ". Decorated=" + this.decorated + ". Reserved=" + this.reserved + ". xchunkcoord=" + this.xChunkCoord + ". ychunkcoord=" + this.yChunkCoord + ". randSeed=" + this.randSeed + ". Reserved2=" + this.reserved2 + ". Reserved3=" + this.reserved3;
	}

	private int canon = 0;

	private int rot = 0;

	private boolean exists = false;

	private boolean notPlacedInPuzzleYet = false;

	private boolean takenToBattle = false;

	private boolean decorated = false;

	private int reserved = 0;

	private int xChunkCoord = 0;

	private int yChunkCoord = 0;

	private int randSeed = 0;

	private int reserved2 = 0;

	private int reserved3 = 0;

	public static final int MR_TERR_COUNT = 20;
}
