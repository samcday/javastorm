package org.sambro.botsy;

public class ChatCommand
{

	public ChatCommand(String commandName, String description, String group, ChatCommandCallback callback, ChatCommandArgument args[], boolean whisperOnly, boolean adminOnly)
	{
		// Quick hack. All commands must now be whispered.
		//whisperOnly = true;

		this.commandName = commandName;
		this.description = description;
		this.group = group;
		this.callback = callback;
		this.whisperOnly = whisperOnly;
		this.adminOnly = adminOnly;
		this.args = args;

		for (int i = 0; i < this.args.length; i++)
			if (this.args[i].isRequired())
				this.reqArgs++;
	}

	public String getCommandName()
	{
		return commandName;
	}

	public String getDescription()
	{
		return description;
	}

	public int getArgCount()
	{
		return this.args.length;
	}

	public int getReqArgCount()
	{
		return this.reqArgs;
	}

	public ChatCommandArgument getArg(int index)
	{
		return this.args[index];
	}

	public ChatCommandArgument[] getArgs()
	{
		return this.args;
	}

	public String getGroup()
	{
		return group;
	}

	public ChatCommandCallback getCallback()
	{
		return this.callback;
	}

	public boolean isAdminOnly()
	{
		return adminOnly;
	}

	public boolean isWhisperOnly()
	{
		return whisperOnly;
	}

	private int reqArgs;

	private String commandName;

	private ChatCommandArgument[] args;

	private ChatCommandCallback callback;

	private String group;

	private String description;

	private boolean adminOnly;

	private boolean whisperOnly;
}
