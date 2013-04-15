package org.javastorm;

import org.javastorm.types.Types.NSType;

public class CanonDecoder
{
	// WARNING! I've made an important chance. The second parameter, canon,
	// is normally applicable when you actually have canon variations.
	// Now, it is also meaningful even if canonStruct is null. In that case
	// it is the exact frame to use when drawing the object. -Ken
	public CanonDecoder(NSType type, BoardCoord bc)
	{
		this(type, 0, 0, bc);
	}

	public CanonDecoder(NSType type, int canon, int rot, BoardCoord bc)
	{
		this(type, canon, rot, bc, false);
	}

	public CanonDecoder(NSType type, int canon, int rot, BoardCoord bc, boolean canonIsFrame)
	{
		int w = 0, h = 0;

		this.type = type.getTypeNum();
		this.typeStruct = type;
		this.canonIsFrame = canonIsFrame;
		this.canon = canon;
		this.reflection = false;
		this.rot = rot / 2;
		this.srcPos = bc;
		this.canonStruct = null;
		this.squareSize = 1;

		if (this.typeStruct.getTypeName().equalsIgnoreCase("puzzlePiece"))
		{
			this.canonStruct = this.puzCan[canon];
		}
		else if (this.typeStruct.getTypeName().equalsIgnoreCase("bridge"))
		{

		}
		else if (this.typeStruct.getTypeName().equalsIgnoreCase("island"))
		{

		}
		else if (this.typeStruct.getTypeName().equalsIgnoreCase("noIsland"))
		{

		}
		else if (this.unitUsesCanons(this.typeStruct))
		{

		}
		else
		{
			this.canonStruct = null;
			w = 1;
			h = 1;
		}

		if (this.canonStruct != null)
		{
			w = (int) this.canonStruct.w;
			h = (int) this.canonStruct.h;
		}

		this.priDim = (this.rot & 1) != 0 ? w : h;
		this.secDim = (this.rot & 1) != 0 ? w : h;

		this.srcX = CanonDecoder.srcXOff[rot] * (w - 1);
		this.srcY = CanonDecoder.srcYOff[rot] * (h - 1);

		if (reflection)
		{
			this.srcX = (w - 1) - this.srcX;
		}

		this.pri = this.sec = 0;
		this.valid = true;

		this.getNext();
	}

	private boolean unitUsesCanons(NSType type)
	{
		return (type.getTypeName().equalsIgnoreCase("thunderCannonType") || type.getTypeName().equalsIgnoreCase("rainCannonType") || type.getTypeName().equalsIgnoreCase("windArcherType") || type.getTypeName().equalsIgnoreCase("windBlockerType"));
	}

	public void getNext()
	{
		do
		{
			if (this.sec > this.secDim)
			{
				int reflX = this.reflection ? -priX[this.rot] : priX[this.rot];
				this.srcX = priX[this.rot] != 0 ? (this.srcX + reflX + this.priDim) % this.priDim : this.srcX;
				this.srcY = priY[this.rot] != 0 ? (this.srcY + priY[this.rot] + this.priDim) % this.priDim : this.srcY;

				if (++this.pri >= this.priDim)
				{
					this.valid = false;
					return;
				}

				this.sec = 0;
			}

			this.frame = -1;
			int orient, vari;

			if (canonStruct == null)
			{
				if (this.canonIsFrame)
					this.frame = this.canon;
				else
					this.frame = this.typeStruct.getDefaultFrame();
			}
			else
			{
				CanonInfo info;
				if ((info = this.canonStruct.getDesc(this.srcX, this.srcY)) != null)
				{
					orient = info.orientation;
					vari = info.variation;
					this.num = info.num;

					if (this.reflection)
					{
						this.frame = this.typeStruct.getFrame((char) transform[(rot + 2) % 4][orient - 'A'], (char) transform[(rot + 2) % 4]['P' - 'A'], (char) vari);
					}
					else
					{
						this.frame = this.typeStruct.getFrame((char) transform[rot][orient - 'A'], (char) transform[rot]['P' - 'A'], (char) vari);
					}
				}
			}
			this.curPos = new BoardCoord(srcPos.getX() + sec * squareSize, srcPos.getY() + pri * squareSize);

			int reflX = this.reflection ? -secX[this.rot] : secX[this.rot];
			this.srcX = secX[this.rot] != 0 ? (this.srcX + reflX + this.secDim) % this.secDim : this.srcX;
			this.srcY = secY[this.rot] != 0 ? (this.srcY + secY[this.rot] + this.secDim) % this.secDim : this.srcY;

			sec++;
		}
		while (this.frame == -1);
	}

	public BoardCoord getCenter()
	{
		int footX, footY;
		footX = this.typeStruct.getFootpadX();
		footY = this.typeStruct.getFootpadY();

		BoardCoord pos = getPos();
		return new BoardCoord(pos.getX() - (float) footX / 2.0f, pos.getY() - (float) footY / 2.0f);
	}

	public BoardCoordRect getRect()
	{
		int footX, footY;
		footX = this.typeStruct.getFootpadX();
		footY = this.typeStruct.getFootpadY();

		BoardCoord br = getPos();
		BoardCoord tl = new BoardCoord(br.getX() - (footX - 1), br.getY() - (footY - 1));
		return new BoardCoordRect(br, tl);
	}

	public int getOrientation()
	{
		return this.typeStruct.getFrameInfo(getFrame()).getSideOrientation();
	}

	public boolean isValid()
	{
		return this.valid;
	}

	public BoardCoord getPos()
	{
		return this.curPos;
	}

	public int getFrame()
	{
		return this.frame;
	}

	public int getType()
	{
		return this.type;
	}

	public int getNorthNum()
	{
		return this.num;
	}

