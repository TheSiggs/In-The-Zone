package inthezone.comptroller;

import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;

public interface BattleListener {
	public void startTurn();
	public void endTurn();
	public void endBattle(boolean playerWins);
	public void badCommand(CommandException e);
	public void command(Command cmd);
}

