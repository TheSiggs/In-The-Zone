package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Collection;

/**
 * Get all the locations that can be targeted.
 * */
public class InfoTargeting extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;
	private final MapPoint castFrom;
	private final Ability ability;

	public InfoTargeting(
		Character subject, MapPoint castFrom, Ability ability
	) {
		this.subject = subject;
		this.castFrom = castFrom;
		this.ability = ability;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(battle.battleState.getTargetableArea(
			subject.getPos(), castFrom, ability));
	}
}

