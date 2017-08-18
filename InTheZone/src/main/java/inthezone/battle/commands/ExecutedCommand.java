package inthezone.battle.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import inthezone.battle.Targetable;
import inthezone.battle.TargetableFrozen;

/**
 * A command that has already been executed, plus the affected targetables.
 * */
public class ExecutedCommand {
	public final Command cmd;
	public final List<TargetableFrozen> affected = new ArrayList<>();

	/**
	 * true if the is the last in a sequence of commands generated by a single
	 * command request.
	 * */
	public final boolean lastInSequence;

	public ExecutedCommand(
		final Command cmd,
		final List<? extends Targetable> affected
	) {
		this(cmd,
			affected.stream().map(c -> c.freeze()).collect(Collectors.toList()),
			false);
	}

	private ExecutedCommand(
		final Command cmd,
		final List<? extends TargetableFrozen> affected,
		final boolean lastInSequence
	) {
		this.cmd = cmd;
		this.lastInSequence = lastInSequence;

		final Set<TargetableFrozen> seen = new HashSet<>();
		for (final TargetableFrozen t : affected) {
			if (!seen.contains(t)) this.affected.add(t);
			seen.add(t);
		}
	}

	public ExecutedCommand markLastInSequence() {
		return new ExecutedCommand(cmd, affected, true);
	}
}

