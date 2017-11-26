package inthezone.battle.commands;

import inthezone.battle.BattleState;
import java.util.List;

public abstract class CommandRequest {
	public abstract List<Command> makeCommand(final BattleState turn)
		throws CommandException;
}

