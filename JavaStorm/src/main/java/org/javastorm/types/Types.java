package org.javastorm.types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import org.javastorm.BoardCoord;
import org.javastorm.BoardCoordRect;
import org.javastorm.Filesystem;
import org.javastorm.IntList;
import org.javastorm.NSUtil;
import org.javastorm.renderer.Renderer;
import org.javastorm.shapes.ShapeFile;
import org.javastorm.shapes.ShapeFile.ArtData;
import org.javastorm.shapes.ShapeFile.VFXFrame;
import org.javastorm.squids.BaseSquid;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidConstructor;
import org.javastorm.squids.ProcessForm.NSProcessFormConstructor;

import com.primalworld.math.MathEvaluator;

public final class Types
{
	private static String typeFiles[];

	private static String processes[];

	private static int numProcessTypes;

	public static boolean loadTypelist(Reader in)
	{
		Vector<String> typeFileList = new Vector<String>();
		Vector<String> processList = new Vector<String>();
		BufferedReader reader = new BufferedReader(in);
		String line;

		try
		{
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.startsWith("//"))
					continue;
				if (line.startsWith("process"))
				{
					String type[] = line.trim().split(" ");
					processList.add(type[1] + "Process");
				}
				else
				{
					String type[] = line.trim().split(" ");
					typeFileList.add(type[0]);
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return false;
		}

		typeFiles = typeFileList.toArray(new String[0]);
		processes = processList.toArray(new String[0]);
		numProcessTypes = processes.length;
		return true;
	}

	public static final boolean loadNamevars(Reader in)
	{
		BufferedReader reader;

		try
		{
			reader = new BufferedReader(in);

			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();

				if (line.startsWith("#define"))
				{
					String[] define = line.split(" ");
					if (define.length < 3)
						continue;

					m.addVariable(define[1].trim(), Integer.parseInt(define[2].trim()));
				}

				String[] expression = line.split("=");
				if (expression.length < 2)
					continue;

				String typeName = expression[0].replace("int", "").replace("Type", "").trim();
				String value = expression[1].trim();
				value = value.substring(0, value.length() - 1);
				m.setExpression(value);

				NSType unit = Types.findByTypeName(typeName);
				if (unit == null)
					continue;
				double typeNum = m.getValue();
				unit.typeNum = (int) typeNum;
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return false;
		}

		return true;
	}

	private static MathEvaluator m = new MathEvaluator();

