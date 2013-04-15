package org.javastorm.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.javastorm.BoardCoord;
import org.javastorm.BoardCoordRect;
import org.javastorm.Filesystem;
import org.javastorm.Rect;
import org.javastorm.ScreenCoord;
import org.javastorm.NSUtil;
import org.javastorm.World;
import org.javastorm.shapes.ShapeFile;
import org.javastorm.shapes.ShapeFile.ArtData;
import org.javastorm.shapes.ShapeFile.VFXFrame;
import org.javastorm.shapes.ShapeFile.VFXPalette;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidXYHash;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;

public class NS2DRenderer extends Renderer
{
	private static final long serialVersionUID = 1L;

	private void drawDebugBCLines()
	{/*
			Graphics g = this.getGraphics();
			g.setColor(Color.RED);
			
			int startX = (this.camera.getX() - this.getWidth() / 2) % NSRenderer.X_TILE_DIM;
			int startY = (this.camera.getY() - this.getHeight() / 2) % NSRenderer.Y_TILE_DIM;
			startX = NSRenderer.X_TILE_DIM - startX;
			startY = NSRenderer.Y_TILE_DIM - startY;

			for(int y = startY; y < this.getHeight(); y+=NSRenderer.Y_TILE_DIM)
				g.drawLine(0, y, this.getWidth(), y);
			for(int x = startX; x < this.getWidth(); x+=NSRenderer.X_TILE_DIM)
				g.drawLine(x, 0, x, this.getHeight());*/
	}

	private void renderCloudyBackground(ScreenCoord start)
	{
		int width = this.cloudBackground.getWidth(null);
		int height = this.cloudBackground.getHeight(null);
		int startX = start.getX() % width;
		int startY = start.getY() % height;

		for (int y = -startY; y < this.getHeight(); y += height)
			for (int x = -startX; x < this.getWidth(); x += width)
				this.backBufferGraphics.drawImage(this.cloudBackground, x, y, null);
	}

	public BoardCoord convertScreenCoord(ScreenCoord sc)
	{
		ScreenCoord screenStart = new ScreenCoord(this.camera.getX() - (this.getWidth() / 2), this.camera.getY() - (this.getHeight() / 2));

		sc.translate(screenStart.getX(), screenStart.getY());
		return NSUtil.screenToBoard(sc);
	}

