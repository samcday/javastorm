package org.javastorm.tests;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileReader;

import org.javastorm.NSFilesystem;
import org.javastorm.shapes.NSShapeFile;
import org.javastorm.shapes.NSShapeFile.VFXFrame;
import org.javastorm.shapes.NSShapeFile.VFXPalette;
import org.javastorm.types.NSTypes;

public class SpamTest extends Applet implements Runnable
{
	public void init()
	{
		this.setSize(800, 600);

		NSFilesystem fs = new NSFilesystem();
		fs.init(new File("D:\\NS"));

		this.shapeFile = new NSShapeFile();
		this.palette = this.shapeFile.registerPalette(NSShapeFile.loadPalette(fs.openBuffer("\\d\\gifcloud.col")));
		this.shapeFile.open(fs.openBuffer("\\d\\_shapes.shp"));

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

		this.backbuffer = this.createVolatileImage(800, 600);

		this.setIgnoreRepaint(true);
	}

	public void start()
	{
		this.setSize(800, 600);
		new Thread(this).start();
	}

	public void paint(Graphics g)
	{
		g.drawImage(this.backbuffer, 0, 0, null);
	}

	public void update(Graphics g)
	{
	}

	public void run()
	{
		Graphics2D g = (Graphics2D) this.backbuffer.getGraphics();
		g.setColor(Color.WHITE);
		VFXFrame frame = this.shapeFile.getFrame(NSTypes.findByTypeName("isle").getLoadOrder(), 0);
		//VFXFrame frame = this.shapeFile.getFrame(NSTypes.findByTypeName("sunAviary").getLoadOrder(), 0);

		BufferedImage blah = new BufferedImage(frame.getVfxData().getWidth(), frame.getVfxData().getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		frame.drawFrame((Graphics2D) blah.getGraphics(), palette, frame.getVfxData().getWidth(), frame.getVfxData().getHeight());
		Graphics thisG = this.getGraphics();

		int width = frame.getVfxData().getImageWidth();
		int height = frame.getVfxData().getImageHeight();

		System.out.println(frame.getArtData().getFootX() + "," + frame.getArtData().getFootY());

		//g.setClip(40, 40, 140, 140);

		while (true)
		{
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			//frameNum++;
			//if(frameNum == 10) frameNum = 0;
			//frame = this.shapeFile.getFrame(2, frameNum);
			{
				for (int y = 0; y < this.getHeight(); y += height)
					for (int x = 0; x < this.getWidth(); x += width)
					{
						frame.drawFrame(g, this.palette, x, y, 120);
						//g.drawImage(blah, x, y, null);
					}
			}

			this.fpsCurrent++;

			if ((this.fpsTime + 1000000000) < System.nanoTime())
			{
				this.fpsLast = this.fpsCurrent;
				this.fpsCurrent = 0;
				this.fpsTime = System.nanoTime();
				System.out.println(this.fpsLast);
			}

			//g.setColor(new Color(255, 255, 255));
			//g.drawString(String.format("FPS: %d", this.fpsLast), 10, 10);

			thisG.drawImage(this.backbuffer, 0, 0, null);
			//this.repaint();

			/*try
			{
				Thread.sleep(1);
			} catch(InterruptedException ie) {}*/
		}
	}

	private int fpsCurrent;

	private int fpsLast;

	private long fpsTime = System.nanoTime();

	private VFXPalette palette;

	private NSShapeFile shapeFile;

	private VolatileImage backbuffer;

	private static final long serialVersionUID = 1L;
}