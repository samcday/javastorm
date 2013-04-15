import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ListeningThread implements Runnable
{
	public ListeningThread(int port)
	{
		System.out.println("ListeningThread starting on " + port);

		this.port = port;
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void run()
	{
		try
		{
			this.server = new ServerSocket(this.port);
		}
		catch (IOException ioe)
		{
			System.out.println("Error creating server socket.");
			ioe.printStackTrace();
			System.exit(-1);
		}

		while (Thread.currentThread() == this.thread)
		{
			try
			{
				this.handleConnection(this.server.accept());
			}
			catch (IOException ioe)
			{
				System.out.println("Error while blocking for connection.");
				ioe.printStackTrace();
				System.exit(-1);
			}
		}
	}

	protected void handleConnection(Socket socket)
	{
		InputStream serverInput;
		OutputStream serverOutput;
		Socket client;

		int timeout;
		byte[] ip;
		int port;

		try
		{
			serverInput = socket.getInputStream();
			serverOutput = socket.getOutputStream();

			while (serverInput.available() < 12)
			{
			}
			ip = new byte[]
			{ (byte) serverInput.read(), (byte) serverInput.read(), (byte) serverInput.read(), (byte) serverInput.read() };
			port = new DataInputStream(serverInput).readInt();
			timeout = new DataInputStream(serverInput).readInt();
		}
		catch (IOException ioe)
		{
			System.out.println("Error while blocking for connection.");
			ioe.printStackTrace();
			return;
		}

		try
		{
			client = new Socket();
			client.connect(new InetSocketAddress(InetAddress.getByAddress(ip), port), timeout);

			System.out.println("Connecting " + socket.getRemoteSocketAddress().toString() + " to " + new InetSocketAddress(InetAddress.getByAddress(ip), port).toString());
			BattleListeningThread.hostIP = socket.getInetAddress().getAddress();

			serverOutput.write(1);
			serverOutput.flush();
		}
		catch (IOException ioe)
		{
			System.out.println("Error while connecting to destination.");
			ioe.printStackTrace();
			try
			{
				serverOutput.write(0);
				serverOutput.flush();
			}
			catch (IOException ioe2)
			{
			}
			return;
		}
		
		try
		{
			serverOutput.close();
		}
		catch(IOException ioe) {}
		
		try
		{
			serverInput.close();
		}
		catch(IOException ioe) {}
		
		new WorkerThread(client, socket);
	}

	private ServerSocket server;

	private Thread thread;

	private int port;
}