	public void frame()
	{
		if (this.graphics == null)
			this.graphics = (Graphics2D) this.getGraphics();

		ScreenCoord start, end;
		ScreenCoord screenStart = new ScreenCoord(this.camera.getX() - (this.getWidth() / 2), this.camera.getY() - (this.getHeight() / 2));

		// Update FPS stuff.
		this.fpsCurrent++;
		if ((this.fpsTime + 1000000000) < System.nanoTime())
		{
			//this.fpsLast = this.fpsCurrent;
			this.fpsCurrent = 0;
			this.fpsTime = System.nanoTime();
			//System.out.println(this.fpsLast);
		}
		this.dirtyRegions.add(new Rect(0, 0, 30, 25));

		// Go through the dirty region list and redraw.
		ListIterator<Rect> dirtyRegionsIter = this.dirtyRegions.listIterator();

		while (dirtyRegionsIter.hasNext())
		{
			Rect region = dirtyRegionsIter.next();

			// Translate camera position to top left corner of viewport.			
			start = new ScreenCoord(screenStart.getX() + region.getL(), screenStart.getY() + region.getT());
			end = new ScreenCoord(start);
			end.translate(region.getWidth(), region.getHeight());

			// Ok I admit it, I like the Graphics object now =)
			this.backBufferGraphics.setClip(region.getL(), region.getT(), region.getWidth(), region.getHeight());

			// Redraw the background for the area we're redrawing.
			//this.backBufferGraphics.setColor(Color.BLACK);
			//this.backBufferGraphics.fillRect(region.getL(), region.getT(), region.getWidth(), region.getHeight());
			this.renderCloudyBackground(screenStart);

			SquidXYHash hash = this.world.getSquidHash();
			for (int i = 0; i < SquidXYHash.HASH_LEVEL_COUNT; i++)
			{
				hash.setLevel(i);
				BoardCoord tl = NSUtil.screenToBoard(start);
				BoardCoord br = NSUtil.screenToBoard(end);

				MainSquid s = null;
				MainSquid drawList[] = hash.get(tl, br);
				if (drawList != null)
				{
					for (int j = 0; j < drawList.length; j++)
					{
						s = drawList[j];
						ScreenCoord sc = NSUtil.boardToScreen(s.getPos());
						sc.translate(-screenStart.getX(), -screenStart.getY());

						VFXPalette pal = this.palette;
						if (this.typeColorTables[s.getTypeStruct().getTypeNum()] != null)
						{
							if(s.getPlayer() != null)
								this.palette = this.typeColorTables[s.getTypeStruct().getTypeNum()][s.getPlayer().getPlayerIndex()];
						}

						if (s.isOverloadDraw())
						{
							s.draw(this, sc.getX(), sc.getY());
						}
						else
						{
							this.draw(s, sc.getX(), sc.getY(), s.getFrame());
						}

						this.palette = pal;

						if (s.isSelected())
						{
							ArtData a = s.getCurrentFrame().getFrame().getArtData();
							int hotX = a.getHotX();
							int hotY = a.getHotY();
							int w = a.getWidth();
							int h = a.getHeight();

							//int footX = s.getTypeStruct().getFootpadX ();
							//int footY = s.getTypeStruct().getFootpadY ();

							int sx = (sc.getX() - hotX/*+3*/);//- ty->hotFootX/3;
							int ex = (sc.getX() - hotX + w);//- ty->hotFootX/3;
							int sy = sc.getY() - hotY + 1;//- ty->hotFootY;
							int ey = sc.getY() - hotY + h;//- ty->hotFootY;
							int xDist = (ex - sx) / 4;
							int yDist = (ey - sy) / 4;

							//extern int playerColorIndex[];
							//int color = playerColorIndex[colorRemap[playerId]];
							this.backBufferGraphics.setColor(Color.BLACK);
							this.backBufferGraphics.drawLine(sx, ey - yDist, sx, ey);
							this.backBufferGraphics.drawLine(sx, ey, sx + xDist, ey);
							this.backBufferGraphics.drawLine(ex, ey - yDist, ex, ey);
							this.backBufferGraphics.drawLine(ex - xDist, ey, ex, ey);
							++sx;
							++sy;
							--ex;
							--ey;
							this.backBufferGraphics.drawLine(sx, ey - yDist, sx, ey);
							this.backBufferGraphics.drawLine(sx, ey, sx + xDist, ey);
							this.backBufferGraphics.drawLine(ex, ey - yDist, ex, ey);
							this.backBufferGraphics.drawLine(ex - xDist, ey, ex, ey);

							if (s.hasHitPoints())
							{
								this.drawBar(Color.GREEN, s.getHitPoints(), s.getMaxHitPoints(), hotX, hotY, w, h, sc.getX(), sc.getY());
							}
						}
					}
				}
			}
		}
		
		// We can now copy the backbuffer to screen.
		this.backBufferGraphics.setClip(null);
		this.graphics.drawImage(this.backBuffer, 0, 0, null);

		this.graphics.drawImage(this.backBuffer, 0, 0, null);

		/*dirtyRegionsIter = this.dirtyRegions.listIterator();

		while(dirtyRegionsIter.hasNext())
		{
			NSRect region = dirtyRegionsIter.next();
			this.graphics.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
			this.graphics.drawRect(region.getL(), region.getT(), region.getWidth(), region.getHeight());
		}*/

		this.dirtyRegions.clear();

		if (false)
			this.drawDebugBCLines();
	}

	public void paint(Graphics g)
	{
		//super.paintComponent(g);
	}

	private void drawBar(Color color, int value, int max, int hotX, int hotY, int w, int h, int sc_x, int sc_y)
	{
		float ratio = (float) value / (float) max;
		int hp = (int) (w * ratio);
		int x = sc_x - hotX;
		int sy = (sc_y - hotY + 1 - 2);

		this.backBufferGraphics.setColor(Color.BLACK);
		this.backBufferGraphics.fillRect(x - 1, sy - 1, w + 2, NS2DRenderer.HP_BAR_HEIGHT + 2);
		this.backBufferGraphics.setColor(color);
		this.backBufferGraphics.fillRect(x, sy, hp, NS2DRenderer.HP_BAR_HEIGHT);
	}

