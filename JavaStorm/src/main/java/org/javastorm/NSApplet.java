package org.javastorm;

import java.applet.Applet;

public class NSApplet extends Applet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init()
	{
		this.netstorm = new NetstormGame();
		this.setSize(800, 600);
		this.setLayout(null);
		this.netstorm.setLocation(0, 0);
		this.add(this.netstorm);
	}

	public void start()
	{
	}

	private NetstormGame netstorm;
}
