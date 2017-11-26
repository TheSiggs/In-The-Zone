package inthezone.battle;

import isogame.engine.MapPoint;

public interface Item {
	public void doEffect(Targetable t);
	public boolean canAffect(BattleState battle, MapPoint p);
}

