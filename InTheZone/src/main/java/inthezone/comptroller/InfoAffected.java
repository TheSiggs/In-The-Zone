package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Get all the locations that can be targeted.
 * */
public class InfoAffected extends InfoRequest<Collection<Targetable>> {
	private final Character subject;
	private final Ability ability;
	private final Collection<Casting> castings;

	public InfoAffected(
		Character subject, Ability ability, Collection<Casting> castings
	) {
		this.subject = subject;
		this.ability = ability;
		this.castings = castings;
	}

	@Override public void completeAction(Battle battle) {
		final Set<MapPoint> affectedArea = new HashSet<>();
		for (Casting t : castings) {
			affectedArea.addAll(
				battle.battleState.getAffectedArea(subject.getPos(),
					AbilityAgentType.CHARACTER, ability, t));
		}

		complete.complete(
			battle.battleState.getAbilityTargets(
				subject.getPos(), AbilityAgentType.CHARACTER,
				ability, affectedArea).stream()
			.collect(Collectors.toList()));
	}
}


