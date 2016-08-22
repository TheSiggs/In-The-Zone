package inthezone.battle.commands;

import java.util.Collection;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UseAbilityCommand extends Command {
	private final MapPoint agent;
	private final Ability ability;
	private final Collection<DamageToTarget> targets;

	public UseAbilityCommand(
		MapPoint agent, Ability ability, Collection<DamageToTarget> targets
	) {
		this.agent = agent;
		this.ability = ability;
		this.targets = targets;
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canDoAbility(agent, ability, targets))
			throw new CommandException("Invalid ability command");

		Collection<Character> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
		for (DamageToTarget d : targets)
			battle.battleState.getCharacterAt(d.target).ifPresent(c -> r.add(c));

		battle.doAbility(agent, ability, targets);

		return r.stream().map(c -> c.clone()).collect(Collectors.toList());
	}
}

