package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Collection;

/**
 * Get the targeting area for an item.
 * */
public class InfoTargetingItem extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;

	public InfoTargetingItem(Character subject) {
		this.subject = subject;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(battle.battleState.getItemArea(subject.getPos()));
	}
}