	public void draw(MainSquid s, int x, int y, int frameNum)
	{
		int alpha = 255;
		if (this.masterOpacity > -1 && ((s.getGenus() & NSType.gHIDEABLE) > 0))
			alpha = this.masterOpacity;

		VFXFrame frame = this.shapeFile.getFrame(s.getTypeStruct().getLoadOrder(), frameNum);
		ScreenCoord sc = new ScreenCoord(x, y);
		sc.translate(-s.getTypeStruct().getHotFootX(), -s.getTypeStruct().getHotFootY());
		frame.drawFrame(this.backBufferGraphics, this.palette, sc.getX(), sc.getY(), alpha);

	}

	public void setMasterOpacity(int alpha)
	{
		this.masterOpacity = alpha;
	}

	public Rect getSquidArea(MainSquid squid)
	{
		ScreenCoord screenStart = new ScreenCoord(this.camera.getX() - (this.getWidth() / 2), this.camera.getY() - (this.getHeight() / 2));

		BoardCoordRect squidBB = squid.getBoundingBox();

		ScreenCoord squidScreenTl = NSUtil.boardToScreen(squidBB.getTl());
		ScreenCoord squidScreenBr = NSUtil.boardToScreen(squidBB.getBr());

		squidScreenBr.translate(-screenStart.getX(), -screenStart.getY());
		squidScreenTl.translate(-screenStart.getX(), -screenStart.getY());
		Rect squidRect = new Rect(squidScreenTl.getX(), squidScreenTl.getY(), squidScreenBr.getX(), squidScreenBr.getY());
		squidRect.clipTo(0, 0, this.getWidth(), this.getHeight());
		if (squidRect.isValid())
			return squidRect;
		else
			return null;
	}

	public void dirtyRegion(Rect dirty)
	{
		if (dirty == null)
			return;
		this.dirtyRegions.add(dirty);
	}

	public Camera getCamera()
	{
		return this.camera;
	}

	protected void scroll(int bx, int by)
	{
		int backbufferWidth = this.backBuffer.getWidth();
		int backbufferHeight = this.backBuffer.getHeight();

		bx = -bx;
		by = -by;

		// Ok unfortunately copyArea is fucking SLOW when scrolling left. What also sucks is the intermediate buffer copy method
		// takes 5 times longer than using copyArea.
		if (bx <= 0)
		{
			int copyRegionX, copyRegionY, copyRegionWidth, copyRegionHeight;
			if (bx < 0)
			{
				copyRegionX = Math.abs(bx);
				copyRegionWidth = backbufferWidth - copyRegionX;
			}
			else
			{
				copyRegionX = 0;
				copyRegionWidth = backbufferWidth - bx;
			}
			if (by < 0)
			{
				copyRegionY = Math.abs(by);
				copyRegionHeight = backbufferHeight - copyRegionY;
			}
			else
			{
				copyRegionY = 0;
				copyRegionHeight = backbufferHeight - by;
			}

			// Nudge the screen over a bit by using Graphics copyArea.
			this.backBufferGraphics.copyArea(copyRegionX, copyRegionY, copyRegionWidth, copyRegionHeight, bx, by);
		}
		else
		{
			this.intermediateGraphics.drawImage(this.backBuffer, bx, by, null);
			this.backBufferGraphics.drawImage(this.intermediate, 0, 0, null);
		}

		// Dirty the areas that just came into view on screen.
		Rect dirty;
		int startX, endX, startY, endY;

		startX = (backbufferWidth + bx);
		if (startX > backbufferWidth)
			startX = 0;
		endX = startX + Math.abs(bx);
		startY = 0;
		endY = backbufferHeight;
		dirty = new Rect(startX, startY, endX, endY);
		if (dirty.isValid())
			this.dirtyRegions.add(dirty);

		startX = 0;
		endX = backbufferWidth;
		startY = (backbufferHeight + by);
		if (startY > backbufferHeight)
			startY = 0;
		endY = startY + Math.abs(by);
		dirty = new Rect(startX, startY, endX, endY);
		if (dirty.isValid())
			this.dirtyRegions.add(dirty);
	}

