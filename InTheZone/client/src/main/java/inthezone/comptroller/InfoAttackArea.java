package inthezone.comptroller;

import isogame.engine.MapPoint;

import java.util.Collection;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.commands.AbilityAgentType;

public class InfoAttackArea extends InfoRequest<Collection<MapPoint>> {
	private final CharacterFrozen subject;
	private final MapPoint castFrom;
	private final Ability ability;
	private final MapPoint target;

	public InfoAttackArea(
		final CharacterFrozen subject,
		final Ability ability,
		final MapPoint castFrom,
		final MapPoint target
	) {
		this.subject = subject;
		this.ability = ability;
		this.castFrom = castFrom;
		this.target = target;
	}

	@Override public void completeAction(final Battle battle) {
		complete.complete(battle.battleState.getAffectedArea(
			subject.getPos(), AbilityAgentType.CHARACTER,
			ability, new Casting(castFrom, target)));
	}
}

