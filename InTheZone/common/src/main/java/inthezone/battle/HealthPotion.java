package inthezone.battle;

import isogame.engine.MapPoint;

/**
 * Since there is only one 'item', we'll hard code it
 * */
public class HealthPotion implements Item {
	private static final double HEAL = 0.5;

	public void doEffect(Targetable t) {
		t.revive();
		t.dealDamage(-((int) Math.ceil(((double) t.getStats().hp) * HEAL)));
		t.cleanse();
	}

	public boolean canAffect(BattleState battle, MapPoint p) {
		return battle.getCharacterAt(p).isPresent();
	}
}

