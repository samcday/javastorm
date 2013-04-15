package org.javastorm;

/*
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import sambro.netstorm.shapes.NSShapeFile;
import sambro.netstorm.squids.NSMainSquid;
import sambro.netstorm.territory.NSIslandBuilder;
import sambro.netstorm.territory.NSTerritory;
import sambro.netstorm.types.NSTypes;
import sambro.netstorm.types.NSIsleType.NSIsleTypeConstructor;
import sambro.netstorm.types.NSOutpostType.NSOutpostConstructor;
import sambro.netstorm.types.NSRainVortexType.NSRainVortexConstructor;
import sambro.netstorm.types.NSTypes.NSType;

public class Netstorm3D extends SimpleGame {
	protected void simpleInitGame()
	{
		File baseDir = null;
		
		baseDir = new File("D:\\NS");
		if(!baseDir.exists())
		{
			baseDir = new File("C:\\NS");
			if(!baseDir.exists())
			{
				baseDir = new File("C:\\Program Files\\NetstormLaunch\\package");
				if(!baseDir.exists())
				{
					baseDir = new File("D:\\Program Files\\NetstormLaunch\\package");
					if(!baseDir.exists())
					{
						JOptionPane.showMessageDialog(null, "Couldn't find NS directory!", "Error", JOptionPane.ERROR_MESSAGE);
						System.exit(-1);
					}
				}
			}
		}
		
		this.fs = new NSFilesystem();
		if(!this.fs.init(baseDir))
		{
			JOptionPane.showMessageDialog(null, "Failed to init Filesystem.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if(!NSTypes.loadTypelist(new InputStreamReader(Netstorm.class.getClassLoader().getResourceAsStream("typelist.txt"))))
		{
			JOptionPane.showMessageDialog(null, "Failed to load typelist.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if(!NSTypes.load(this.fs))
		{
			JOptionPane.showMessageDialog(null, "Failed to load types.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if(!NSTypes.loadNamevars(new InputStreamReader(Netstorm.class.getClassLoader().getResourceAsStream("_tg_namevars.cpp"))))
		{
			JOptionPane.showMessageDialog(null, "Failed to load typenums.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		// Could put this in a utility method somewhere perhaps....
		NSShapeFile shapeFile = new NSShapeFile();
		if(!shapeFile.open(this.fs.openBuffer("\\D\\_shapes.shp")))
		{
			JOptionPane.showMessageDialog(null, "Failed to load shapefile:\n" + shapeFile.getLastError(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		//VFXPalette palette = shapeFile.registerPalette(NSShapeFile.loadPalette(this.fs.openBuffer("\\d\\gifcloud.col")));
		NSTypes.loadShapeData(shapeFile);

		this.initTypeConstructors();
		this.world = new NSWorld();
		//this.rootNode.attachChild(this.world);
		
		NSTerritory thePrizeTerr = new NSTerritory();
		thePrizeTerr.setCanon(2);
		thePrizeTerr.setReserved(1);
		thePrizeTerr.setRandSeed(33);
		NSIslandBuilder ib = new NSIslandBuilder();
		ib.initAsBattleMode(this.world, null);
		ib.createBountyIsland( thePrizeTerr );

		NSType xbowType = NSTypes.findByTypeName("windArcher");
		NSType outpostType = NSTypes.findByTypeName("outpost");
		NSType rainVortexType = NSTypes.findByTypeName("rainVortex");

		this.xbow = (NSMainSquid)this.world.take(outpostType.getTypeNum(), 15004);
		this.xbow.setPos(new NSBoardCoord(119, 128));
		this.xbow.setPlayer(null);
		this.xbow.pop();
		
		this.xbow = (NSMainSquid)this.world.take(xbowType.getTypeNum(), 15003);
		this.xbow.setPos(new NSBoardCoord(30, 30));
		this.xbow.pop();
		
		this.xbow = (NSMainSquid)this.world.take(NSTypes.findByTypeName("priest").getTypeNum(), 15002);
		this.xbow.setPos(new NSBoardCoord(128, 116));
		this.xbow.setPlayer(null);
		this.xbow.setZOrder(rainVortexType.getZOrder() + 1);
		this.xbow.pop();

		this.xbow = (NSMainSquid)this.world.take(xbowType.getTypeNum(), 15000);
		this.xbow.setPos(new NSBoardCoord(100, 117));
		this.xbow.pop();
		this.xbow.dumpMyself();

		this.rainTemple = (NSMainSquid)this.world.take(rainVortexType.getTypeNum(), 15001);
		this.rainTemple.setPlayer(null);
		this.rainTemple.setPos(new NSBoardCoord(127, 118));
		this.rainTemple.pop();

	    CullState cullState = this.display.getRenderer().createCullState();
	    cullState.setCullMode(CullState.CS_BACK);
	    cullState.setEnabled(true);
	    this.rootNode.setRenderState(cullState);
	    
		//this.thread = new Thread(this);
		//this.thread.start();
	}

	public static void main(String args[])
	{
		Netstorm3D app = new Netstorm3D();
	    app.setDialogBehaviour(Netstorm3D.ALWAYS_SHOW_PROPS_DIALOG);
	    app.start();
	}
	
	private void initTypeConstructors()
	{
		NSTypes.findByTypeName("isle").registerConstructor(new NSIsleTypeConstructor());
		NSTypes.findByTypeName("rainVortex").registerConstructor(new NSRainVortexConstructor());
		NSTypes.findByTypeName("outpost").registerConstructor(new NSOutpostConstructor());
	}

	private NSFilesystem fs;
	//private Vector<NSEvent> events;
	//private NSScreenCoord mousePos;
	private NSMainSquid xbow, rainTemple;
	//private Thread thread;
	private NSWorld world;
}*/
