package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Zone extends Targetable implements HasParentAgent {
	public final MapPoint centre;
	public final Set<MapPoint> range = new HashSet<>();
	public final Ability ability;
	public final Character parent;

	private final boolean hasMana;
	private final double chanceBuff;
	private final double attackBuff;
	private final double defenceBuff;
	private final Stats stats;

	private final Optional<SpriteInfo> sprite;
	private boolean purged = false;
	private Optional<Integer> turnsRemaining;

	@Override public String toString() {
		return "ZONE on " + centre + " " + range.toString();
	}

	@Override public Character getParent() {return parent;}

	public Zone(
		MapPoint centre,
		Collection<MapPoint> range,
		Optional<Integer> turns,  // empty for a zone that isn't turn limited
		boolean hasMana,
		Ability ability,
		Character agent
	) {
		this.centre = centre;
		this.range.addAll(range);
		this.hasMana = hasMana;
		this.ability = ability;
		this.parent = agent;

		this.turnsRemaining = turns;

		this.sprite = ability.info.media.zoneTrapSprite;

		this.chanceBuff = agent.getChanceBuff();
		this.attackBuff = agent.getAttackBuff();
		this.defenceBuff = agent.getDefenceBuff();
		this.stats = agent.getStats();
	}

	public Zone(
		MapPoint centre,
		Collection<MapPoint> range,
		Optional<Integer> turns,
		Ability ability,
		Character parent,
		Optional<SpriteInfo> sprite,
		boolean hasMana,
		double chanceBuff,
		double attackBuff,
		double defenceBuff,
		Stats stats,
		boolean purged
	) {
		this.centre = centre;
		this.range.addAll(range);
		this.ability = ability;
		this.parent = parent;
		this.sprite = sprite;
		this.turnsRemaining = turns;
		this.hasMana = hasMana;
		this.chanceBuff = chanceBuff;
		this.attackBuff = attackBuff;
		this.defenceBuff = defenceBuff;
		this.stats = stats;
		this.purged = purged;
	}

	/**
	 * Call once at the start of each turn
	 * */
	public void notifyTurn() {
		turnsRemaining = turnsRemaining.map(t -> t - 1);
	}

	@Override public boolean blocksSpace() {return false;}
	@Override public boolean blocksPath(Player player) {return false;}

	@Override public Stats getStats() {return stats;}
	@Override public MapPoint getPos() {return centre;}
	@Override public double getAttackBuff() {return attackBuff;}
	@Override public double getDefenceBuff() {return defenceBuff;}
	@Override public void dealDamage(int damage) {return;}
	@Override public void defuse() {return;}
	@Override public void cleanse() {return;}
	@Override public void purge() {purged = true;}
	@Override public void revive() {return;}
	@Override public void applyStatus(Battle battle, StatusEffect status) {return;}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(Character character) {return true;}
	@Override public boolean isDead() {
		return purged || turnsRemaining.map(t -> t <= 0).orElse(false);
	}
	@Override public SpriteInfo getSprite() {return sprite.orElse(null);}
	@Override public boolean reap() {return isDead();}

	@Override public boolean hasMana() {return hasMana;}
	@Override public double getChanceBuff() {return chanceBuff;}

	@Override public Zone clone() {
		return new Zone(
			centre, range, turnsRemaining, ability, parent,
			sprite, hasMana, chanceBuff, attackBuff, defenceBuff,
			stats, purged);
	}
}

