package inthezone.comptroller;

import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import java.util.List;

/**
 * Implemented by the GUI to process battle events.
 * */
public interface BattleListener {
	public void startTurn();
	public void endTurn();
	public void endBattle(boolean playerWins);
	public void badCommand(CommandException e);

	/**
	 * May be invoked with a null command, in which case we should just update
	 * the characters list.
	 * */
	public void command(Command cmd, List<Character> affectedCharacters);
}

