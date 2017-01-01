package inthezone.comptroller;

import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.Targetable;
import java.util.List;

/**
 * Implemented by the GUI to process battle events.  These methods must be
 * invoked in the GUI thread.
 * */
public interface BattleListener {
	/**
	 * Deal with the battle end condition.
	 * */
	public void endBattle(BattleOutcome outcome);

	/**
	 * Handle a bad command.  This probably happens because the other player is
	 * cheating.
	 * */
	public void badCommand(CommandException e);

	/**
	 * Execute a validated and not null command.
	 * */
	public void command(ExecutedCommand ec);

	/**
	 * Complete an instant effect with extra targeting information
	 * */
	public void completeEffect(InstantEffect e);
}