	public static boolean load(Filesystem fs)
	{
		if (typeFiles == null)
			return false;

		Vector<NSType> typesVec = new Vector<NSType>();
		BufferedReader reader;

		int loadOrder = 0;

		m.addVariable("FIRST_PROCESS_TYPE", 10);
		m.addVariable("NUM_PROCESS_TYPES", 60);
		m.addVariable("FIRST_GENERATED_REAL_TYPE", 10 + 60);
		for (int i = 0; i < MainSquid.zOrderStrings.length; i++)
		{
			m.addVariable(MainSquid.zOrderStrings[i].zOrder, MainSquid.zOrderStrings[i].zOrderValue);
		}

		for (int i = 0; i < 10; i++)
		{
			NSType type = new NSType();
			type.typeNum = i;
			type.notifyPostLoad();
			typesVec.add(type);
		}
		for (int i = 0; i < typeFiles.length; i++)
		{
			boolean typeFrames = false;
			Vector<NSTypeFrame> frames = new Vector<NSTypeFrame>();

			try
			{
				NSType type = new NSType();

				Reader in = fs.open("\\d\\" + typeFiles[i] + ".type");

				if (in != null)
				{
					reader = new BufferedReader(in);

					String line;
					while ((line = reader.readLine()) != null)
					{
						if (line.trim().equals(""))
							continue;
						if (line.trim().startsWith("//"))
							continue;

						if (line.trim().startsWith("typename"))
						{
							line = line.replace("typename", "").trim();
							int end = line.indexOf(" ");
							if (end == -1)
								end = line.length();

							String typeName = line.substring(0, end);
							type.typeName = typeName;

							continue;
						}

						if (line.trim().startsWith("typeflags"))
						{
							line = line.replace("typeflags", "").trim();

							if (line.indexOf(";") > -1)
								line = line.substring(0, line.indexOf(";"));
							
							String[] typeFlags = line.split(" ");
							for (int j = 0; j < typeFlags.length; j++)
							{
								// MAIN TYPE FLAGS.
								if (typeFlags[j].equalsIgnoreCase("dontdrawdefault"))
								{
									assert(false);
								}
								
								if (typeFlags[j].equalsIgnoreCase("carribleInVehicle"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_CARRIABLE_IN_VEHICLE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("container"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_CONTAINER;
								}
								
								if (typeFlags[j].equalsIgnoreCase("default_hotspot") || typeFlags[j].equalsIgnoreCase("defaulthotspot"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_DEFAULT_HOTSPOT;
								}
								
								if (typeFlags[j].equalsIgnoreCase("shadow"))
								{
									assert( (type.typeFlags & NSType.TYPE_FLAG_FLYER_SHADOW) == 0 );
									type.typeFlags |= NSType.TYPE_FLAG_SHADOW;
								}
								
								if (typeFlags[j].equalsIgnoreCase("flyershadow"))
								{
									assert( (type.typeFlags & NSType.TYPE_FLAG_SHADOW) == 0 );
									type.typeFlags |= NSType.TYPE_FLAG_FLYER_SHADOW;
								}
								
								if (typeFlags[j].equalsIgnoreCase("not_selectable"))
								{
									type.typeFlags |= NSType.TYPE_NOT_SELECTABLE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("not_real") || typeFlags[j].equalsIgnoreCase("notreal"))
								{
									type.genus |= NSType.gNOT_REAL;
									type.typeFlags |= NSType.TYPE_NOT_SELECTABLE;
									type.typeFlags |= NSType.TYPE_FLAG_DONT_SAVE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("createisland"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_CREATE_ISLAND;
								}
								
								if (typeFlags[j].equalsIgnoreCase("mayDropOnRim"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_MAY_DROP_ON_RIM;
								}
								
								if (typeFlags[j].equalsIgnoreCase("mayDropOnIsle"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_MAY_DROP_ON_ISLE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("surface"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_IS_SURFACE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("randframe"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_RAND_FRAME;
								}
								
								if (typeFlags[j].equalsIgnoreCase("matchframe"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_MATCH_FRAME;
								}
								
								if (typeFlags[j].equalsIgnoreCase("opaqueCollide"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_OPAQUE_COLLIDE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("dontSave"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_DONT_SAVE;
								}
								
								if (typeFlags[j].equalsIgnoreCase("saveFrame"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_SAVE_FRAME;
								}
								
								if (typeFlags[j].equalsIgnoreCase("saveQA"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_SAVE_Q8;
								}
								
								if (typeFlags[j].equalsIgnoreCase("saveQB"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_SAVE_Q16;
								}
								
								// GENUS TYPE FLAGS.
								if (typeFlags[j].equalsIgnoreCase("dropblocking"))
								{
									type.genus |= NSType.gDROP_BLOCKING;
								}

								if (typeFlags[j].equalsIgnoreCase("blocking"))
								{
									type.genus |= NSType.gDROP_BLOCKING;
									type.genus |= NSType.gWALK_BLOCKING;
								}

								if (typeFlags[j].equalsIgnoreCase("shotblocking"))
								{
									type.genus |= NSType.gSHOT_BLOCKING;
								}

								if (typeFlags[j].equalsIgnoreCase("fencePost"))
								{
									type.genus |= NSType.gFENCEPOST;
								}

								if (typeFlags[j].equalsIgnoreCase("fence"))
								{
									type.genus |= NSType.gFENCE;
								}

								if (typeFlags[j].equalsIgnoreCase("buried"))
								{
									type.genus |= NSType.gBURIED;
								}

								if (typeFlags[j].equalsIgnoreCase("nugget"))
								{
									type.genus |= NSType.gNUGGET;
								}

								if (typeFlags[j].equalsIgnoreCase("bomb"))
								{
									type.genus |= NSType.gBOMB;
								}

								if (typeFlags[j].equalsIgnoreCase("emplacement"))
								{
									type.genus |= NSType.gEMPLACEMENT;
								}

								if (typeFlags[j].equalsIgnoreCase("bridge"))
								{
									type.genus |= NSType.gBRIDGE;
								}

								if (typeFlags[j].equalsIgnoreCase("walker"))
								{
									type.genus |= NSType.gWALKER;
								}

								if (typeFlags[j].equalsIgnoreCase("balloon"))
								{
									type.genus |= NSType.gBALLOON;
								}

								if (typeFlags[j].equalsIgnoreCase("island"))
								{
									type.genus |= NSType.gISLAND;
								}

								if (typeFlags[j].equalsIgnoreCase("islandThreeByThree"))
								{
									type.genus |= NSType.gISLAND3x3;
								}

								if (typeFlags[j].equalsIgnoreCase("factory"))
								{
									type.genus |= NSType.gFACTORY;
								}

								if (typeFlags[j].equalsIgnoreCase("vortex"))
								{
									type.genus |= NSType.gVORTEX;
								}

								if (typeFlags[j].equalsIgnoreCase("edgefarm"))
								{
									type.genus |= NSType.gEDGEFARM;
								}

								if (typeFlags[j].equalsIgnoreCase("focus"))
								{
									//type.genus |= NSType.gFOCUS;
								}

								if (typeFlags[j].equalsIgnoreCase("guy"))
								{
									type.genus |= NSType.gGUY;
								}

								if (typeFlags[j].equalsIgnoreCase("geyser"))
								{
									type.genus |= NSType.gGEYSER;
								}

								if (typeFlags[j].equalsIgnoreCase("flyer"))
								{
									type.genus |= NSType.gFLYER;
								}

								if (typeFlags[j].equalsIgnoreCase("residence"))
								{
									type.genus |= NSType.gRESIDENCE;
								}

								if (typeFlags[j].equalsIgnoreCase("fringe"))
								{
									type.genus |= NSType.gFRINGE;
								}
								
								// Flags used during compression
								//------------------------------
								if(typeFlags[j].equalsIgnoreCase("priest"))
								{
									type.genus |= NSType.gPRIEST;
								}
								
								if(typeFlags[j].equalsIgnoreCase("dais"))
								{
									type.genus |= NSType.gDAIS;
								}
								
								if(typeFlags[j].equalsIgnoreCase("altar"))
								{
									type.genus |= NSType.gALTAR;
								}
								
								if(typeFlags[j].equalsIgnoreCase("predictable"))
								{
									// TODO
									//type.cFlags |= cPREDICTABLE;
								}
								
								// OTHER TYPE FLAGS.
								if (typeFlags[j].equalsIgnoreCase("flyershadow"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_FLYER_SHADOW;
								}

								if (typeFlags[j].equalsIgnoreCase("shadow"))
								{
									type.typeFlags |= NSType.TYPE_FLAG_SHADOW;
								}
							}

							continue;
						}

						if (line.indexOf("//") > -1)
						{
							line = line.substring(0, line.indexOf("//") - 2);
						}

						if (line.trim().equals("}"))
						{
							typeFrames = true;
							continue;
						}

						if (!typeFrames)
						{
							if (line.trim().equals("}"))
							{
								typeFrames = true;
								break;
							}

							String[] parts = line.split("=");
							if (parts.length < 2)
								continue;

							String property = parts[0].trim();
							String value = parts[1].trim();

							value = value.replace(";", "");

							if (property.equalsIgnoreCase("description"))
							{
								type.name = value.replace("\"", "");
							}
							else if (property.equalsIgnoreCase("techbit"))
							{
								type.techBit = Integer.parseInt(value);
							}

							else if (property.equalsIgnoreCase("level"))
							{
								type.level = Integer.parseInt(value) - 1;
							}

							else if (property.equalsIgnoreCase("maxHitPoints"))
							{
								type.typeFlags |= NSType.TYPE_FLAG_HAS_HIT_POINTS;
								type.maxHitPoints = Integer.parseInt(value);
							}

							else if (property.equalsIgnoreCase("cost"))
							{
								type.cost = Integer.parseInt(value);
							}

							else if (property.equalsIgnoreCase("cost"))
							{
								type.cost = Integer.parseInt(value);
							}

							else if (property.equalsIgnoreCase("theme"))
							{
								type.alignment = value.replace("\"", "");
							}
							else if (property.equalsIgnoreCase("group"))
							{
								type.group = Types.stringToGroup(value.replace("\"", ""));
							}
							else if (property.equalsIgnoreCase("footx") || property.equalsIgnoreCase("foot_x"))
							{
								type.footX = Integer.parseInt(value);
							}
							else if (property.equalsIgnoreCase("footy") || property.equalsIgnoreCase("foot_y"))
							{
								type.footY = Integer.parseInt(value);
							}
							else if (property.equalsIgnoreCase("hotFootRatioX"))
							{
								type.hotFootX = (int) ((float) Renderer.X_TILE_DIM * Float.parseFloat(value));
							}
							else if (property.equalsIgnoreCase("hotFootRatioY"))
							{
								type.hotFootY = (int) ((float) Renderer.Y_TILE_DIM * Float.parseFloat(value));
							}
							else if (property.equalsIgnoreCase("height"))
							{
								type.height = Integer.parseInt(value);
							}
							else if (property.equalsIgnoreCase("zorder"))
							{
								m.setExpression(value.replace("\"", ""));
								Double zOrder = m.getValue();
								if (zOrder == null)
									type.zOrder = 0;
								else
								{
									double zOrderPrimitive = zOrder;
									type.zOrder = (int) zOrderPrimitive;
								}
							}
						}
						else
						{
							NSTypeFrame frame = new NSTypeFrame();

							String frameData[] = line.replace("\"", "").split(":");
							if (frameData.length < 3)
							{
								throw new Error("Bad frame data '" + line + "'");
							}

							frame.sideOrientation = frameData[0].trim().charAt(0);
							frameData[0] = frameData[0].substring(1);
							if (frameData[0].charAt(0) >= 'A' && frameData[0].charAt(0) <= 'Z')
							{
								frame.cornerOrientation = frameData[0].trim().charAt(0);
								frameData[0] = frameData[0].substring(1);
							}
							else
								frame.cornerOrientation = 'P';

							frame.variation = Integer.parseInt(frameData[0].trim().substring(0, 2));

							String[] frameFlags = frameData[1].trim().split(" ");

							for (int j = 0; j < frameFlags.length; j++)
							{
								if (frameFlags[j].equalsIgnoreCase("fringe"))
								{
									frame.flags |= NSTypeFrame.FF_FRINGE;
								}
								if (frameFlags[j].equalsIgnoreCase("suck"))
								{
									frame.flags |= NSTypeFrame.FF_SUCK;
								}
								if (frameFlags[j].equalsIgnoreCase("rim"))
								{
									frame.flags |= NSTypeFrame.FF_RIM;
								}
								if (frameFlags[j].equalsIgnoreCase("lit"))
								{
									frame.flags |= NSTypeFrame.FF_LIT;
								}
								if (frameFlags[j].equalsIgnoreCase("unlit"))
								{
									frame.flags |= NSTypeFrame.FF_UNLIT;
								}
								if (frameFlags[j].equalsIgnoreCase("cracked"))
								{
									frame.flags |= NSTypeFrame.FF_CRACKED;
								}
								if (frameFlags[j].equalsIgnoreCase("hard"))
								{
									frame.flags |= NSTypeFrame.FF_HARD;
								}
								if (frameFlags[j].equalsIgnoreCase("default"))
									type.defaultFrame = frames.size();
							}

							frames.add(frame);
						}
					}

					type.frames = frames.toArray(new NSTypeFrame[0]);
				}

				type.loadOrder = loadOrder++;
				type.notifyPostLoad();
				typesVec.add(type);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				continue;
			}
		}

		for (int i = 0; i < processes.length; i++)
		{
			NSType type = new NSType();
			type.typeName = processes[i];
			type.notifyPostLoad();
			typesVec.add(type);
		}

		Types.types = typesVec.toArray(new NSType[0]);

		return true;
	}

	public static final boolean isProcessType(int type)
	{
		return Types.FIRST_PROCESS_TYPE <= type && type < (numProcessTypes + Types.FIRST_PROCESS_TYPE);
	}

	private static int stringToGroup(String group)
	{
		for (int i = 0; i < Types.groupNames.length; i++)
		{
			if (group.equalsIgnoreCase(Types.groupNames[i]))
			{
				return i;
			}
		}

		return -1;
	}

	public static NSType findByName(String name)
	{
		for (int i = 0; i < Types.types.length; i++)
		{
			NSType unit = Types.types[i];
			if (unit.getName() != null)
				if (unit.getName().equalsIgnoreCase(name))
					return unit;
		}

		return null;
	}

	public static NSType findByLoadOrder(int lo)
	{
		for (int i = 0; i < Types.types.length; i++)
		{
			NSType unit = Types.types[i];
			if (unit.loadOrder == lo)
				return unit;
		}

		return null;
	}

	public static NSType findByTypeName(String name)
	{
		for (int i = 0; i < Types.types.length; i++)
		{
			NSType unit = Types.types[i];
			if (unit.getTypeName() != null)
				if (unit.getTypeName().equalsIgnoreCase(name))
					return unit;
		}

		return null;
	}

	public static NSType findByTypeNum(int typeNum)
	{
		for (int i = 0; i < Types.types.length; i++)
		{
			NSType unit = Types.types[i];
			if (unit.getTypeNum() == typeNum)
				return unit;
		}

		return null;
	}

	public static void loadShapeData(ShapeFile shape)
	{
		NSType type;
		NSTypeFrame frames[];

		for (int i = 0; i < types.length; i++)
		{
			type = types[i];
			if (type.getLoadOrder() == -1)
				continue;
			frames = type.frames;

			for (int j = 0; j < type.getFrameCount(); j++)
			{
				frames[j].frame = shape.getFrame(type.getLoadOrder(), j);
			}
		}
	}

	public static NSType[] getAllTypes()
	{
		return Types.types;
	}

	public static class NSTypeFrame
	{
		public char getFlags()
		{
			return this.flags;
		}

		public char getSideOrientation()
		{
			return sideOrientation;
		}

		public char getCornerOrientation()
		{
			return cornerOrientation;
		}

		public int getVariation()
		{
			return variation;
		}

		public VFXFrame getFrame()
		{
			return this.frame;
		}

		private VFXFrame frame;

		private char sideOrientation;

		private char cornerOrientation;

		private int variation;

		private char flags;

		public static final int EMPTY_FRAME = -1;

		public static final int FF_FRINGE = 0x01; // should have a stalactite under it 

		public static final int FF_SUCK = 0x02; // suck-move frame, needs to be drawn over base frame

		public static final int FF_RIM = 0x04; // This is a rim piece. See TYPE_FLAG_MAY_BUILD_ON_RIM

		public static final int FF_LIT = 0x08;

		public static final int FF_UNLIT = 0x10;

		public static final int FF_CRACKED = 0x20;

		public static final int FF_HARD = 0x40;
	}

	public static class NSType
	{
		private NSType()
		{
			// Defaults.
			this.footX = 1;
			this.footY = 1;
			this.gumpFrame = -1;
			this.typeFlagsObj = new TypeFlags();
		}

		public void registerConstructor(SquidConstructor constructor)
		{
			this.constructor = constructor;
		}

		public SquidConstructor getConstructor()
		{
			return this.constructor;
		}

		public ArtData getArtDim(boolean isAbstract, int frame)
		{
			//assert( frameCheck( isAbstract, frame, false ) );

			if (this.frames[frame].frame != null)
				return this.frames[frame].frame.getArtData();
			else
				return null;
		}

		//		 This will pick a random variation out of the frames that best match what you're
		//		 looking for. If no frames are possible it will return EMPTY_FRAME, which you
		//		 must check for yourself. -KLD
		public int getAnyBestFrame(char sideOrientation, char cornerOrientation)
		{
			return this.getAnyBestFrame(sideOrientation, cornerOrientation, -1, -1);
		}

		public int getAnyBestFrame(char sideOrientation, char cornerOrientation, int frameFlags, int islandflag)
		{
			IntList possible = new IntList(256);

			NSTypeFrame f;

			for (int i = 0; i < this.frames.length; i++)
			{
				f = this.frames[i];
				if (f.sideOrientation == sideOrientation && f.cornerOrientation == cornerOrientation && (frameFlags == f.flags || frameFlags == -1 || ((frameFlags & f.flags) != 0)))
				{
					possible.add(i);
				}
			}

			if (possible.isEmpty() && frameFlags != -1)
			{
				return NSTypeFrame.EMPTY_FRAME;
			}
			// I added this so that islands would have a better chance 
			// of preventing beetles from walking in crazy places. -Jim
			if (possible.isEmpty())
			{
				// see comment in boardcoord.cpp
				int newSide, newCorner;
				int[] ret = NSUtil.trimOrientation(sideOrientation, cornerOrientation);
				newSide = ret[0];
				newCorner = ret[1];

				for (int i = 0; i < this.frames.length; i++)
				{
					f = this.frames[i];
					if (f.sideOrientation == newSide && f.cornerOrientation == newCorner)
					{
						possible.add(i);
					}
				}
			}

			// I added this so that islands would have a better chance 
			// of working. -Ken
			if (possible.isEmpty())
			{
				for (int i = 0; i < this.frames.length; i++)
				{
					f = this.frames[i];
					if (f.sideOrientation == sideOrientation && f.cornerOrientation == 'A')
					{
						possible.add(i);
					}
				}
			}

			if (possible.isEmpty())
			{
				for (int i = 0; i < this.frames.length; i++)
				{
					f = this.frames[i];
					if (f.sideOrientation == sideOrientation)
					{
						possible.add(i);
					}
				}
			}

			if (possible.isEmpty())
			{
				return NSTypeFrame.EMPTY_FRAME;
			}
			else
			{
				if (islandflag == -1)
				{
					return possible.getRandom();
				}
				else
				{
					// Fleet Says NOTE THIS!
					// note the 4 is how many diffrent theams we have
					int value = NSUtil.fastRandomInt((islandflag * ((possible.getTotal() / 4))) + 1, ((possible.getTotal() / 4) * (islandflag + 1)));
					return possible.get(value);
				}
			}

			//return NSTypeFrame.EMPTY_FRAME;
		}

		public int getFirstFrame(int cluster)
		{
			return clusterToFrame[cluster];
		}

		public int getFirstFrame(char sideOrientation, char cornerOrientation, int frameFlags)
		{
			NSTypeFrame f;
			for (int i = 0; i < this.frames.length; i++)
			{
				f = this.frames[i];
				if (f.sideOrientation == sideOrientation && f.cornerOrientation == cornerOrientation && frameFlags == f.flags)
				{
					return i;
				}
			}
			return NSTypeFrame.EMPTY_FRAME;
		}

		public int getFrame(char sideOrientation, char cornerOrientation, char variation)
		{
			for (int i = 0; i < this.frames.length; i++)
			{
				if (this.frames[i].sideOrientation == sideOrientation && this.frames[i].cornerOrientation == cornerOrientation && this.frames[i].variation == variation)
				{
					return i;
				}
			}

			return -1;
		}

		public int getOrientation(int frame)
		{
			return this.frames[frame].sideOrientation;
		}

		public int getCornerOrientation(int frame)
		{
			return this.frames[frame].cornerOrientation;
		}

		public NSTypeFrame getFrameInfo(int frame)
		{
			return this.frames[frame];
		}

		public int getDefaultFrame()
		{
			return this.defaultFrame;
		}

		public int getGumpFrame()
		{
			return this.gumpFrame;
		}

		public BoardCoordRect getRect(BoardCoord br)
		{
			int footX, footY;
			footX = this.getFootpadX();
			footY = this.getFootpadY();
			BoardCoord tl = new BoardCoord(br.getX() - (footX - 1), br.getY() - (footY - 1));
			return new BoardCoordRect(tl, br);
		}

		public int getHeight()
		{
			return this.height;
		}

		public int getFootpadX()
		{
			return this.footX;
		}

		public int getFootpadY()
		{
			return this.getFootpadY(false);
		}

		public int getFootpadY(boolean considerHeight)
		{
			return this.footY + (considerHeight ? this.height : 0);
		}

		public NSTypeFrame[] getAllFrames()
		{
			return this.frames;
		}

		public int getFrameCount()
		{
			if (this.frames == null)
				return 0;
			return this.frames.length;
		}

		public int getLoadOrder()
		{
			return loadOrder;
		}

		public int getGroup()
		{
			return group;
		}

		public int getGenus()
		{
			return genus;
		}

		public int getMaxHitPoints()
		{
			return maxHitPoints;
		}

		public TypeFlags getTypeFlags()
		{
			return this.typeFlagsObj;
		}

		public int getZOrder()
		{
			return zOrder;
		}

		public String getName()
		{
			return this.name;
		}

		public String getAlignment()
		{
			return this.alignment;
		}

		public int getTechBit()
		{
			return this.techBit;
		}

		public int getLevel()
		{
			return this.level;
		}

		public int getCost()
		{
			return this.cost;
		}

		public String getTypeName()
		{
			return this.typeName;
		}

		public int getTypeNum()
		{
			return this.typeNum;
		}

		public int getHotFootX()
		{
			return hotFootX;
		}

		public int getHotFootY()
		{
			return hotFootY;
		}

		public int getDefaultDepth()
		{
			return defaultDepth;
		}

		public void setDefaultDepth(int defaultDepth)
		{
			this.defaultDepth = defaultDepth;
		}

		private void notifyPostLoad()
		{
			if (this.typeNum == 1)
			{
				this.name = "DependForm";
				this.typeName = "dependForm";
			}
			if (this.typeNum == 2)
			{
				this.name = "ProcessForm";
				this.typeName = "processForm";
				this.constructor = new NSProcessFormConstructor();
			}
			if (this.typeNum == 3)
			{
				this.name = "GumpForm";
				this.typeName = "gumpForm";
			}
			if (this.typeNum == 4)
			{
				this.name = "PlayerSquid";
				this.typeName = "playerSquid";
			}
			if (this.typeNum == 6)
			{
				this.name = "ContentForm";
				this.typeName = "contentForm";
			}

			if (this.group == grBATTERY)
			{
				this.genus |= gBATTERY;
			}

			if (this.group == grCANNON || this.group == grARCHER)
			{
				this.genus |= gSHOOTER;
			}

			if (this.group == grBLOCKER)
			{
				this.genus |= gBLOCKER;
			}
			
			if((genus & (gFACTORY|gCARRIER|gVORTEX)) > 0 ) {
				typeFlags |= TYPE_FLAG_CONTAINER;
				
				// TODO:
				if((genus & (gCARRIER)) > 0 ) {
					//defaultListFlags |= CFF_CONTENTS;
				}
				else {
					//defaultListFlags |= CFF_PRODUCT;
				}
			}	

			if (this.group != NO_GROUP)
				if (this.cost == 0)
				{
					if ((this.genus & gCARRIER) != 0)
					{
						this.cost = 400 * (this.level + 1);
					}
					else
					{
						this.cost = 200 * (this.level + 1);
					}
				}

			this.fillOrientationArrays();
		}

		private void fillOrientationArrays()
		{
			if (this.frames == null)
				return;

			numClusters = 0;
			this.clusterToFrame = new int[16];
			this.clusterNumFrames = new int[16];

			for (int t = 0; t < 16; ++t)
			{
				this.clusterToFrame[t] = NSTypeFrame.EMPTY_FRAME;
				this.clusterNumFrames[t] = 0;

				NSTypeFrame f;
				for (int i = 0; i < this.frames.length; i++)
				{
					f = this.frames[i];
					if (f.sideOrientation == 'A' + t)
					{
						if (this.clusterToFrame[t] == NSTypeFrame.EMPTY_FRAME)
						{
							this.numClusters++;
							this.clusterToFrame[t] = i;
						}
						++this.clusterNumFrames[t];
					}
				}
			}
		}

		public class TypeFlags
		{
			private TypeFlags()
			{

			}

			public boolean container()
			{
				return (typeFlags & TYPE_FLAG_CONTAINER) != 0;
			}

			public boolean saveQ8()
			{
				return (typeFlags & TYPE_FLAG_SAVE_Q8) != 0;
			}

			public boolean saveQ16()
			{
				return (typeFlags & TYPE_FLAG_SAVE_Q16) != 0;
			}

			public boolean saveFrame()
			{
				return (typeFlags & TYPE_FLAG_SAVE_FRAME) != 0;
			}

			public boolean hasHitPoints()
			{
				return (typeFlags & TYPE_FLAG_HAS_HIT_POINTS) != 0;
			}

			public boolean matchFrame()
			{
				return (typeFlags & TYPE_FLAG_MATCH_FRAME) != 0;
			}

			public boolean randFrame()
			{
				return (typeFlags & TYPE_FLAG_RAND_FRAME) != 0;
			}

			public boolean isSurface()
			{
				return (typeFlags & TYPE_FLAG_IS_SURFACE) != 0;
			}

			public boolean usesMana()
			{
				return (typeFlags & TYPE_FLAG_USES_MANA) != 0;
			}
		}

		private TypeFlags typeFlagsObj;

		int numClusters;

		int clusterToFrame[];

		int clusterNumFrames[];

		private int defaultDepth;

		private int zOrder;

		private SquidConstructor constructor;

		private int defaultFrame;

		private int height;

		private int gumpFrame;

		private int footX, footY;

		private int hotFootX, hotFootY;

		private String typeName;

		private int typeNum;

		private int typeFlags;

		private String name = "Unknown";

		private String alignment = "Unknown";

		private int techBit = -1;

		private int level;

		private int cost;

		private int maxHitPoints;

		private int genus;

		private int group = NO_GROUP;

		private int loadOrder = -1;

		private NSTypeFrame[] frames;

		public static final int TYPE_FLAG_SHADOW = (0x00040000);

		public static final int TYPE_FLAG_FLYER_SHADOW = (0x00400000);

		public static final int TYPE_FLAG_HAS_HIT_POINTS = (0x00000010);

		public static final int TYPE_FLAG_DEFAULT_HOTSPOT = (0x00000001);

		public static final int TYPE_FLAG_MAY_DROP_ON_ISLE = (0x00000002);

		public static final int TYPE_FLAG_MAY_DROP_ON_RIM = (0x00000004);

		public static final int TYPE_FLAG_UNUSED = (0x00000008);

		public static final int TYPE_FLAG_SAVE_FRAME = (0x00000020);

		public static final int TYPE_FLAG_PROCESS = (0x00000080);

		public static final int TYPE_FLAG_HAS_ORIENTATION = (0x00000100);

		public static final int TYPE_FLAG_ORIENTATION_8 = (0x00000200);

		public static final int TYPE_FLAG_CREATE_ISLAND = (0x00000400);

		public static final int TYPE_FLAG_IS_SURFACE = (0x00000800);

		public static final int TYPE_FLAG_SAVE_Q8 = (0x00001000);

		public static final int TYPE_FLAG_SAVE_Q16 = (0x00002000);

		public static final int TYPE_FLAG_IS_GRAPH_USER = (0x00004000);

		public static final int TYPE_FLAG_TELEPORTABLE = (0x00008000);

		public static final int TYPE_FLAG_CARRIABLE_IN_VEHICLE = (0x00010000);

		public static final int TYPE_FLAG_CONTAINER = (0x00020000);

		public static final int TYPE_FLAG_IS_CAPTURABLE = (0x00080000);

		public static final int TYPE_FLAG_RAND_FRAME = (0x00100000);

		public static final int TYPE_FLAG_MATCH_FRAME = (0x00200000);

		public static final int TYPE_FLAG_OPAQUE_COLLIDE = (0x00800000);

		public static final int TYPE_FLAG_NO_PICKUP = (0x01000000);

		public static final int TYPE_FLAG_USES_MANA = (0x04000000);

		public static final int TYPE_NOT_SELECTABLE = (0x08000000);

		public static final int TYPE_FLAG_SPREADS_MANA = (0x10000000);

		public static final int TYPE_FLAG_DONT_SAVE = (0x20000000);

		public static final int gNONE = (0x00000000);

		public static final int gFENCEPOST = (0x00000001);

		public static final int gISLAND = (0x00000002);

		public static final int gBRIDGE = (0x00000004);

		public static final int gWALK_BLOCKING = (0x00000008);

		public static final int gDROP_BLOCKING = (0x00000010);

		public static final int gSHOT_BLOCKING = (0x00000020);

		// These two are an optimization for pathfinding it would be slightly less 
		// efficient if there were only one, but we can do it. It would be a lot
		// more inefficient if there weren't any. = (Halves the speed.);
		public static final int gPATH_OPEN = (0x00000040);

		public static final int gPATH_CLOSED = (0x00000080);

		public static final int gSPOT = (0x000000FF);

		public static final int gSTANDABLE = (gBRIDGE | gISLAND);

		public static final int gBOMB = (0x00000100);

		public static final int gVORTEX = (0x00000200);

		public static final int gGUY = (0x00000400);

		public static final int gBATTERY = (0x00000800);

		public static final int gFRINGE = (0x00001000);

		public static final int gBURIED = (0x00002000);

		public static final int gFACTORY = (0x00004000);

		public static final int gNUGGET = (0x00008000);

		public static final int gWALKER = (0x00010000);

		public static final int gBALLOON = (0x00020000);

		public static final int gEMPLACEMENT = (0x00040000);

		public static final int gEDGEFARM = (0x00080000);

		public static final int gFLYER = (0x00100000);

		public static final int gPRIEST = (0x00200000);

		public static final int gDAIS = (0x00400000);

		public static final int gISLAND3x3 = (0x01000000);

		public static final int gNOT_REAL = (0x02000000);

		public static final int gBLOCKER = (0x04000000);

		public static final int gSHOOTER = (0x08000000);

		public static final int gGEYSER = (0x10000000);

		public static final int gFENCE = (0x20000000);

		public static final int gRESIDENCE = (0x40000000);

		public static final int gALTAR = (0x80000000);

		public static final int gHAS_SPOT_FLAG = (0x000000FF);

		public static final int gCARRIER = (gWALKER | gBALLOON);

		public static final int gMINIMAP = (gBRIDGE | gISLAND /*| gVORTEX*/);

		public static final int gMINIMAPONTOP = (0/*gVORTEX*/);

		public static final int gFLYINGTHING = (gFLYER | gBALLOON);

		public static final int gMANA_PRODUCER = (gVORTEX | gBATTERY);

		public static final int gBUILDING = (gVORTEX | gFACTORY);

		public static final int gPRIEST_BUILT = (gVORTEX | gFACTORY | gDAIS);

		public static final int gTHINGS_BUILDINGS_KILL = (0); //gTREE);

		public static final int gTHINGS_VORTEX_KILLS = (0); //gBOMB|gWALKER|gEMPLACEMENT|gBURIED|gTREE);

		public static final int gHAS_PLAYER_ID = gVORTEX | gBOMB | gGUY | gBATTERY | gFACTORY | gNUGGET | gWALKER | gBALLOON | gEMPLACEMENT | gFLYER | gPRIEST | gDAIS | gISLAND3x3 | gBLOCKER | gSHOOTER | gGEYSER | gRESIDENCE;

		public static final int gALWAYS_INVALID_PLAYER_ID = gGEYSER | gBURIED;

		public static final int gHIDEABLE = (gEMPLACEMENT | gBATTERY | gVORTEX | gBALLOON | gFLYER | gFACTORY | gGEYSER | gDAIS | gALTAR);
	}

	private static NSType[] types;

	private static String groupNames[] =
	{ ("archer"), ("cannon"), ("blocker"), ("aviary"), ("flyer"), ("battery"), ("fence"), ("walker"), ("balloon"), ("misc"), ("anti-air") };

	public static final int grARCHER = 0;

	public static final int grCANNON = 1;

	public static final int grBLOCKER = 2;

	public static final int grAVIARY = 3;

	public static final int grNOT_USED = 4;

	public static final int grBATTERY = 5;

	public static final int grFENCE = 6;

	public static final int grWALKER = 7;

	public static final int grBALLOON = 8;

	public static final int grMISC = 9;

	public static final int grANTIAIR = 10;

	public static final int NO_GROUP = groupNames.length;

	public static final int FIRST_PROCESS_TYPE = 10;
}
