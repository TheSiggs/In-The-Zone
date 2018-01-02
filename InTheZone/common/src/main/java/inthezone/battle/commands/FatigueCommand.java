package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.DamageToTarget;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.annotations.Field;

/**
 * Fatigue damage.
 * */
public class FatigueCommand extends Command {
	private final CommandKind kind = CommandKind.FATIGUE;
	private final Collection<DamageToTarget> targets = new ArrayList<>();

	@JSONConstructor
	private FatigueCommand(
		@Field("kind") final CommandKind kind,
		@Field("targets") final Collection<DamageToTarget> targets
	) throws ProtocolException {
		this(targets);

		if (kind != CommandKind.FATIGUE)
			throw new ProtocolException("Expected fatigue command");
	}

	public FatigueCommand(final Collection<DamageToTarget> targets) {
		this.targets.addAll(targets);
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		final List<Targetable> r = new ArrayList<>();

		for (final DamageToTarget d : targets) {
			battle.battleState.getTargetableAt(d.target.target)
				.forEach(t -> r.add(t));
		}

		battle.doFatigue(targets);

		return r;
	}
}

