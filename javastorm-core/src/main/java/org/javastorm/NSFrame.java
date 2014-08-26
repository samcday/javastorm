package org.javastorm;

import java.awt.Dimension;
import java.awt.Frame;

public class NSFrame extends Frame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NSFrame()
	{
		NetstormGame ns = new NetstormGame();
		this.add(ns);
		this.setSize(800, 600);
		this.setPreferredSize(new Dimension(800, 600));
		this.setVisible(true);

		while (true)
		{
			try
			{
				Thread.sleep(5000);
			}
			catch (Throwable t)
			{
			}
			break;
		}

		//System.exit(-1);
	}

	public static void main(String[] args)
	{
		new NSFrame();
	}
}
