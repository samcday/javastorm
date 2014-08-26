package org.javastorm;

import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.javastorm.types.Types;
import org.javastorm.types.OutpostType.NSOutpostConstructor;
import org.javastorm.types.RainVortexType.NSRainVortexConstructor;

public class Netstorm
{
	private static final long serialVersionUID = 1L;

	public static final void init()
	{
		File baseDir = null;

		baseDir = new File("D:\\NS");
		if (!baseDir.exists())
		{
			baseDir = new File("C:\\NS");
			if (!baseDir.exists())
			{
				baseDir = new File("C:\\Program Files\\NetstormLaunch\\package");
				if (!baseDir.exists())
				{
					baseDir = new File("D:\\Program Files\\NetstormLaunch\\package");
					if (!baseDir.exists())
					{
						baseDir = new File("D:\\Sammy\\Documents\\netstorm");
						if(!baseDir.exists())
						{
							JOptionPane.showMessageDialog(null, "Couldn't find NS directory!", "Error", JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
					}
				}
			}
		}

		fs = new Filesystem();
		if (!fs.init(baseDir))
		{
			JOptionPane.showMessageDialog(null, "Failed to init Filesystem.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!Types.loadTypelist(new InputStreamReader(Netstorm.class.getClassLoader().getResourceAsStream("typelist.txt"))))
		{
			JOptionPane.showMessageDialog(null, "Failed to load typelist.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!Types.load(fs))
		{
			JOptionPane.showMessageDialog(null, "Failed to load types.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!Types.loadNamevars(new InputStreamReader(Netstorm.class.getClassLoader().getResourceAsStream("_tg_namevars.cpp"))))
		{
			JOptionPane.showMessageDialog(null, "Failed to load typenums.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		initTypeConstructors();
	}

	private static final void initTypeConstructors()
	{
		//NSTypes.findByTypeName("isle").registerConstructor(new NSIslandTypeConstructor());
		Types.findByTypeName("rainVortex").registerConstructor(new NSRainVortexConstructor());
		Types.findByTypeName("outpost").registerConstructor(new NSOutpostConstructor());
	}
	
	public static final Filesystem getFileSystem()
	{
		return fs;
	}

	private static Filesystem fs;

	public static final int BOARDDIM_IN_TILES = 256;

	public static final int FIRST_REAL_PLAYER = 1;

	public static final int MAX_REAL_PLAYERS_PER_GAME = 8;

	public static final int END_REAL_PLAYERS = FIRST_REAL_PLAYER + MAX_REAL_PLAYERS_PER_GAME;

	public static final int INVALID_PLAYER_ID = 0;
	
	public static final int MAJOR_VERSION = 10;
}
