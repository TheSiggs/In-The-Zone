package inthezone.comptroller;

import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import java.util.List;

/**
 * Implemented by the GUI to process battle events.
 * */
public interface BattleListener {
	/**
	 * Start the player's turn.
	 * @param characters Data for all the characters to update the HUD.
	 * */
	public void startTurn(List<Character> characters);

	/**
	 * Start the other player's turn.
	 * @param characters Data for all the characters to update the HUD.
	 * */
	public void endTurn(List<Character> characters);

	/**
	 * Deal with the battle end condition.
	 * */
	public void endBattle(boolean playerWins);

	/**
	 * Handle a bad command.  This probably happens because the other player is
	 * cheating.
	 * */
	public void badCommand(CommandException e);

	/**
	 * Execute a validated and not null command.
	 * */
	public void command(Command cmd, List<Character> affectedCharacters);
}

