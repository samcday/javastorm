import org.javastorm.network.Connection;
import org.sambro.botsy.MasterBotsy;

public class Run
{
	public static void main(String args[])
	{
		MasterBotsy bot = new MasterBotsy();

		System.out.println("==========");
		System.out.println("NSBOT v1.0");
		System.out.println("==========");
		System.out.println("");

		/*System.out.print("Proxy: ");
		String proxy = "";

		try
		{
			proxy = stdin.readLine();
		}
		catch(Exception e)
		{
			System.out.println("IO Exception. Try again.");
		}

		if(proxy.replace('\n', ' ').trim().length() != 0)
		{
			NSNetwork.useProxy(proxy);
		}*/

		//Connection.useProxy("202.125.43.98");

		//NSChallengeServer zoneserver = new NSChallengeServer(6668);
		//zoneserver.start();

		//if(!bot.connect("localhost", 6668))
		if (!bot.connect("netstorm.game-host.org", 6800))
			System.out.println("Failed.");
		else
			System.out.println("Connected.");

		//bot.disconnect();
	}
}