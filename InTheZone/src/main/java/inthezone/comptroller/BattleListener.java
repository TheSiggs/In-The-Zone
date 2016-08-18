package inthezone.comptroller;

import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import java.util.Collection;

/**
 * Implemented by the GUI to process battle events.
 * */
public interface BattleListener {
	public void startTurn();
	public void endTurn();
	public void endBattle(boolean playerWins);
	public void badCommand(CommandException e);
	public void command(Command cmd);
	public void updateCharacters(Collection<Character> characters);
}

