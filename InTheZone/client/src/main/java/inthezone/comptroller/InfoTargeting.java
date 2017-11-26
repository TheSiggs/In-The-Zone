package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.data.AbilityZoneType;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Get all the locations that can be targeted.
 * */
public class InfoTargeting extends InfoRequest<Collection<MapPoint>> {
	private final CharacterFrozen subject;
	private final MapPoint castFrom;
	private final Ability ability;
	private final Set<MapPoint> retargetedFrom;

	public InfoTargeting(
		final CharacterFrozen subject,
		final MapPoint castFrom,
		final Set<MapPoint> retargetedFrom,
		final Ability ability
	) {
		this.subject = subject;
		this.castFrom = castFrom;
		this.ability = ability;
		this.retargetedFrom = retargetedFrom;
	}


	@Override public void completeAction(final Battle battle) {
		final Collection<MapPoint> area =
			battle.battleState.getTargetableArea(
				subject.getPos(), castFrom, ability).stream()
					.filter(x -> !retargetedFrom.contains(x))
					.collect(Collectors.toList());

		if (ability.info.trap) {
			complete.complete(area.stream()
				.filter(p ->
					battle.battleState.isSpaceFree(p) &&
					!battle.battleState.getTrapAt(p).isPresent() &&
					!battle.battleState.hasDefusingZone(p))
				.collect(Collectors.toList()));

		} else if (
			ability.info.instantBefore.map(i -> i.isField()).orElse(false) ||
			ability.info.instantAfter.map(i -> i.isField()).orElse(false)
		) {
			complete.complete(area.stream()
				.filter(p -> battle.battleState.isSpaceFree(p) &&
					!battle.battleState.getTrapAt(p).isPresent())
				.collect(Collectors.toList()));

		} else if (ability.info.zone == AbilityZoneType.ZONE) {
			complete.complete(area.stream()
				.filter(p ->
					!battle.battleState.getAffectedArea(
						subject.getPos(), AbilityAgentType.CHARACTER,
						ability, new Casting(castFrom, p)).stream()
					.anyMatch(p2 -> battle.battleState.getZoneAt(p2).isPresent()))
				.collect(Collectors.toList()));

		} else if (ability.info.zone == AbilityZoneType.BOUND_ZONE) {
			complete.complete(area.stream()
				.filter(p ->
					battle.battleState.isSpaceFree(p) &&
					!battle.battleState.getAffectedArea(
						subject.getPos(), AbilityAgentType.CHARACTER,
						ability, new Casting(castFrom, p)).stream()
					.anyMatch(p2 -> battle.battleState.getZoneAt(p2).isPresent()))
				.collect(Collectors.toList()));
			
		} else {
			complete.complete(area.stream()
				.filter(t -> 
					!battle.battleState.getAbilityTargets(
						subject.getPos(), AbilityAgentType.CHARACTER,
						ability, battle.battleState.getAffectedArea(
							subject.getPos(), AbilityAgentType.CHARACTER,
							ability, new Casting(castFrom, t))).isEmpty())
				.collect(Collectors.toList()));
		}
	}
}

