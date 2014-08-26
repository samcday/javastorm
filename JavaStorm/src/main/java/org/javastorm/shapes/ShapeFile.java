package org.javastorm.shapes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

public class ShapeFile
{
	public ShapeFile()
	{
		this.palettes = new VFXPalette[256];
	}

	// A quick Utility method to load a .col palette file.
	// Returns null if there was any problems.
	public static final int[] loadPalette(ByteBuffer buffer)
	{
		int[] palette = new int[256];

		buffer.position(8);
		for (int i = 0; i < 256; i++)
		{
			Color col = new Color(getUnsigned(buffer.get()), getUnsigned(buffer.get()), getUnsigned(buffer.get()), 1);
			palette[i] = col.getRGB();
		}

		return palette;
	}

	public VFXPalette registerPalette(int palette[])
	{
		int id = this.paletteCount++;
		VFXPalette pal = new VFXPalette();
		pal.palette = palette;
		pal.id = id;
		this.palettes[id] = pal;
		return pal;
	}

	private String lastError;

	public String getLastError()
	{
		return lastError;
	}

	// Open a shapefile, process all the type & frame data.
	public boolean open(ByteBuffer shapeData)
	{
		shapeData.order(ByteOrder.LITTLE_ENDIAN);

		long fileSize = shapeData.capacity();

		LinkedList<VFXType> headers = new LinkedList<VFXType>();

		// First, read the VFX headers.
		String typeMarker;
		int frameCount;
		int index = 0;
		VFXType header;
		int offsetOfThisType;

		while (true)
		{
			offsetOfThisType = shapeData.position();
			typeMarker = getStr(shapeData, 4);

			// We'll deduce what the vfxVer is based on the first one we read.
			// (which is the first 4 bytes of file)
			if (this.vfxVer == null)
				this.vfxVer = typeMarker;

			// If what we just read in isn't the start of type marker, then we're done reading headers.
			if (!typeMarker.equals(this.vfxVer))
			{
				break;
			}

			frameCount = shapeData.getInt();
			this.totalFrameCount += frameCount;

			header = new VFXType(frameCount);

			for (int i = 0; i < frameCount; i++)
			{
				// This tells us where the data is for this entry.
				// The offset is actually from the start of this type definition in file header.
				// We'll add that offset position to ensure that all pointers are absolute.
				// Then we can subtract base offset of data and make easy jumps around the memory-mapped
				// data segment.
				header.frames[i].offset = shapeData.getInt() + offsetOfThisType - 36;

				// Next 4 bytes are for .... meaningless padding? Always 0.
				shapeData.getInt();
			}

			headers.add(header);
			index++;
		}

		this.types = headers.toArray(new VFXType[0]);

		// We're going to map the data portion of the shapebuffer into memory, for quick access
		// we'll fix up the offsets to start at end of header block (start of data block)
		this.baseOffset = (this.totalFrameCount * 8) + (this.types.length * 8);

		for (int i = 0; i < this.types.length; i++)
			for (int j = 0; j < this.types[i].frames.length; j++)
			{
				this.types[i].frames[j].offset -= baseOffset;
			}

		shapeData.position(baseOffset);
		this.shapeBuffer = new byte[(int) (fileSize - baseOffset)];
		for (int i = 0; i < this.shapeBuffer.length; i++)
			this.shapeBuffer[i] = shapeData.get();

		// Quickly scan through the files, and add populate all the art data information.
		int typeCount = this.types.length;
		VFXType types[] = this.types;

		for (int i = 0; i < typeCount; i++)
		{
			frameCount = types[i].frames.length;
			for (int j = 0; j < frameCount; j++)
			{
				shapeData.position(baseOffset + (int) types[i].frames[j].offset);

				ArtData artData = new ArtData();
				artData.footX = shapeData.getFloat();
				artData.footY = shapeData.getFloat();
				shapeData.getFloat();
				shapeData.getFloat();
				shapeData.getFloat();
				shapeData.getFloat();

				artData.width = shapeData.getShort();
				artData.height = shapeData.getShort();
				artData.hotX = shapeData.getShort();
				artData.hotY = shapeData.getShort();
				artData.warmX = shapeData.getShort();
				artData.warmY = shapeData.getShort();

				VFXData vfxData = new VFXData();
				// Width and height of frame.
				vfxData.height = shapeData.getShort() + 1;
				vfxData.width = shapeData.getShort() + 1;
				// Hot foots.
				vfxData.hotY = shapeData.getShort();
				vfxData.hotX = shapeData.getShort();
				// Warm is offset from the hot coords.
				vfxData.warmX = shapeData.getInt();
				vfxData.warmY = shapeData.getInt();
				// Signifies where the image ends.
				vfxData.right = shapeData.getInt();
				vfxData.bottom = shapeData.getInt();

				// We have a starting point and an ending point. We can gather these based on the hots/warms.
				vfxData.startX = vfxData.hotX + vfxData.warmX;
				vfxData.startY = vfxData.hotY + vfxData.warmY;
				vfxData.endX = vfxData.hotX + vfxData.right;
				vfxData.endY = vfxData.hotY + vfxData.bottom;

				// Because a lot of frames have a good chunk of transparent crap around them, we'll only make the image
				// as large as the frame itself. For example if there's a 10x10 shadow floating inside a 200x100
				// frame, we just make the Image 10x10. Rendering system will obviously need to offset by hot co-ords.
				vfxData.imageWidth = vfxData.endX - vfxData.startX + 1;
				vfxData.imageHeight = vfxData.endY - vfxData.startY + 1;

				// There's a bizarre bug in the SHP, a few frames have screwed up warm spots. I think
				// this actually means there's no data inside the frame (place-holder with warm spots for e.g)
				// We will never be asked to render these frames anyway, they're only there for referencing
				// the ArtData warm/hot spots.
				if ((vfxData.startX > vfxData.width || vfxData.startY > vfxData.height))
				{
					vfxData.width = -1;
					vfxData.height = -1;
				}

				types[i].frames[j].artData = artData;
				types[i].frames[j].vfxData = vfxData;
			}
		}

		return true;
	}

