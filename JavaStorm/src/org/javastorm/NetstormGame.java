package org.javastorm;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.javastorm.battle.BattleOptions;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.fort.FortData;
import org.javastorm.renderer.NS2DRenderer;
import org.javastorm.renderer.Renderer;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidFinder;
import org.javastorm.territory.IslandBuilder;
import org.javastorm.territory.Territory;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;


public class NetstormGame extends Container implements Runnable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6429290316045859464L;
	
	public NetstormGame()
	{
		Netstorm.init();
		this.mousePos = new ScreenCoord();
		
		this.renderer = new NS2DRenderer();
		this.world = new World();
		this.world.setParent(this);
		this.events = new Vector<Event>();
	}

	public void addNotify()
	{
		super.addNotify();
		this.add(this.renderer);
		this.renderer.setLocation(0, 0);
		this.renderer.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
		this.renderer.setSize(800, 600);
		if (!this.renderer.init(Netstorm.getFileSystem(), this.world))
		{
			JOptionPane.showMessageDialog(null, "Failed to init renderer.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		this.world.setBattleOptions(new BattleOptions());

		BattlePlayer player = new BattlePlayer(new Version(10, 79));
		player.setPlayerIndex(1);
		player.setNickname("sambro.");
		player.getFortData().create("NONAME", true);
		
		FortData fd = new FortData(player);
		fd.open("botsy.fort");
		player.getFortData().receiveStrippedImage(fd.createStrippedImage());
		this.world.addPlayer(player);

		Territory thePrizeTerr = new Territory();
		thePrizeTerr.setCanon(1);
		thePrizeTerr.setExists(true);
		thePrizeTerr.setReserved(1);
		thePrizeTerr.setRandSeed(33);
		IslandBuilder ib = new IslandBuilder();
		ib.initAsBattleMode(this.world, player);
		ib.createBattleIsland();

		this.renderer.addKeyListener(new NSKeyListener());
		this.renderer.addMouseMotionListener(new NSMouseListener());
		this.addKeyListener(new NSKeyListener());
		NSMouseListener listener = new NSMouseListener();
		this.addMouseMotionListener(listener);
		this.addMouseListener(listener);
		this.renderer.addMouseListener(listener);

		NSType xbowType = Types.findByTypeName("windArcher");
		NSType outpostType = Types.findByTypeName("outpost");
		NSType rainVortexType = Types.findByTypeName("rainVortex");

		this.xbow = (MainSquid) this.world.take(outpostType.getTypeNum(), 15004);
		this.xbow.setPos(new BoardCoord(119, 128));
		this.xbow.setPlayer(null);
		this.xbow.pop();

		this.xbow = (MainSquid) this.world.take(xbowType.getTypeNum(), 15003);
		this.xbow.setPos(new BoardCoord(30, 30));
		this.xbow.pop();

		this.xbow = (MainSquid) this.world.take(Types.findByTypeName("priest").getTypeNum(), 15002);
		this.xbow.setPos(new BoardCoord(128, 116));
		this.xbow.setPlayer(null);
		this.xbow.setZOrder(rainVortexType.getZOrder() + 1);
		this.xbow.pop();

		this.xbow = (MainSquid) this.world.take(xbowType.getTypeNum(), 15000);
		this.xbow.setPos(new BoardCoord(100, 117));
		this.xbow.pop();
		this.xbow.dumpMyself();

		this.rainTemple = (MainSquid) this.world.take(rainVortexType.getTypeNum(), 15001);
		this.rainTemple.setPlayer(null);
		this.rainTemple.setPos(new BoardCoord(127, 118));
		this.rainTemple.pop();

		MainSquid s;
		for(int i = world.getFirstServerSid(); i < world.getFirstPredictable(); i++)
		{
			s = (MainSquid)world.getSquid(i);
			if(s == null) continue;
			
			if(!s.isFree() && !s.isForm() && !s.isSurface())
			{
				System.err.println(s.getTypeStruct().getTypeName());
			}
		}
		
		this.thread = new Thread(this);
		this.thread.start();
	}

	
	private void handleEvents()
	{
		synchronized (this.events)
		{
			ListIterator<Event> iter = this.events.listIterator();
			Event event;

			while (iter.hasNext())
			{
				event = iter.next();

				if (event.isMouse())
				{
					if (event.isLeftClick())
					{
						BoardCoord bc = this.renderer.convertScreenCoord(this.mousePos);
						MainSquid s = this.findTopSquid(bc);
						if (s != null)
							this.selectObject(s);

						this.renderer.redrawAll();
					}
				}
				if (event.isKey())
				{
					switch (event.getKey())
					{
						case KeyEvent.VK_DELETE:
						{
							if (this.currentSelection != null)
							{
								this.currentSelection.destroy(0);
								this.renderer.redrawAll();
							}
							break;
						}
						case KeyEvent.VK_ALT:
						{
							if (event.isUp())
								this.isScrolling = false;
							else
								this.isScrolling = true;

							break;
						}
						case KeyEvent.VK_S:
						{
							this.renderer.redrawAll();
							break;
						}
						case KeyEvent.VK_F2:
						{
							if (event.isUp())
								break;

							if (this.unitHideToggle)
								this.renderer.setMasterOpacity(255);
							else
								this.renderer.setMasterOpacity(110);

							this.renderer.redrawAll();

							this.unitHideToggle = !this.unitHideToggle;
							break;
						}
					}
				}
			}

			this.events.clear();
		}
	}
	
	public void run()
	{
		this.selectObject(this.rainTemple);
		this.renderer.getCamera().setX(90 * Renderer.X_TILE_DIM);
		this.renderer.getCamera().setY(90 * Renderer.Y_TILE_DIM);
		this.renderer.getCamera().setX(120 * Renderer.X_TILE_DIM);
		this.renderer.getCamera().setY(120 * Renderer.Y_TILE_DIM);
		this.renderer.redrawAll();

		//int dir = 0;
		//int step = 1;

		while (true)
		{
			this.world.doProcesses();
			this.handleEvents();

			/*
			if(dir == 0)
			{
				this.renderer.getCamera().move(step, 0);
				if(this.renderer.getCamera().getX() > 180 * NSRenderer.X_TILE_DIM)
					dir = 1;
			}
			if(dir == 1)
			{
				this.renderer.getCamera().move(0, step);
				if(this.renderer.getCamera().getY() > 180 * NSRenderer.Y_TILE_DIM)
					dir = 2;
			}
			if(dir == 2)
			{
				this.renderer.getCamera().move(-step, 0);
				if(this.renderer.getCamera().getX() < 70 * NSRenderer.X_TILE_DIM)
					dir = 3;
			}
			if(dir == 3)
			{
				this.renderer.getCamera().move(0, -step);
				if(this.renderer.getCamera().getY() < 70 * NSRenderer.Y_TILE_DIM)
					dir = 0;
			}*/

			if (this.isScrolling)
			{
				int offsetX = this.mousePos.getX() - this.getWidth() / 2;
				int offsetY = this.mousePos.getY() - this.getHeight() / 2;
				int xSpeed = (offsetX / 10);
				int ySpeed = (offsetY / 10);
				this.renderer.getCamera().move(xSpeed, ySpeed);
			}

			this.xbow.setFrame(this.xbow.getFrame() + 1);
			if (this.xbow.getFrame() == 4)
				this.xbow.setFrame(0);

			this.renderer.frame();

			this.repaint();

			this.getGraphics().setColor(Color.BLACK);
			this.getGraphics().fillRect(10, 10, 100, 100);
			/*try
			{
				Thread.sleep(1);
			}
			catch(InterruptedException ie) {}*/
		}
	}
	
	public MainSquid findTopSquid(BoardCoord bc)
	{
		SquidFinder sf = new SquidFinder(this.world, bc, 7, 7);
		while (sf.isValid())
		{
			MainSquid s = sf.s();
			if (!s.getTypeStruct().getTypeName().equalsIgnoreCase("isle"))
				if (!s.getTypeStruct().getTypeName().equalsIgnoreCase("fringe"))
				{
					if (s.getBoundingBox().intersects(bc))
						return s;
				}

			sf.findNext();
		}

		return null;
	}
	
	public void selectObject(MainSquid s)
	{
		if (this.currentSelection != null)
		{
			this.currentSelection.setSelected(false);
		}

		this.currentSelection = s;
		this.currentSelection.setSelected(true);
		NSUtil.dumpMyself(this.currentSelection);
	}

	public void dirtyMe(MainSquid squid)
	{
		Rect rect = this.renderer.getSquidArea(squid);
		this.renderer.redrawArea(rect);
	}
	
	private class NSKeyListener implements KeyListener
	{
		private boolean[] keys = new boolean[256];

		public void keyPressed(KeyEvent e)
		{
			if (keys[e.getKeyCode()])
				return;

			Event event = new Event();
			event.setKey(e.getKeyCode());
			event.setKeyDown(true);
			synchronized (events)
			{
				events.add(event);
			}
			keys[e.getKeyCode()] = true;
		}

		public void keyReleased(KeyEvent e)
		{
			Event event = new Event();
			event.setKey(e.getKeyCode());
			event.setKeyDown(false);
			synchronized (events)
			{
				events.add(event);
			}
			keys[e.getKeyCode()] = false;
		}

		public void keyTyped(KeyEvent e)
		{
		}
	}
	
	private class NSMouseListener extends MouseMotionAdapter implements MouseListener
	{
		public void mouseMoved(MouseEvent e)
		{
			mousePos.setX(e.getPoint().x);
			mousePos.setY(e.getPoint().y);
		}

		public void mousePressed(MouseEvent e)
		{
			Event event = new Event();
			event.setMouse();
			if (e.getButton() == MouseEvent.BUTTON1)
				event.setLeftClick();

			synchronized (events)
			{
				events.add(event);
			}
		}

		public void mouseReleased(MouseEvent e)
		{
		}

		public void mouseClicked(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}
	}
	


	private World world;
	private MainSquid currentSelection;
	private boolean unitHideToggle;
	private Vector<Event> events;
	private boolean isScrolling;
	private ScreenCoord mousePos;
	private MainSquid xbow, rainTemple;
	private Thread thread;
	private NS2DRenderer renderer;
}
