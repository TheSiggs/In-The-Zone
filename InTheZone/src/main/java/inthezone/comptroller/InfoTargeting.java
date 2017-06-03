package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Get all the locations that can be targeted.
 * */
public class InfoTargeting extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;
	private final MapPoint castFrom;
	private final Ability ability;
	private final Set<MapPoint> retargetedFrom;

	public InfoTargeting(
		Character subject,
		MapPoint castFrom,
		Set<MapPoint> retargetedFrom,
		Ability ability
	) {
		this.subject = subject;
		this.castFrom = castFrom;
		this.ability = ability;
		this.retargetedFrom = retargetedFrom;
	}

	@Override public void completeAction(Battle battle) {
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
		}

		complete.complete(area);
	}
}

