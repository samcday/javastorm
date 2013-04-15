package org.sambro.botsy;

import org.javastorm.Player;

public interface ChatCommandCallback
{
	public void execute(Player player, boolean whisper, Object... args);
}
