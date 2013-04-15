import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BattleListeningThread extends ListeningThread
{
	public BattleListeningThread(int port)
	{
		super(port);
		System.out.println("BattleListeningThread starting on " + port);
	}

	public static byte[] hostIP = null;

	protected void handleConnection(Socket socket)
	{
		Socket client = null;

		try
		{
			System.out.println("New connection from: " + socket.getRemoteSocketAddress().toString());

			if (hostIP == null)
			{
				System.out.println("We don't have a host IP yet!");
				throw new IOException();
			}
			if (hostIP.length != 4)
			{
				System.out.println("Bad host IP!");
				throw new IOException();
			}

			client = new Socket();
			InetSocketAddress hostAddress = new InetSocketAddress(InetAddress.getByAddress(hostIP), 6798);
			System.out.println("Connecting " + socket.getRemoteSocketAddress().toString() + " to " + hostAddress.toString());
			client.connect(hostAddress, 3000);
		}
		catch (IOException ioe)
		{
			System.out.println("Error while connecting to destination.");
			ioe.printStackTrace();

			try
			{
				socket.close();
				client.close();
			}
			catch (Throwable t)
			{
			}

			return;
		}

		new WorkerThread(client, socket);
	}
}
