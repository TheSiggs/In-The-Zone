package inthezone.battle.commands;

import java.util.Collection;
import java.util.stream.Collectors;

import inthezone.battle.Ability;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import isogame.engine.MapPoint;

public class AttackCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint target;

	public AttackCommandRequest(MapPoint agent, MapPoint target) {
		this.agent = agent;
		this.target = target;
	}

	@Override
	public Command makeCommand(BattleState battleState) throws CommandException {
		Collection<DamageToTarget> targets =
			battleState.getCharacterAt(agent).map(a ->
				battleState.getAbilityTargets(agent, a.basicAbility, target).stream()
					.map(t -> a.basicAbility.computeDamageToTarget(a, t))
					.collect(Collectors.toList())
			).orElseThrow(() -> new CommandException("Invalid ability command request"));

		if (battleState.canAttack(agent, targets)) {
			return new AttackCommand(agent, targets);
		} else {
			throw new CommandException("Invalid ability command request");
		}
	}
}

