package org.sambro.botsy;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;

import org.javastorm.Player;

//Scans chat for commands. Processes the arguments and calls callbacks.
public class CommandParser
{
	// Some internal commands.
	private class CommandCountCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean hostWhisper, Object... args)
		{
			host.hostSay("There are " + commands.size() + " registered commands in my system.");
		}
	}

	private class CommandHelpCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean hostWhisper, Object... args)
		{
			host.hostPopupTo("~E~u<h2>Command Help</h2>" + "~E~wCommands are how you tell Botsy to do things! It's really simple. Commands are in the format of " + formatCommand("commandName") + ". For example: " + formatCommand("country") + " is calling the 'country' command which will tell you the country you live in.<p>" + "~E~wFor a complete list of commands, you can type " + formatCommand("commandlist") + ".<p>" + "~E~wSometimes commands require further input from you. You can provide this specific input in the form of a 'parameter'. To specify a parameter to a command, you place it inside brackets [ ]. E.g " + formatInvocation("commandlist", "Fun") + "<p>" + "~E~wFor commands that allow an input of a form of message, you must enclose the argument in quotes \" \". e.g. " + formatInvocation("echo", "\"This is a message, with commas, yay.\"") + ", try that command without the quotes and see what happens.", player);
		}
	}

	private class EchoCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean hostWhisper, Object... args)
		{
			host.hostSay(player.getNickname() + " says " + (String) args[0]);
		}
	}

	private class DescribeCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean hostWhisper, Object... args)
		{
			StringBuilder describe = new StringBuilder();

			ListIterator<ChatCommand> iter = commands.listIterator();
			ChatCommand iterCmd;
			while (iter.hasNext())
			{
				iterCmd = iter.next();

				if (iterCmd.getCommandName().equalsIgnoreCase((String) args[0]))
				{
					describe.append("~E~B~u<h2>").append(iterCmd.getCommandName()).append(" ");
					if (iterCmd.getArgCount() > 0)
					{
						describe.append("«");
						ChatCommandArgument[] argList = iterCmd.getArgs();
						for (int i = 0; i < iterCmd.getArgCount(); i++)
						{
							describe.append(argList[i].getName());
							if (i != (iterCmd.getArgCount() - 1))
								describe.append(", ");
						}
						describe.append("»");
					}

					describe.append("</h2>~E~iAdmin only: ~w");
					if (iterCmd.isAdminOnly())
						describe.append("Yes");
					else
						describe.append("No");
					describe.append("<br>");

					describe.append("~E~iWhisper only: ~w");
					if (iterCmd.isWhisperOnly())
						describe.append("Yes");
					else
						describe.append("No");

					describe.append("<p>");

					describe.append("~E~B~w").append(iterCmd.getDescription());

					host.hostPopupTo(describe.toString(), player);

					return;
				}
			}
		}

	}

	private class CommandListCommand implements ChatCommandCallback
	{
		public void execute(Player player, boolean hostWhisper, Object... args)
		{
			if (args == null)
			{
				// Sort the command group list.
				String groups[] = groupList.toArray(new String[0]);
				StringBuilder groupStr = new StringBuilder();
				groupStr.append("~E~B~iGroups: ~w");
				Arrays.sort(groups);

				for (int i = 0; i < groups.length; i++)
				{
					groupStr.append(groups[i]);
					if (i < (groups.length - 1))
						groupStr.append(", ");
				}

				host.hostPopupTo("~E~u<h2>Command List</h2>~E~B~wCommands are broken up into different groups. Type " + formatInvocation("commandList", "Name of Group") + " for information on each group. e.g. " + formatInvocation("commandlist", "Fun") + "<p>" + groupStr.toString(), player);
				return;
			}

			// Might want to optimize this later, maybe maintain the lists as the commands are registered.
			String group = (String) args[0];
			Boolean listAll = false;
			//if(group.equalsIgnoreCase("all")) listAll = true;

			StringBuilder groupCommands = new StringBuilder();
			ListIterator<ChatCommand> iter = commands.listIterator();
			ChatCommand iterCmd;
			while (iter.hasNext())
			{
				iterCmd = iter.next();

				if (iterCmd.getGroup().equalsIgnoreCase(group) || listAll)
				{
					groupCommands.append("~E~i");
					groupCommands.append(iterCmd.getCommandName());
					groupCommands.append(" - ~w");
					groupCommands.append(iterCmd.getDescription().replace("<", "«").replace(">", "»"));
					groupCommands.append("<br>");
				}
			}
			if (groupCommands.length() > 0)
				host.hostPopupTo("~E~u<h2>Command List for " + group + "</h2>~E~B~wFor more information on these commands, type " + formatInvocation("describe", "Name of Command") + "~w<p>" + groupCommands.toString(), player);
		}
	}

	public String formatCommand(String command)
	{
		return String.format("~y/%s~w", command);
	}

	public String formatInvocation(String command, String args)
	{
		return String.format("~l/%s(%s)~w", command, args);
	}

	public CommandParser(CommandParserHost host)
	{
		this.host = host;
		this.commands = new Vector<ChatCommand>();
		this.groupList = new Vector<String>();
	}

	public void registerDefaultCommands()
	{
		// Register our internal commands here.
		this.registerCommand(new ChatCommand("commandlist", "Generates a list of all commands Botsy recognizes.", "Help", new CommandListCommand(), new ChatCommandArgument[]
		{ new ChatCommandArgument("Name of Group", ChatCommandArgument.ARG_STRING, false) }, false, false));

		this.registerCommand(new ChatCommand("commandhelp", "Basic info about commands.", "Help", new CommandHelpCommand(), new ChatCommandArgument[] {}, false, false));

		this.registerCommand(new ChatCommand("describe", "Gives detailed information about a specific command.", "Help", new DescribeCommand(), new ChatCommandArgument[]
		{ new ChatCommandArgument("Name of Command", ChatCommandArgument.ARG_STRING, true) }, false, false));

		this.registerCommand(new ChatCommand("commandcount", "Total number of commands in system.", "Help", new CommandCountCommand(), new ChatCommandArgument[] {}, false, false));

		this.registerCommand(new ChatCommand("echo", "Hollaback!", "Utility", new EchoCommand(), new ChatCommandArgument[]
		{ new ChatCommandArgument("Message", ChatCommandArgument.ARG_STRING, true) }, false, false));
	}

	public void registerCommand(ChatCommand command)
	{

		this.commands.add(command);

		// If this is the first time we've seen this commans group, add it to list.
		if (!this.groupList.contains(command.getGroup().intern()))
			this.groupList.add(command.getGroup().intern());
	}

	public void processChatline(Player player, String chatline)
	{
		char line[] = chatline.toCharArray();
		int length = chatline.length(), startArgs = 0, endArgs = 0, currentIndex = 0, slashLoc;
		String commandStr;
		ChatCommand command = null;
		Vector<String> args = new Vector<String>();
		Object[] theArgs = null;
		char argEndDelim = 0;

		// We basically loop until we find a valid command, process < > args if they exist, pass off to callback.
		while (command == null)
		{
			slashLoc = chatline.indexOf("/", currentIndex);
			if (slashLoc == -1)
				return; // No more commands, we're outtie.

			currentIndex = slashLoc + 1;

			// Look for either a space or a bracket <
			while (currentIndex < length)
			{
				if ((line[currentIndex] == ' '))
				{
					break;
				}

				if ((argEndDelim = this.isArgDelim(line[currentIndex])) > 0)
				{
					startArgs = currentIndex;
					break;
				}
				currentIndex++;
			}

			// If it was just a lonely slash on it's own, then it was nuzzing.
			if (currentIndex == (slashLoc + 1))
			{
				currentIndex++;
				continue;
			}

			commandStr = chatline.substring(slashLoc + 1, currentIndex);

			// Find the command in list.
			ListIterator<ChatCommand> iter = this.commands.listIterator();
			ChatCommand iterCmd;
			while (iter.hasNext())
			{
				iterCmd = iter.next();

				if (iterCmd.getCommandName().equalsIgnoreCase(commandStr))
				{
					command = iterCmd;
					break;
				}
			}
		}

		// If this is an admin only command, and the player is not an admin, then we quit now.
		if (command.isAdminOnly())
		{
			if (!this.host.hostIsAdmin(player))
			{
				this.host.hostWhisper("~E~B~wThat command is admin only.", player);
				return;
			}
		}

		boolean isWhisper = chatline.startsWith("~[Iicon.D38]");
		if (command.isWhisperOnly() && !isWhisper)
		{
			this.host.hostWhisper("~E~B~wThat command is whisper only.", player);
			return;
		}

		if (command.isWhisperOnly() && !(chatline.indexOf(this.host.hostGetNickname()) > -1))
		{
			return;
		}

		// Ok so we have command, now  If we haven't already run into args, scan ahead and see if they supplied args, if not we're done.
		if (startArgs == 0)
			while (currentIndex < length)
			{
				if ((argEndDelim = this.isArgDelim(line[currentIndex])) > 0)
				{
					startArgs = currentIndex;
					break;
				}

				else if ((line[currentIndex] != ' '))
				{
					break;
				}

				currentIndex++;
			}

		// If we founds a bracket then parse args.
		char parseInQuotes = 0;
		if (startArgs != 0)
		{
			while (currentIndex < length)
			{
				if (line[currentIndex] == argEndDelim && parseInQuotes == 0)
				{
					endArgs = currentIndex;
					break;
				}

				if (line[currentIndex] == '\'' || line[currentIndex] == '"')
				{
					if (parseInQuotes == 0)
						parseInQuotes = line[currentIndex];
					else
						parseInQuotes = 0;
				}

				currentIndex++;
			}

			// If we didn't find a closing bracket then we won't try and parse args.
			String temp = "";
			if (endArgs > 0)
			{
				temp = chatline.substring(startArgs + 1, endArgs).trim();
			}

			if (temp.length() > 0)
			{
				// Split them.
				char[] argChars = temp.toCharArray();
				boolean inQuotes = false;
				int lastArg = 0;

				// We go through the arg list and pickup the arg seperator (comma).
				// We skip over them if they're contained in a string literal though.
				for (int i = 0; i < temp.length(); i++)
				{
					if (argChars[i] == '"')
					{
						inQuotes = !inQuotes;
					}

					if (argChars[i] == ',' && !inQuotes)
					{
						String arg = temp.substring(lastArg, i).trim();
						if (arg.startsWith("\"") && arg.endsWith("\""))
							arg = arg.substring(1, arg.length() - 1);
						args.add(arg);
						lastArg = i + 1;
					}
				}

				String arg = temp.substring(lastArg, temp.length()).trim();
				if (arg.startsWith("\"") && arg.endsWith("\""))
					arg = arg.substring(1, arg.length() - 1);
				args.add(arg);
			}

			theArgs = new Object[args.size()];

			// If there's args, we parse them.
			if ((args != null) && (args.size() > 0))
			{
				for (int i = 0; i < args.size(); i++)
				{
					ChatCommandArgument arg;
					if (i < command.getArgCount())
					{
						arg = command.getArg(i);

						if (arg.getType() == ChatCommandArgument.ARG_NUMBER)
						{
							float tempVal;
							try
							{
								tempVal = Float.parseFloat(args.elementAt(i));
							}
							catch (NumberFormatException nfe)
							{
								this.host.hostWhisper("~E~B~wParameter #" + (i + 1) + " is not a valid number.", player);
								return;
							}
							theArgs[i] = tempVal;
						}

						if (arg.getType() == ChatCommandArgument.ARG_STRING)
							theArgs[i] = args.elementAt(i);

						if (arg.getType() == ChatCommandArgument.ARG_PLAYER)
						{
							theArgs[i] = this.host.hostFindPlayerByNickname(args.elementAt(i));
							if (theArgs[i] == null)
							{
								this.host.hostWhisper("~E~B~wCouldn't find player '" + args.elementAt(i) + "'", player);
								return;
							}
						}
					}
					else
						theArgs[i] = args.elementAt(i);
				}
			}
		}

		// If the user didn't supply enough args, we won't proceed.
		int argsSupplied = 0;
		if (theArgs != null)
		{
			argsSupplied = theArgs.length;
		}

		if (argsSupplied == 0)
			theArgs = null;

		if (argsSupplied < command.getReqArgCount())
		{
			this.host.hostWhisper("~E~B~wThis command requires " + command.getReqArgCount() + " parameters, you only supplied " + argsSupplied + ".", player);
			return;
		}

		if (argsSupplied > command.getArgCount())
		{
			this.host.hostWhisper("~E~B~wYou have supplied too many parameters for this command.", player);
			return;
		}

		// Invoke the command now.
		command.getCallback().execute(player, isWhisper, theArgs);
	}

	private char isArgDelim(char check)
	{
		for (int i = 0; i < this.argDelims.length; i++)
			if (this.argDelims[i][0] == check)
				return this.argDelims[i][1];

		return 0;
	}

	private char[][] argDelims =
	{
	{ '<', '>' },
	{ '{', '}' },
	{ '[', ']' },
	{ '(', ')' }, };

	private CommandParserHost host;

	private Vector<ChatCommand> commands;

	private Vector<String> groupList;
}
