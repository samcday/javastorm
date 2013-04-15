package org.javastorm.tests;

import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.javastorm.NSFilesystem;
import org.javastorm.NSVersion;
import org.javastorm.NSWorld;
import org.javastorm.Netstorm;
import org.javastorm.battle.NSBattleOptions;
import org.javastorm.battle.NSBattlePlayer;
import org.javastorm.fort.NSFortData;
import org.javastorm.territory.IslandBuilder;
import org.javastorm.types.NSTypes;

// Tests the fort data interpretor.
public class FortDataTester
{
	public static final void main(String[] args)
	{
		new FortDataTester();
	}
	
	public FortDataTester()
	{
		NSFilesystem fs = new NSFilesystem();
		if (!fs.init(new File("C:\\Documents and Settings\\Administrator\\My Documents\\programming\\NS")))
		//if(!fs.init(new File("C:\\NS")))
		//if(!fs.init(new File("C:\\Program Files\\NetstormLaunch\\")))
		{
			JOptionPane.showMessageDialog(null, "Failed to init fs.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!NSTypes.loadTypelist(new InputStreamReader(Netstorm.class.getClassLoader().getResourceAsStream("typelist.txt"))))
		{
			JOptionPane.showMessageDialog(null, "Failed to load typelist.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!NSTypes.load(fs))
		{
			JOptionPane.showMessageDialog(null, "Failed to load types.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!NSTypes.loadNamevars(new InputStreamReader(Netstorm.class.getClassLoader().getResourceAsStream("_tg_namevars.cpp"))))
		{
			JOptionPane.showMessageDialog(null, "Failed to load typenums.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		
		NSBattleOptions bo = new NSBattleOptions();

		NSBattlePlayer player = new NSBattlePlayer(new NSVersion(10, 78));
		player.setNickname("test");
		player.setPlayerIndex(1);
		player.setSubscriberID(666);


		NSWorld world = new NSWorld();
		world.setBattleOptions(bo);

		NSFortData fd = new NSFortData(player);
		fd.open("MyOnlineGame.fort");
		
		player.getFortData().create("NONAME", true);
		player.getFortData().receiveStrippedImage(fd.createStrippedImage());

		IslandBuilder ib = new IslandBuilder();
		ib.initAsBattleMode(world, player);
		ib.createBattleIsland();
	}
}
