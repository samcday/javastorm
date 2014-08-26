package org.javastorm.network;

public class BandwidthMonitor
{
	public BandwidthMonitor()
	{
		this.updater = new UpdateThread();
	}

	public void start()
	{
		timeStart = System.currentTimeMillis();
		running = true;
		this.updater.start();
	}

	public boolean started()
	{
		return this.running;
	}

	public void end()
	{
		running = false;
		timeEnd = System.currentTimeMillis();
	}

	public void trafficIn(int bytes)
	{
		this.in_totalTraffic += bytes;
	}

	public void trafficOut(int bytes)
	{
		this.out_totalTraffic += bytes;
	}

	// This will fire every 1 second, updating data.
	private class UpdateThread implements Runnable
	{
		public void start()
		{
			new Thread(this, "Bandwidth Monitor Thread").start();
		}

		public void run()
		{
			long duration = 0;
			while (running)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException ie)
				{
				}

				// Update our timings.
				timeEnd = System.currentTimeMillis();

				// Now we calculate the current kb/s. This is based off how many bits we've received since last sample.
				// (1 second ago)
				in_bpsCurrent = (int) (in_totalTraffic - in_trafficLastSample) * 8;
				in_trafficLastSample = in_totalTraffic;

				// Now we calculate average bps. This is the amount on average we have utilized
				// over the duration of this monitor.
				duration = timeEnd - timeStart;
				if (duration == 0)
					duration = 1;
				in_bpsAverage = (int) ((float) in_totalTraffic / ((float) duration / (float) 1000)) * 8;

				if (in_bpsCurrent > in_bpsPeak)
					in_bpsPeak = in_bpsCurrent;

				// Now we calculate the current kb/s. This is based off how many bits we've received since last sample.
				// (1 second ago)
				out_bpsCurrent = (int) (out_totalTraffic - out_trafficLastSample) * 8;
				out_trafficLastSample = out_totalTraffic;

				// Now we calculate average bps. This is the amount on average we have utilized
				// over the duration of this monitor.
				duration = timeEnd - timeStart;
				if (duration == 0)
					duration = 1;
				out_bpsAverage = (int) ((float) out_totalTraffic / ((float) duration / (float) 1000)) * 8;

				if (in_bpsCurrent > out_bpsPeak)
					out_bpsPeak = out_bpsCurrent;
			}

			in_bpsCurrent = 0;
			out_bpsCurrent = 0;
		}
	}

	// Returns a formatted output of current bps.
	public String getBpsInCurrent()
	{
		return this.formatBitrate(this.in_bpsCurrent);
	}

	// Returns a formatted output of average bps.
	public String getBpsInAverage()
	{
		return this.formatBitrate(this.in_bpsAverage);
	}

	// Returns a formatted output of peak bps.
	public String getBpsInPeak()
	{
		return this.formatBitrate(this.in_bpsPeak);
	}

	// Returns the total traffic in a formatted String.
	public String getInTraffic()
	{
		return this.formatBytes(this.in_trafficLastSample);
	}

	// Returns a formatted output of current bps.
	public String getBpsOutCurrent()
	{
		return this.formatBitrate(this.out_bpsCurrent);
	}

	// Returns a formatted output of average bps.
	public String getBpsOutAverage()
	{
		return this.formatBitrate(this.out_bpsAverage);
	}

	// Returns a formatted output of peak bps.
	public String getBpsOutPeak()
	{
		return this.formatBitrate(this.out_bpsPeak);
	}

	// Returns the total traffic in a formatted String.
	public String getOutTraffic()
	{
		return this.formatBytes(this.out_trafficLastSample);
	}

	private String formatBitrate(long bps)
	{
		String formatter = "%.2f";
		String suffix = "bps";
		int divisor = 1;

		if (bps >= 1000)
		{
			suffix = "kbps";
			divisor = 1024;
		}
		else if (bps >= ((1000 * 1000)))
		{
			suffix = "mbps";
			divisor = 1000 * 1000;
		}
		else
			formatter = "%.0f";

		return String.format(formatter + "%s", (float) bps / (float) divisor, suffix);
	}

	private static final float marginPercent = .90f;

	private String formatBytes(long bytes)
	{
		String formatter = "%.2f";
		String suffix = "bytes";
		int divisor = 1;

		if (bytes >= (1024 * marginPercent))
		{
			suffix = "kb";
			divisor = 1024;
		}
		else if (bytes >= ((1024 * 1024) * marginPercent))
		{
			suffix = "mb";
			divisor = 1024 * 1024;
		}
		else
			formatter = "%.0f";

		return String.format(formatter + "%s", (float) bytes / (float) divisor, suffix);
	}

	private boolean running;

	private UpdateThread updater;

	private long timeStart, timeEnd;

	private int in_bpsCurrent; // Current bandwidth rate in bits.

	private int in_bpsAverage; // The average birate over the duration of this bandwidth monitor.

	private int in_bpsPeak; // The highest bps we hit at anytime.

	private long in_totalTraffic; // In bytes.

	private long in_trafficLastSample; // How much traffic was recorded last sample.

	private int out_bpsCurrent; // Current bandwidth rate in bits.

	private int out_bpsAverage; // The average birate over the duration of this bandwidth monitor.

	private int out_bpsPeak; // The highest bps we hit at anytime.

	private long out_totalTraffic; // In bytes.

	private long out_trafficLastSample; // How much traffic was recorded last sample.
}
