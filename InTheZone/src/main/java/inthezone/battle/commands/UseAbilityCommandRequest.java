package inthezone.battle.commands;

import java.util.Collection;
import java.util.stream.Collectors;

import inthezone.battle.Ability;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import isogame.engine.MapPoint;

public class UseAbilityCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint target;
	private final Ability ability;

	public UseAbilityCommandRequest(
		MapPoint agent, MapPoint target, Ability ability
	) {
		this.agent = agent;
		this.target = target;
		this.ability = ability;
	}

	@Override
	public Command makeCommand(BattleState battleState) throws CommandException {
		Collection<DamageToTarget> targets =
			battleState.getCharacterAt(agent).map(a ->
				battleState.getAbilityTargets(agent, ability, target).stream()
					.map(t -> ability.computeDamageToTarget(a, t))
					.collect(Collectors.toList())
			).orElseThrow(() -> new CommandException("Invalid ability command request"));

		if (battleState.canDoAbility(agent, ability, targets)) {
			return new UseAbilityCommand(agent, ability.info.name, targets);
		} else {
			throw new CommandException("Invalid ability command request");
		}
	}
}