	public Rect getFullRect()
	{
		int footX, footY;
		footX = this.typeStruct.getFootpadX();
		footY = this.typeStruct.getFootpadY();

		Rect r;
		r = new Rect((int) this.curPos.getX() - (footX * this.secDim), (int) this.curPos.getY() - (footY * this.priDim), (int) this.curPos.getX(), (int) this.curPos.getY());

		return r;
	}

	public static class CanonInfo
	{
		public int orientation;

		public int variation;

		public int num;
	}

	private class CanonStruct
	{
		public CanonStruct(int probability, int w, int h, String... args)
		{
			this.probability = probability;
			this.w = w;
			this.h = h;

			this.elements = new long[args.length];
			for (int i = 0; i < args.length; i++)
			{
				char[] elementParts = args[i].toCharArray();

				// We have to compact the strings into a long. Funky eh?
				// They get packed in order. E.g "st" = 0x7374
				this.elements[i] = 0;
				for (int j = 0; j < elementParts.length; j++)
					this.elements[i] |= elementParts[j] << (((elementParts.length - 1) - j) * 8);
			}
		}

		public CanonInfo getDesc(int x, int y)
		{
			CanonInfo info = new CanonInfo();
			long a = this.elements[(int) (y * w + x)];

			info.num = (int) (a >> 24) & 0xFF;
			info.num -= 'a';

			info.orientation = (int) (a >> 8) & 0xFF;
			info.variation = (int) (a & 0xFF);
			info.variation -= '0';

			if (info.orientation == '.')
				return null;

			return info;
		}

		long probability;

		long w, h;

		long elements[];
	}

	private static int srcXOff[] =
	{ 0, 0, 1, 1 };

	private static int srcYOff[] =
	{ 0, 1, 1, 0 };

	private static int priX[] =
	{ 0, 1, 0, -1 };

	private static int priY[] =
	{ 1, 0, -1, 0 };

	private static int secX[] =
	{ 1, 0, -1, 0 };

	private static int secY[] =
	{ 0, -1, 0, 1 };

	private static int transform[][] =
	{
	{ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P' },
	{ 'A', 'C', 'D', 'E', 'B', 'G', 'H', 'I', 'F', 'K', 'J', 'M', 'N', 'O', 'L', 'P' },
	{ 'A', 'D', 'E', 'B', 'C', 'H', 'I', 'F', 'G', 'J', 'K', 'N', 'O', 'L', 'M', 'P' },
	{ 'A', 'E', 'B', 'C', 'D', 'I', 'F', 'G', 'H', 'K', 'J', 'O', 'L', 'M', 'N', 'P' }, };

	CanonStruct puzCan[] =
	{
	// 0
	new CanonStruct(1, 3, 3, "..", "L1", "..", "F1", "A1", "G1", "I1", "N1", "H1"),

	// 1
	new CanonStruct(1, 3, 3, "F1", "G1", "..", "B1", "A1", "G1", "I1", "N1", "H1"),

	// DANGER!! Do not change the location of this canon
	// in the list. It is used by the archipelago code.
	// If you must move it, change puzCanon3x3 above. -Ken
	// 2
	new CanonStruct(1, 3, 3, "F1", "C1", "G1", "B1", "A1", "D1", "I1", "N1", "H1"),

	// 3
	new CanonStruct(1, 2, 2, "O1", "G1", "..", "N1"),

	// 4
	new CanonStruct(1, 3, 1, "O1", "K1", "M1"),

	// 5
	new CanonStruct(1, 3, 2, "O1", "K1", "G1", "..", "..", "N1"),

	// 6
	new CanonStruct(1, 3, 2, "O1", "C1", "M1", "..", "N1", ".."),

	// 7
	new CanonStruct(1, 2, 2, "F1", "G1", "I1", "H1"),

	// 8
	new CanonStruct(1, 3, 2, "O1", "G1", "..", "..", "I1", "M1"),

	// 9
	new CanonStruct(1, 3, 2, "O1", "C1", "G1", "..", "I1", "H1"),

	// 10
	new CanonStruct(1, 3, 2, "F1", "K1", "G1", "N1", "..", "N1"),

	// 11
	new CanonStruct(1, 3, 3, "..", "L1", "..", "O1", "A1", "M1", "..", "N1", ".."),

	// 12
	new CanonStruct(1, 3, 3, "L1", "..", "..", "I1", "G1", "..", "..", "I1", "M1"),

	// 13
	new CanonStruct(1, 3, 3, "F1", "K1", "G1", "J1", "..", "J1", "I1", "K1", "H1"),

	// 14
	new CanonStruct(1, 3, 3, "..", "L1", "..", "O1", "A1", "G1", "..", "I1", "H1"),

	// 15
	new CanonStruct(1, 3, 3, "..", "..", "L1", "O1", "C1", "D1", "..", "I1", "H1"),

	// 16
	new CanonStruct(1, 3, 2, "O1", "C1", "G1", "..", "I1", "H1"),

	// 17
	new CanonStruct(1, 3, 3, "F1", "K1", "G1", "N1", "..", "J1", "..", "..", "N1"),

	// 18
	new CanonStruct(1, 3, 2, "..", "F1", "M1", "O1", "H1", ".."),

	// 19
	new CanonStruct(1, 3, 3, "L1", "..", "..", "B1", "K1", "G1", "N1", "..", "N1"),

	// 20
	new CanonStruct(1, 3, 3, "..", "L1", "..", "F1", "E1", "G1", "N1", "..", "N1"), };

	private CanonStruct canonStruct;

	private boolean valid, canonIsFrame, reflection;

	private int type, canon, rot, frame, num, squareSize;

	private BoardCoord curPos, srcPos;

	private NSType typeStruct;

	int priDim, secDim;

	int srcX, srcY;

	int pri, sec;
}
