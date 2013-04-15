import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.javastorm.shapes.NSShapeFile;
import org.javastorm.shapes.NSShapeFile.VFXFrame;
import org.javastorm.shapes.NSShapeFile.VFXPalette;
import org.javastorm.util.MyDataInputStream;

public class ShapeBrowser extends JApplet
{
	private static final long serialVersionUID = 1L;

	public void init()
	{
		try
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					createGUI();
				}
			});

			// Try and scan for default NS directories.
			File file;
			file = new File("C:\\NS\\D");
			if (file.exists())
			{
				this.lastDir = file;
				return;
			}

			file = new File("D:\\NS\\D");
			if (file.exists())
			{
				this.lastDir = file;
				return;
			}

			file = new File("C:\\Program Files\\NetstormLaunch\\package\\d");
			if (file.exists())
			{
				this.lastDir = file;
				return;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void createGUI()
	{
		this.setSize(300, 300);
		this.menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem loadPalette = new JMenuItem("Load Palette");
		loadPalette.addActionListener(new LoadPalListener());
		fileMenu.add(loadPalette);
		this.loadShapeFile = new JMenuItem("Load Shapefile");
		loadShapeFile.addActionListener(new LoadShapeListener());
		this.loadShapeFile.setEnabled(false);
		fileMenu.add(this.loadShapeFile);

		this.menuBar.add(fileMenu);

		this.typeList = new JComboBox();

		this.getContentPane().add(this.typeList, BorderLayout.PAGE_START);

		this.frameList = new JSlider();
		this.getContentPane().add(this.frameList, BorderLayout.PAGE_END);

		this.frame = new ImageIcon();
		this.frameView = new JLabel();
		this.frameView.setVerticalTextPosition(JLabel.CENTER);
		this.frameView.setHorizontalTextPosition(JLabel.CENTER);
		this.frameView.setIcon(this.frame);
		this.getContentPane().add(this.frameView, BorderLayout.CENTER);

		frameList.setMajorTickSpacing(10);
		frameList.setMinorTickSpacing(1);
		frameList.setPaintTicks(true);
		frameList.setPaintLabels(true);
		frameList.setMaximum(0);
		this.typeList.addActionListener(new TypeListListener());
		this.frameList.addChangeListener(new FrameListListener());

		frameList.setEnabled(false);
		typeList.setEnabled(false);

		this.setJMenuBar(this.menuBar);

		this.setVisible(true);
	}

	private class TypeListListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (typeList.getSelectedIndex() == 0)
				return;
			if (typeList.getSelectedItem() == null)
				return;

			int type = (Integer) typeList.getSelectedItem();
			currentType = type;
			frameList.setEnabled(true);
			frameList.setMaximum(shape.getFrameCount(type) - 1);

			updateIcon(0);
		}
	}

	private class FrameListListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			updateIcon(frameList.getValue());
		}
	}

	private void updateIcon(int frame)
	{
		//System.out.println(this.currentType.getLoadOrder() + " " + frame);
		VFXFrame theFrame = this.shape.getFrame(this.currentType, frame);
		//VFXFrame theShadowFrame = null;
		//int width = theFrame.getFrame().getWidth(null);
		//int height = theFrame.getFrame().getHeight(null);

		/*
		// Render the shadow if it exists.
		if((this.currentType.getTypeFlags() & NSType.TYPE_FLAG_SHADOW|NSType.TYPE_FLAG_FLYER_SHADOW) != 0)
		{
			int shadowFrame = frame + this.currentType.getFrameCount();
			if(shadowFrame < this.shape.getFrameCount(this.currentType.getLoadOrder()))
			{
				theShadowFrame = this.shape.getFrame(this.currentType.getLoadOrder(), shadowFrame);
				width += theFrame.getHotX() + theShadowFrame.getWidth();
				height += theFrame.getHotY() + theShadowFrame.getHeight();
			}
		}*/

		/*BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = image.getGraphics();
		if(theShadowFrame != null)
			g.drawImage(theShadowFrame.getFrame(), theFrame.getWidth() + theShadowFrame.getWarmX() - 1, theFrame.getHeight() + theShadowFrame.getWarmY() - 2, null);
		g.drawImage(theFrame.getFrame(), 0, 0, null);*/

		BufferedImage frameData = new BufferedImage(theFrame.getArtData().getWidth(), theFrame.getArtData().getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		theFrame.drawFrame(frameData.createGraphics(), this.pal, 100, 100);
		this.frame.setImage(frameData);
		this.frameView.repaint();
	}

	private class LoadPalListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("NS Palette", "col");
			chooser.setFileFilter(filter);
			if (lastDir != null)
				chooser.setCurrentDirectory(lastDir);
			int returnVal = chooser.showOpenDialog(ShapeBrowser.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				loadPalette(chooser.getSelectedFile());
				lastDir = chooser.getCurrentDirectory();
				loadShapeFile.setEnabled(true);
			}
		}
	}

	private class LoadShapeListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("NS Shapefile", "shp");
			chooser.setFileFilter(filter);
			if (lastDir != null)
				chooser.setCurrentDirectory(lastDir);
			int returnVal = chooser.showOpenDialog(ShapeBrowser.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				loadSHP(chooser.getSelectedFile());
				lastDir = chooser.getCurrentDirectory();
			}
		}
	}

	private void loadPalette(File file)
	{
		try
		{
			MyDataInputStream in;
			try
			{
				in = new MyDataInputStream(new FileInputStream(file));
			}
			catch (FileNotFoundException fnfe)
			{
				throw new Error();
			}

			in.skip(8);

			this.colors = new int[256];
			for (int i = 0; i < 256; i++)
			{
				Color col = new Color(in.read(), in.read(), in.read());
				this.colors[i] = col.getRGB();
			}
		}
		catch (IOException ioe)
		{
			throw new Error();
		}
	}

	private void loadSHP(File file)
	{
		//NSTypes.init();
		//this.types = NSTypes.getAllTypes();

		try
		{
			this.shape = new NSShapeFile();
			FileInputStream fin = new FileInputStream(file);
			ByteBuffer buf = fin.getChannel().map(MapMode.READ_ONLY, 0, fin.getChannel().size());
			shape.open(buf);
			this.pal = this.shape.registerPalette(this.colors);

			this.typeList.removeAllItems();
			this.typeList.addItem("--Select--");
			for (int i = 0; i < this.shape.getTypeCount(); i++)
				this.typeList.addItem(i);

			this.typeList.setEnabled(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private VFXPalette pal;

	private File lastDir;

	private JMenuItem loadShapeFile;

	private JMenuBar menuBar;

	private int currentType;

	private NSShapeFile shape;

	private int colors[];

	private JComboBox typeList;

	private JSlider frameList;

	private ImageIcon frame;

	private JLabel frameView;
}
