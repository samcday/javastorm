import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WorkerThread implements Runnable
{
	public WorkerThread(Socket server, Socket client)
	{
		this.client = client;
		this.server = server;

		new Thread(this).start();
	}

	public void run()
	{
		System.out.println("Worker thread spawned.");

		InputStream clientIn, serverIn;
		OutputStream clientOut, serverOut;

		try
		{
			clientIn = this.server.getInputStream();
			serverIn = this.client.getInputStream();

			clientOut = new BufferedOutputStream(this.server.getOutputStream());
			serverOut = new BufferedOutputStream(this.client.getOutputStream());

			while (true)
			{
				if (clientIn.available() > 0)
				{
					//System.out.print("Server: ");
					while (clientIn.available() > 0)
					{
						int val = clientIn.read();
						//System.out.print(val + " ");
						serverOut.write(val);
					}

					serverOut.flush();
					//System.out.println();
					//System.out.flush();
				}

				if (serverIn.available() > 0)
				{
					//System.out.print("Client: ");
					while (serverIn.available() > 0)
					{
						int val = serverIn.read();
						//System.out.print(val + " ");
						clientOut.write(val);
					}

					clientOut.flush();
					//System.out.println();
					//System.out.flush();
				}

				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
		catch (IOException ioe)
		{
		}

		try
		{
			this.client.close();
		}
		catch (IOException ioe)
		{
		}
		try
		{
			this.server.close();
		}
		catch (IOException ioe)
		{
		}

		System.out.println("Worker thread exited.");
	}

	private Socket server, client;
}
