package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Get all the locations that can be targeted.
 * */
public class InfoAffected extends InfoRequest<Collection<Targetable>> {
	private final Character subject;
	private final MapPoint castFrom;
	private final Ability ability;
	private final Collection<MapPoint> targets;

	public InfoAffected(
		Character subject, Ability ability,
		MapPoint castFrom, Collection<MapPoint> targets
	) {
		this.subject = subject;
		this.castFrom = castFrom;
		this.ability = ability;
		this.targets = targets;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(targets.stream()
			.flatMap(t ->
				battle.battleState.getAbilityTargets(
					subject.getPos(), AbilityAgentType.CHARACTER,
					castFrom, ability, t).stream())
			.collect(Collectors.toList()));
	}
}


