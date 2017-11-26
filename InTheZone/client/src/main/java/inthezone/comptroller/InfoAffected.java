package inthezone.comptroller;

import isogame.engine.MapPoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.TargetableFrozen;
import inthezone.battle.commands.AbilityAgentType;

/**
 * Get all the locations that can be targeted.
 * */
public class InfoAffected extends InfoRequest<Collection<TargetableFrozen>> {
	private final CharacterFrozen subject;
	private final Ability ability;
	private final Collection<Casting> castings;

	public InfoAffected(
		final CharacterFrozen subject,
		final Ability ability,
		final Collection<Casting> castings
	) {
		this.subject = subject;
		this.ability = ability;
		this.castings = castings;
	}

	@Override public void completeAction(final Battle battle) {
		final Set<MapPoint> affectedArea = new HashSet<>();
		for (final Casting t : castings) {
			affectedArea.addAll(
				battle.battleState.getAffectedArea(subject.getPos(),
					AbilityAgentType.CHARACTER, ability, t));
		}

		complete.complete(
			battle.battleState.getAbilityTargets(
				subject.getPos(), AbilityAgentType.CHARACTER,
				ability, affectedArea).stream()
			.map(t -> t.freeze())
			.collect(Collectors.toList()));
	}
}


