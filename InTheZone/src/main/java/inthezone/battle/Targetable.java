package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

public interface Targetable extends Obstacle {
	public Stats getStats();
	public MapPoint getPos();
	public double getAttackBuff();
	public double getDefenceBuff();
	public void dealDamage(int damage);
	public boolean isPushable();
	public boolean isEnemyOf(Character character);
	public boolean isDead();

	// get the sprite that represents this object
	public SpriteInfo getSprite();

	// return true if this targetable should be removed from the board
	public boolean reap();
}

