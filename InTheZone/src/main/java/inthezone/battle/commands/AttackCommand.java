package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AttackCommand extends Command {
	private final MapPoint agent;
	private final Collection<DamageToTarget> targets;


	public AttackCommand(
		MapPoint agent, Collection<DamageToTarget> targets
	) {
		this.agent = agent;
		this.targets = targets;
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canAttack(agent, targets))
			throw new CommandException("Invalid attack");

		List<Character> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
		for (DamageToTarget d : targets)
			battle.battleState.getCharacterAt(d.target).ifPresent(c -> r.add(c));

		battle.doAttack(agent, targets);

		return r.stream().map(c -> c.clone()).collect(Collectors.toList());
	}
}

