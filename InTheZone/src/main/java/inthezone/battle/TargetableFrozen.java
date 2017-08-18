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
	}

	public Stats getStats() { return parent.getStats(); }
	public MapPoint getPos() { return parent.getPos(); }
	public double getAttackBuff() { return parent.getAttackBuff(); }
	public double getDefenceBuff() { return parent.getDefenceBuff(); }
	public boolean isPushable() { return parent.isPushable(); }
	public boolean isDead() { return parent.isDead(); }
	public boolean hasMana() { return parent.hasMana(); }
	public double getChanceBuff() { return parent.getChanceBuff(); }
	public SpriteInfo getSprite() { return parent.getSprite(); }
	public boolean reap() { return parent.reap(); }
	public Optional<Zone> getCurrentZone() { return parent.currentZone; }
	public boolean blocksSpace() { return parent.blocksSpace(); }

	public boolean isEnemyOf(final Character character) {
		return parent.isEnemyOf(character);
	}

	public boolean blocksPath(final Player player) {
		return parent.blocksPath(player);
	}
}

