package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import java.util.Collection;

public abstract class Command {
	/**
	 * Do a command.
	 * @return All the characters that were affected by the command
	 * */
	public abstract Collection<Character> doCmd(Battle turn) throws CommandException;
}

