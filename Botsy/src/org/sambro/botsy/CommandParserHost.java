package org.sambro.botsy;

import org.javastorm.Player;

// Anything that wants to host the command parser needs to implement this interface.
public interface CommandParserHost
{
	public void hostSay(String sayText);

	public void hostPopupTo(String sayText, Player player);

	public void hostWhisper(String sayText, Player player);

	public boolean hostIsAdmin(Player player);

	public Player hostFindPlayerByNickname(String nickname);

	public String hostGetNickname();
}
