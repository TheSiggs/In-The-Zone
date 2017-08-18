package inthezone.battle;

import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

import java.util.Optional;

import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;

/**
 * Immutable information about a targetable
 * */
public abstract class TargetableFrozen {
	protected final Targetable parent;
	protected TargetableFrozen(final Targetable parent) {
		this.parent = parent;

		getStats = parent.getStats();
		getPos = parent.getPos();
		getAttackBuff = parent.getAttackBuff();
		getDefenceBuff = parent.getDefenceBuff();
		isPushable = parent.isPushable();
		isDead = parent.isDead();
		hasMana = parent.hasMana();
		getChanceBuff = parent.getChanceBuff();
		getSprite = parent.getSprite();
		reap = parent.reap();
		getCurrentZone = parent.currentZone;
		blocksSpace = parent.blocksSpace();
	}

	final protected Stats getStats;
	final protected MapPoint getPos;
	final protected double getAttackBuff;
	final protected double getDefenceBuff;
	final protected boolean isPushable;
	final protected boolean isDead;
	final protected boolean hasMana;
	final protected double getChanceBuff;
	final protected SpriteInfo getSprite;
	final protected boolean reap;
	final protected Optional<Zone> getCurrentZone;
	final protected boolean blocksSpace;

	public Stats getStats() { return getStats; }
	public MapPoint getPos() { return getPos; }
	public double getAttackBuff() { return getAttackBuff; }
	public double getDefenceBuff() { return getDefenceBuff; }
	public boolean isPushable() { return isPushable; }
	public boolean isDead() { return isDead; }
	public boolean hasMana() { return hasMana; }
	public double getChanceBuff() { return getChanceBuff; }
	public SpriteInfo getSprite() { return getSprite; }
	public boolean reap() { return reap; }
	public Optional<Zone> getCurrentZone() { return getCurrentZone; }
	public boolean blocksSpace() { return blocksSpace; }
}