	public int getTypeCount()
	{
		return this.types.length;
	}

	public int getFrameCount(int type)
	{
		return this.types[type].frames.length;
	}

	public VFXFrame getFrame(int type, int frame)
	{
		if (type >= this.types.length)
			return null;
		if (frame >= this.types[type].frameCount)
			return null;

		return this.types[type].frames[frame];
	}

	private class VFXType
	{
		public VFXType(int frameCount)
		{
			this.frameCount = frameCount;
			this.frames = new VFXFrame[frameCount];
			for (int i = 0; i < this.frames.length; i++)
				this.frames[i] = new VFXFrame();
		}

		int frameCount; // How many frames in this type?

		VFXFrame[] frames;
	}

	public class VFXFrame
	{
		// Another desperate attempt to squeeze as much performance as possible.
		// Apparently referencing between nested classes is quite expensive.
		// So we'll store local references here.
		private byte[] localShapeBuffer;

		private BufferedImage cache[] = new BufferedImage[256];

		private VFXCommand commands;

		private void makeVFXCommands(byte[] shapeData)
		{
			int index = (int) this.offset + 60;

			VFXCommand prev = null;
			VFXCommand current;

			int currentY, commandByte, mode, repetitions, colourRun, colourRepeat, colour, colourCount;

			// To save time, we just render the area that actually has pixels, then draw it offset from hotspot.
			currentY = vfxData.startY;

			while (true)
			{
				current = new VFXCommand();
				commandByte = getUnsigned(shapeBuffer[index++]);

				// Is this a repeat transparent block?
				if (commandByte == 1)
				{
					current.commandID = 0;
					current.count = getUnsigned(shapeBuffer[index++]);
					//currentX += getUnsigned(_shapeBuffer[index++]);
				}
				// Is this a end of line byte?
				else if (commandByte == 0)
				{
					current.commandID = 1;
					currentY++;

					if (currentY > vfxData.endY)
					{
						prev.next = current;
						break;
					}
				}
				// This is a color byte. There are two variations. If the LSB is 0, we repeat the following byte
				// however many times. If the LSB is 1, a list of colour indexes will follow this byte.
				else
				{
					mode = commandByte & 1;

					if (mode == 0)
					{
						current.commandID = 2;
						// The amount of repetitions is encoded in the commandByte, it's just shifted left one.
						repetitions = commandByte >> 1;
						current.count = repetitions;

						colour = getUnsigned(shapeBuffer[index++]);
						current.colours = new int[repetitions];

						for (colourRepeat = 0; colourRepeat < repetitions; colourRepeat++)
						{
							current.colours[colourRepeat] = colour;
						}
					}
					else
					{
						current.commandID = 2;
						// The amount of individual pixels we're going to write is encoded the same way as above.
						colourCount = commandByte >> 1;

						current.count = colourCount;
						current.colours = new int[current.count];

						for (colourRun = 0; colourRun < colourCount; colourRun++)
						{
							current.colours[colourRun] = getUnsigned(shapeBuffer[index++]);
						}
					}
				}
				if (this.commands == null)
				{
					this.commands = current;
					prev = current;
				}
				else
				{
					prev.next = current;
					prev = current;
				}
			}
		}