	public void redrawAll()
	{
		this.dirtyRegions.add(new Rect(0, 0, this.getWidth(), this.getHeight()));
	}

	public void redrawArea(Rect rect)
	{
		if (rect != null)
			this.dirtyRegions.add(rect);
	}

	public boolean init(Filesystem fs, World world)
	{
		this.camera = new NS2DCamera(this);
		this.world = world;
		this.fs = fs;

		this.masterOpacity = -1;

		// Will only be a minor once off performance hit if we exceed this number.
		this.dirtyRegions = new Vector<Rect>(30);

		// Could put this in a utility method somewhere perhaps....
		this.shapeFile = new ShapeFile();
		if (!this.shapeFile.open(this.fs.openBuffer("\\D\\_shapes.shp")))
		{
			JOptionPane.showMessageDialog(null, "Failed to load shapefile:\n" + this.shapeFile.getLastError(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		this.palette = this.shapeFile.registerPalette(ShapeFile.loadPalette(this.fs.openBuffer("\\d\\gifcloud.col")));
		Types.loadShapeData(this.shapeFile);

		// This is the background, the famous gifcloud2!
		try
		{
			this.cloudBackground = ImageIO.read(this.fs.openStream("\\D\\gifcloud2.gif"));
		}
		catch (IOException ioe)
		{
			return false;
		}

		// We draw to this for render, then paint it onto ourselves (we're a Component)
		//this.backBuffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		this.backBuffer = this.getGraphicsConfiguration().createCompatibleImage(800, 600);
		this.intermediate = this.getGraphicsConfiguration().createCompatibleImage(800, 600);
		this.backBufferGraphics = this.backBuffer.createGraphics();
		this.intermediateGraphics = this.intermediate.createGraphics();
		this.backBufferGraphics.setColor(Color.WHITE);

		// We will handle repaints ourselves thanks.
		this.setIgnoreRepaint(true);

		this.initColorTables();

		return true;
	}

	// Palette stuff.
	private void initColorTables()
	{
		int[] masterPalette = this.palette.getPalette();

		this.h = new double[256];
		this.l = new double[256];
		this.s = new double[256];

		for (int i = 0; i < 256; i++)
		{
			double r = (double) ((masterPalette[i] >> 16) & 0xFF) / (double) 256;
			double g = (double) (masterPalette[i] >> 8 & 0xFF) / (double) 256;
			double b = (double) (masterPalette[i] & 0xFF) / (double) 256;

			double max = Math.max(Math.max(r, g), b);
			double min = Math.min(Math.min(r, g), b);

			double h = 0, l = 0, s = 0;

			l = (max + min) / 2.0;

			if (max == min)
			{
				s = 0;
				h = -1;
			}
			else
			{
				if (l <= 0.5)
				{
					s = (max - min) / (max + min);
				}
				else
				{
					s = (max - min) / (2 - max - min);
				}

				double delta = max - min;
				if (r == max)
				{
					h = (g - b) / delta;
				}
				else if (g == max)
				{
					h = 2 + (b - r) / delta;
				}
				else if (b == max)
				{
					h = 4 + (r - g) / delta;
				}
				h *= 60.0;

				if (h < 0.0)
				{
					h += 360.0;
				}
			}

			this.h[i] = h;
			this.l[i] = l;
			this.s[i] = s;
		}

		int[] BROWNS = new int[]
		{ 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 3, 177, 200, 203, 224, 220, 221, 222, 223, 114, 201, 226 };

		makeTypeColorTable(Types.findByTypeName("isle").getTypeNum(), 0.0f, BROWNS);

		makeTypeColorTableDirect(Types.findByTypeName("outpost").getTypeNum());
		makeTypeColorTableDirect(Types.findByTypeName("priest").getTypeNum());
	}

	private void makeTypeColorTable(int typeNum, double sourceLightnessAdjust, int[] colours)
	{
		int palette[][] = new int[9][256];
		int masterPalette[] = this.palette.getPalette();
		int isleType = Types.findByTypeName("isle").getTypeNum();
		this.typeColorTables[typeNum] = new VFXPalette[9];

		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 256; j++)
				palette[i][j] = masterPalette[j];
		this.typeColorTables[typeNum][0] = this.shapeFile.registerPalette(palette[0]);

		double lightAdjust[] =
		{ 0.0, -0.10, -0.17, 0.15, 0.0, 0.00, 0.05, -0.20, 0.0 };
		//                         blu,   red   whit   grn	pur  yel   Lblu   org
		//double lightAdjust[9] = { 0.0,  0.0,   0.0,  0.15, 0.0,  0.0,  0.1,  -0.2,  0.0 };
		for (int playerId = 1; playerId < 9; playerId++)
		{
			for (int i = 0; i < colours.length; i++)
			{
				int q = colours[i];
				if (typeNum == isleType)
				{
					double findLightness = this.l[q] + lightAdjust[playerId];
					double bestDistance = 99.0f;
					int bestIndex = 0;
					for (int k = 0; k < 8; k++)
					{
						double distance = Math.abs(findLightness - this.l[playerColorTable[playerId][k]]);
						if (distance < bestDistance)
						{
							bestIndex = k;
							bestDistance = distance;
						}
					}
					palette[playerId][q] = masterPalette[playerColorTable[playerId][bestIndex]];
				}
			}

			this.typeColorTables[typeNum][playerId] = this.shapeFile.registerPalette(palette[playerId]);
		}
	}

	private VFXPalette[] directPals;

	private void makeTypeColorTableDirect(int typeNum)
	{
		if (this.directPals == null)
		{
			VFXPalette pals[] = new VFXPalette[9];
			int palette[][] = new int[9][256];
			int masterPalette[] = this.palette.getPalette();

			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 256; j++)
					palette[i][j] = masterPalette[j];
			pals[0] = this.shapeFile.registerPalette(palette[0]);

			for (int playerId = 1; playerId < 9; ++playerId)
			{
				for (int i = 228; i <= 245; i++)
				{
					int bestIndex = 245 - i;

					if (i < 238)
					{
						bestIndex = 0;
					}
					palette[playerId][i] = masterPalette[playerColorTable[playerId][bestIndex]];
				}
				pals[playerId] = this.shapeFile.registerPalette(palette[playerId]);
			}
			this.directPals = pals;
		}

		this.typeColorTables[typeNum] = this.directPals;
	}

	private int masterOpacity;

	private Filesystem fs;

	private Vector<Rect> dirtyRegions;

	private int fpsCurrent;

	//private int fpsLast;
	private long fpsTime = System.nanoTime();

	private Graphics2D graphics;

	private VFXPalette palette;

	private double[] h, l, s;

	private ShapeFile shapeFile;

	private World world;

	private Camera camera;

	private BufferedImage backBuffer;

	private Graphics2D backBufferGraphics;

	private Image cloudBackground;

	private BufferedImage intermediate;

	private Graphics2D intermediateGraphics;

	private VFXPalette typeColorTables[][] = new VFXPalette[256][];

	private int playerColorTable[][] =
	{
	{ 0, 0, 0, 0, 0, 0, 0, 0 }, // NONE
	{ 81, 80, 76, 145, 146, 147, 4, 10 }, // BLUE
	{ 94, 100, 170, 169, 204, 249, 217, 227 }, // RED
	{ 195, 93, 185, 35, 31, 149, 70, 15 }, // WHITE
	{ 207, 131, 133, 128, 136, 2, 142, 143 }, // GREEN
	{ 189, 190, 253, 150, 148, 70, 14, 10 }, // PURPLE
	{ 113, 218, 219, 3, 52, 59, 67, 213 }, // BROWN/YELLOW
	{ 9, 85, 155, 156, 6, 72, 144, 143 }, // LIGHT BLUE
	{ 220, 221, 222, 223, 224, 63, 227, 213 } // ORANGE
	};

	private static final int HP_BAR_HEIGHT = 2;
}
