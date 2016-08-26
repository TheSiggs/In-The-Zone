package inthezone.battle.commands;

import inthezone.battle.Ability;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class UseAbilityCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final Collection<MapPoint> targets = new ArrayList<>();;
	private final Ability ability;

	public UseAbilityCommandRequest(
		MapPoint agent, Collection<MapPoint> targets, Ability ability
	) {
		this.agent = agent;
		this.targets.addAll(targets);
		this.ability = ability;
	}

	@Override
	public Command makeCommand(BattleState battleState) throws CommandException {
		Collection<DamageToTarget> allTargets =
			battleState.getCharacterAt(agent).map(a ->
				targets.stream()
					.flatMap(t ->
						battleState.getAbilityTargets(agent, ability, t).stream())
					.map(t -> ability.computeDamageToTarget(a, t))
					.collect(Collectors.toList())
			).orElseThrow(() -> new CommandException("Invalid ability command request"));

		if (battleState.canDoAbility(agent, ability, allTargets)) {
			return new UseAbilityCommand(agent, ability.info.name, allTargets);
		} else {
			throw new CommandException("Invalid ability command request");
		}
	}
}