		// Draws a frame at the specified location.
		public void drawFrame(Graphics2D g, VFXPalette palette, int x, int y)
		{
			drawFrame(g, palette, x, y, 255);
		}

		public void drawFrame(Graphics2D g, VFXPalette _palette, int x, int y, int alpha)
		{
			int palette[] = _palette.palette;

			if (this.localShapeBuffer == null)
			{
				this.localShapeBuffer = shapeBuffer;
			}

			byte[] _shapeBuffer = this.localShapeBuffer;

			if (this.commands == null)
				this.makeVFXCommands(_shapeBuffer);

			if (this.vfxData.width == -1)
				return;

			int currentX, currentY, colourRun;

			// To save time, we just render the area that actually has pixels, then draw it offset from hotspot.
			//vfxData.startX = vfxData.startY = 0;
			currentX = vfxData.startX;
			currentY = vfxData.startY;

			int scanline = currentY * vfxData.width;

			if (this.cache[_palette.id] == null)
			{
				int cacheScratch[] = new int[this.vfxData.width * this.vfxData.height];
				this.cache[_palette.id] = createImage(vfxData.width, vfxData.height, cacheScratch);

				VFXCommand curr = this.commands;
				while (curr != null)
				{
					switch (curr.commandID)
					{
						case 0:
						{
							currentX += curr.count;
							break;
						}
						case 1:
						{
							currentX = vfxData.startX;
							currentY++;
							scanline = currentY * vfxData.width;
							break;
						}
						case 2:
						{
							//System.arraycopy(curr.colours, 0, _scratchBuffer, scanline + currentX, curr.count);
							//currentX += curr.count;

							for (colourRun = 0; colourRun < curr.count; colourRun++)
							{
								cacheScratch[scanline + currentX++] = palette[curr.colours[colourRun]];
							}
							break;
						}
					}

					curr = curr.next;
				}
			}

			// Plonk the frame where it belongs.

			// If we're drawing alpha blended, we just use a Graphics2D composite, this is gonna have to be faster than redrawing
			// image with an AlphaChannel, cmon =\
			if (alpha < 255)
			{
				Composite oldComposite = g.getComposite();
				g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
				g.drawImage(this.cache[_palette.id], x - vfxData.hotX, y - vfxData.hotY, null);
				g.setComposite(oldComposite);
			}
			else
				g.drawImage(this.cache[_palette.id], x - vfxData.hotX, y - vfxData.hotY, null);
		}

		public VFXData getVfxData()
		{
			return vfxData;
		}

