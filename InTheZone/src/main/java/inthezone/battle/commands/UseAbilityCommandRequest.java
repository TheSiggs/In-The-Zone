package inthezone.battle.commands;

import inthezone.battle.Ability;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import inthezone.battle.instant.InstantEffectFactory;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UseAbilityCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint castFrom;
	private final Collection<MapPoint> targets = new ArrayList<>();;
	private final Ability ability;

	public UseAbilityCommandRequest(
		MapPoint agent, MapPoint castFrom,
		Collection<MapPoint> targets, Ability ability
	) {
		this.agent = agent;
		this.castFrom = castFrom;
		this.targets.addAll(targets);
		this.ability = ability;
	}

	@Override
	public List<Command> makeCommand(BattleState battleState) throws CommandException {
		Collection<DamageToTarget> allTargets =
			battleState.getCharacterAt(agent).map(a ->
				targets.stream()
					.flatMap(t ->
						battleState.getAbilityTargets(agent, castFrom, ability, t).stream())
					.map(t -> ability.computeDamageToTarget(a, t))
					.collect(Collectors.toList())
			).orElseThrow(() -> new CommandException("Invalid ability command request"));

		List<MapPoint> preTargets = new ArrayList<>();
		List<MapPoint> postTargets = new ArrayList<>();
		for (DamageToTarget t : allTargets) {
			if (t.pre) preTargets.add(t.target);
			if (t.post) preTargets.add(t.target);
		}

		List<Command> r = new ArrayList<>();

		// Instant effect
		Collection<MapPoint> targetArea = null;
		if (preTargets.size() > 0 && ability.info.instantBefore.isPresent()) {
			targetArea = battleState.getTargetableArea(agent, castFrom, ability);
			r.add(new InstantEffectCommand(InstantEffectFactory.getEffect(
				battleState, ability.info.instantBefore.get(),
				castFrom, targetArea, preTargets)));
		}

		// Main damage
		if (battleState.canDoAbility(agent, castFrom, ability, allTargets)) {
			r.add(new UseAbilityCommand(agent, castFrom, ability.rootName,
				allTargets, ability.subsequentLevel, ability.recursionLevel));
		} else {
			throw new CommandException("Invalid ability command request");
		}
			
		// Instant effect
		if (postTargets.size() > 0 && ability.info.instantAfter.isPresent()) {
			if (targetArea == null) targetArea =
				battleState.getTargetableArea(agent, castFrom, ability);

			r.add(new InstantEffectCommand(InstantEffectFactory.getEffect(
				battleState, ability.info.instantAfter.get(),
				castFrom, targetArea, preTargets)));
		}

		return r;
	}
}

