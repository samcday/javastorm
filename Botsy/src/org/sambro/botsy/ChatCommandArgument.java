package org.sambro.botsy;

public class ChatCommandArgument
{
	public ChatCommandArgument(String argName, int argType, boolean required)
	{
		this.argName = argName;
		this.argType = argType;
		this.required = required;
	}

	public String getName()
	{
		return argName;
	}

	public int getType()
	{
		return argType;
	}

	public boolean isRequired()
	{
		return required;
	}

	private String argName;

	private int argType;

	private boolean required;

	public static final int ARG_PLAYER = 0;

	public static final int ARG_STRING = 1;

	public static final int ARG_NUMBER = 2;
}