		// Returns the art data for this frame.
		public ArtData getArtData()
		{
			return this.artData;
		}

		private long offset; // Where is the art data for this frame in shapefile?

		// This is data inside the ArtData block.
		private ArtData artData;

		// This is data inside the VFX headers.
		private VFXData vfxData;
	}

	// Creates an image with 1bit transparency that will use the given int array directly.
	private BufferedImage createImage(int width, int height, int[] data)
	{
		int redMask = 0x00FF0000;
		int greenMask = 0x0000FF00;
		int blueMask = 0x000000FF;
		int alphaMask = 0x01000000;

		ColorModel colorModel;
		SampleModel sampleModel;
		DataBuffer dataBuffer;
		WritableRaster raster;

		// Credit goes to keldon85 on javagaming.org for this code, it's genius.

		colorModel = new DirectColorModel(25, redMask, greenMask, blueMask, alphaMask);

		sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, new int[]
		{ redMask, greenMask, blueMask, alphaMask });

		dataBuffer = new DataBufferInt(data, width * height);

		raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
		// Note how we set alphaPremulitplied to true. This means when we go to draw the colours won't be touched
		// at all, just directly copied. We don't need premultiplied alpha because our transparency is a simple
		// toggle bit that we've already set.
		return new BufferedImage(colorModel, raster, true, null);
	}

	// Converts a signed byte into an unsigned.
	// Hopefully the JVM will inline this.
	private static final int getUnsigned(byte b)
	{
		return (b < 0) ? b + 256 : b;
	}

	private static final String getStr(ByteBuffer buffer, int len)
	{
		char chars[] = new char[len];
		for (int i = 0; i < len; i++)
			chars[i] = (char) buffer.get();
		return new String(chars);
	}

	public static final class ArtData
	{
		public float getFootX()
		{
			return footX;
		}

		public void setFootX(float footX)
		{
			this.footX = footX;
		}

		public float getFootY()
		{
			return footY;
		}

		public void setFootY(float footY)
		{
			this.footY = footY;
		}

		public int getHeight()
		{
			return height;
		}

		public int getHotX()
		{
			return hotX;
		}

		public int getHotY()
		{
			return hotY;
		}

		public int getWarmX()
		{
			return warmX;
		}

		public int getWarmY()
		{
			return warmY;
		}

		public int getWidth()
		{
			return width;
		}

		private float footX, footY;

		private int width, height;

		private int warmX, warmY;

		private int hotX, hotY;
	}

	public static final class VFXData
	{
		public int getBottom()
		{
			return bottom;
		}

		public int getEndX()
		{
			return endX;
		}

		public int getEndY()
		{
			return endY;
		}

		public int getHeight()
		{
			return height;
		}

		public int getHotX()
		{
			return hotX;
		}

		public int getHotY()
		{
			return hotY;
		}

		public int getImageHeight()
		{
			return imageHeight;
		}

		public int getImageWidth()
		{
			return imageWidth;
		}

		public int getRight()
		{
			return right;
		}

		public int getStartX()
		{
			return startX;
		}

		public int getStartY()
		{
			return startY;
		}

		public int getWarmX()
		{
			return warmX;
		}

		public int getWarmY()
		{
			return warmY;
		}

		public int getWidth()
		{
			return width;
		}

		private int width, height;

		private int hotX, hotY;

		private int warmX, warmY;

		private int right, bottom;

		private int imageWidth, imageHeight;

		private int startX, startY;

		private int endX, endY;
	}

	private static final class VFXCommand
	{
		int commandID;

		int[] colours;

		int count;

		VFXCommand next;
	}

	public static final class VFXPalette
	{
		private VFXPalette()
		{
		}

		public int[] getPalette()
		{
			return palette;
		}

		private int id;

		private int[] palette;
	}

	private VFXPalette palettes[];

	private int paletteCount;

	private int baseOffset;

	private byte[] shapeBuffer;

	private String vfxVer;

	private int totalFrameCount;

	private VFXType[] types;
}
