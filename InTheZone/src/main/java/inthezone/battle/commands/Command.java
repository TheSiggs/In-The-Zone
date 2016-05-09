package inthezone.battle.commands;

import inthezone.battle.Battle;

public abstract class Command {
	public abstract void doCmd(Battle turn) throws CommandException;
}

