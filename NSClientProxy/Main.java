public class Main
{
	public static void main(String[] args)
	{
		new Main();
	}

	public Main()
	{
		new BattleListeningThread(6799);
		new ListeningThread(6668);
	}
}