package org.sambro.botsy;

import org.javastorm.BoardCoord;
import org.javastorm.challenge.ZonePlayer;

public class IslandHump implements Runnable
{
	public void start(MasterBotsy bot, ZonePlayer centerPoint)
	{
		this.bot = bot;
		this.centerPoint = centerPoint;
		this.orbitPos = new BoardCoord(centerPoint.getPos());
		this.orbitPos.moveTo(this.centerPoint.getPos().getX() + 4, this.centerPoint.getPos().getY() - 1);
		this.bot.challengeServer.move(this.orbitPos);
		this.thread = new Thread(this, "Island Hump Thread");
		thread.start();
	}

	public void stop()
	{
		if (this.thread != null)
			thread.interrupt();
		thread = null;
	}

	public boolean isRunning()
	{
		return this.thread != null;
	}

	public long subscriberID()
	{
		// ID of person we're orbiting around.
		if (this.centerPoint != null)
		{
			return this.centerPoint.getSubscriberID();
		}
		return 0;
	}

	public void run()
	{
		// Wait an initial 10 seconds before orbiting otherwise it gets skewy.
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException ie)
		{
		}

		while (this.thread == Thread.currentThread())
		{
			if (this.up)
				this.orbitPos.moveTo(this.centerPoint.getPos().getX() + 1, this.centerPoint.getPos().getY() - 1);
			else
				this.orbitPos.moveTo(this.centerPoint.getPos().getX() + 2, this.centerPoint.getPos().getY() - 1);

			this.bot.challengeServer.move(this.orbitPos);
			this.up = !this.up;

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException ie)
			{
			}
		}
	}

	private Thread thread;

	private MasterBotsy bot;

	private boolean up;

	private ZonePlayer centerPoint;

	private BoardCoord orbitPos;
}
