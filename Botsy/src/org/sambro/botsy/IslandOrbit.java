package org.sambro.botsy;

import org.javastorm.BoardCoord;
import org.javastorm.challenge.ZonePlayer;

public class IslandOrbit implements Runnable
{
	public void start(BaseBotsy bot, ZonePlayer centerPoint)
	{
		this.bot = bot;
		this.centerPoint = centerPoint;
		this.orbitPos = new BoardCoord(centerPoint.getPos());
		this.origOrbitPos = new BoardCoord(centerPoint.getPos());

		this.thread = new Thread(this, "Island Orbit Thread");
		this.thread.start();
	}

	public void stop()
	{
		if (this.thread != null)
			this.thread.interrupt();
		this.thread = null;
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
		this.bot.challengeServer.move(this.origOrbitPos);
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
			if (!this.origOrbitPos.equals(this.centerPoint.getPos()))
			{
				//this.bot.say("Damnit, " + this.centerPoint.getNickname() + ", gravity doesn't move!");
				this.thread = null;
				continue;
			}
			if (this.currentStage > 3)
				this.currentStage = 0;

			this.orbitPos.moveTo(this.origOrbitPos);

			// Top mid.
			if (this.currentStage == 0)
			{
				this.orbitPos.moveBy(0, -this.radius);
			}

			// mid right.
			if (this.currentStage == 1)
			{
				this.orbitPos.moveBy(+this.radius, 0);
			}

			if (this.currentStage == 2)
			{
				this.orbitPos.moveBy(0, +this.radius);
			}

			if (this.currentStage == 3)
			{
				this.orbitPos.moveBy(-this.radius, 0);
			}

			this.bot.challengeServer.move(this.orbitPos);
			this.currentStage++;

			try
			{
				Thread.sleep(1500);
			}
			catch (InterruptedException ie)
			{
			}
		}
	}

	private Thread thread;

	private BaseBotsy bot;

	private int currentStage = 0;

	private ZonePlayer centerPoint;

	private int radius = 3;

	private BoardCoord orbitPos;

	private BoardCoord origOrbitPos;
}
