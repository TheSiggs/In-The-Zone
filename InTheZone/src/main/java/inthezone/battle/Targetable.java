package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import isogame.engine.MapPoint;

public interface Targetable {
	public Stats getStats();
	public MapPoint getPos();
	public double getAttackBuff();
	public double getDefenceBuff();
	public void dealDamage(int damage);
	public boolean isPushable();
	public boolean isEnemyOf(Character character);
}

