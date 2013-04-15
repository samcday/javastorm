package org.javastorm.tests;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;

import org.javastorm.NSFilesystem;
import org.javastorm.shapes.NSShapeFile;
import org.javastorm.shapes.NSShapeFile.VFXFrame;
import org.javastorm.shapes.NSShapeFile.VFXPalette;
import org.javastorm.types.NSTypes;
import org.javastorm.types.NSTypes.NSType;

public class ShadowTest extends Applet implements Runnable, ComponentListener
{

	public void init()
	{
		this.setSize(400, 400);

		NSFilesystem fs = new NSFilesystem();
		fs.init(new File("D:\\NS"));
		this.shapeFile = new NSShapeFile();
		this.shapeFile.open(fs.openBuffer("\\d\\_shapes.shp"));
		this.palette = this.shapeFile.registerPalette(NSShapeFile.loadPalette(fs.openBuffer("\\D\\gifcloud.col")));

		try
		{
			NSTypes.loadTypelist(new FileReader(new File("D:\\NS\\O\\typelist.txt")));
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(-1);
		}
		NSTypes.load(fs);

		this.backbuffer = new BufferedImage(400, 400, BufferedImage.TYPE_3BYTE_BGR);

		this.setIgnoreRepaint(true);
		new Thread(this).start();

		this.addComponentListener(this);
	}

	public void paint(Graphics g)
	{
		g.setColor(Color.PINK);
		g.fillRect(0, 0, 400, 400);
		//g.setXORMode(Color.BLACK);
		//fuck.setColor(new Color(255, 0, 0, 255));
		//fuck.fillRect(1, 1, 200, 200);
		g.drawImage(this.backbuffer, 0, 0, null);
	}

	public void run()
	{
		index = 0;
		int step = 1;
		long start, end;

		while (true)
		{
			backbuffer.getGraphics().setColor(new Color(255, 255, 255));
			backbuffer.getGraphics().fillRect(0, 0, this.getWidth(), this.getHeight());

			NSType type = NSTypes.findByTypeName("windFlyer");
			Graphics2D g = (Graphics2D) this.backbuffer.getGraphics();

			index += step;
			if (index > 253)
			{
				step = -2;
			}
			if (index < 2)
			{
				step = 2;
			}

			VFXFrame frame = this.shapeFile.getFrame(type.getLoadOrder(), 0);
			VFXFrame bg = this.shapeFile.getFrame(22, 0);
			VFXFrame shadow = this.shapeFile.getFrame(type.getLoadOrder(), 0 + type.getFrameCount());

			// Shadow.
			int shadowX = frame.getArtData().getHotX() - (shadow.getArtData().getHotX());
			int shadowY = frame.getArtData().getHotY() - (shadow.getArtData().getHotY());
			/*System.out.println(shadowX + "x" + shadowY);
			System.out.println("dim: " + frame.getArtData().getWidth() + "x" + frame.getArtData().getHeight());
			System.out.println("warm: " + frame.getArtData().getWarmX() + "x" + frame.getArtData().getWarmY());
			System.out.println("hot: " + frame.getArtData().getHotX() + "x" + frame.getArtData().getHotY());
			System.out.println("dim: " + shadow.getArtData().getWidth() + "x" + shadow.getArtData().getHeight());
			System.out.println("warm: " + shadow.getVfxData().getWarmX() + "x" + shadow.getVfxData().getWarmY());
			System.out.println("hot: " + shadow.getArtData().getHotX() + "x" + shadow.getArtData().getHotY());
			*/

			//index = 100;
			start = System.currentTimeMillis();
			{
				bg.drawFrame(g, this.palette, 0, 0);
				for (int i = 0; i < 50; i += 6)
				{
					shadow.drawFrame(g, this.palette, i + shadowX, i + shadowY, index);
					frame.drawFrame(g, this.palette, i, i, index);
				}
			}
			end = System.currentTimeMillis();
			System.out.println(end - start);

			this.getGraphics().drawImage(this.backbuffer, 0, 0, null);

			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException ie)
			{
			}
			this.repaint();
		}
	}

	private int index;

	private VFXPalette palette;

	private NSShapeFile shapeFile;

	private BufferedImage backbuffer;

	private static final long serialVersionUID = 1L;

	public void componentHidden(ComponentEvent arg0)
	{
	}

	public void componentMoved(ComponentEvent arg0)
	{
	}

	public void componentResized(ComponentEvent arg0)
	{
	}

	public void componentShown(ComponentEvent arg0)
	{
	}
}
