package inthezone.battle.commands;

import inthezone.battle.BattleState;

public abstract class CommandRequest {
	public abstract Command makeCommand(BattleState turn) throws CommandException;
}

