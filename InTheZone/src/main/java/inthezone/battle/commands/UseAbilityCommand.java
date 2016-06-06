package inthezone.battle.commands;

import java.util.Collection;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.DamageToTarget;
import isogame.engine.MapPoint;

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
	public void doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canDoAbility(agent, ability, targets))
			throw new CommandException("Invalid ability command");
		battle.doAbility(agent, ability, targets);
	}
}
