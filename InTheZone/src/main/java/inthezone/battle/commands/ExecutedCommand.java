package inthezone.battle.commands;

import inthezone.battle.Targetable;
import java.util.List;

/**
 * A command that has already been executed, plus the affected targetables.
 * */
public class ExecutedCommand {
	public final Command cmd;
	public final List<Targetable> affected;

	public ExecutedCommand(Command cmd, List<Targetable> affected) {
		this.cmd = cmd;
		this.affected = affected;
	}
}

