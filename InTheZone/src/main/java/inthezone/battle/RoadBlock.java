package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

public class RoadBlock implements Targetable {
	private final static int HITS_TO_DESTROY = 2;
	private final MapPoint pos;
	private int hits;
	private final SpriteInfo sprite;

	public RoadBlock(MapPoint pos, StandardSprites sprites) {
		this.pos = pos;
		hits = HITS_TO_DESTROY;
		this.sprite = sprites.roadBlock;
	}

	@Override public boolean blocksSpace(Player player) {return true;}
	@Override public boolean blocksPath(Player player) {return true;}

	@Override public Stats getStats() {return new Stats();}
	@Override public MapPoint getPos() {return pos;}
	@Override public double getAttackBuff() {return 0;}
	@Override public double getDefenceBuff() {return 0;}
	@Override public void dealDamage(int damage) {
		hits -= 1;
		if (hits < 0) hits = 0;
	}
	@Override public void applyStatus(StatusEffect status) {return;}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(Character character) {return true;}
	@Override public boolean isDead() {return hits == 0;}
	@Override public boolean reap() {return isDead();}

	@Override public SpriteInfo getSprite() {return sprite;}
}

