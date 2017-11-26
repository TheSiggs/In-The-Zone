package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.CharacterFrozen;
import isogame.engine.MapPoint;
import java.util.Collection;

/**
 * Get the targeting area for an item.
 * */
public class InfoTargetingItem extends InfoRequest<Collection<MapPoint>> {
	private final CharacterFrozen subject;

	public InfoTargetingItem(final CharacterFrozen subject) {
		this.subject = subject;
	}

	@Override public void completeAction(final Battle battle) {
		complete.complete(battle.battleState.getItemArea(subject.getPos()));
	}
}

