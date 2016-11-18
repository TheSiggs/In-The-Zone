package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.Collection;

public class Zone extends Targetable implements HasParentAgent {
	public final MapPoint centre;
	public final Collection<MapPoint> range;
	public final Ability ability;
	public final Character parent;

	private final boolean hasMana;
	private final double chanceBuff;
	private final double attackBuff;
	private final double defenceBuff;
	private final Stats stats;

	private boolean purged = false;
	private int turnsRemaining;

	@Override public Character getParent() {return parent;}

	public Zone(
		MapPoint centre,
		Collection<MapPoint> range,
		int turns,
		boolean hasMana,
		Ability ability,
		Character agent
	) {
		this.centre = centre;
		this.range = range;
		this.hasMana = hasMana;
		this.ability = ability;
		this.parent = agent;

		this.turnsRemaining = turns;

		this.chanceBuff = agent.getChanceBuff();
		this.attackBuff = agent.getAttackBuff();
		this.defenceBuff = agent.getDefenceBuff();
		this.stats = agent.getStats();
	}

	public Zone(
		MapPoint centre,
		Collection<MapPoint> range,
		int turns,
		Ability ability,
		Character parent,
		boolean hasMana,
		double chanceBuff,
		double attackBuff,
		double defenceBuff,
		Stats stats,
		boolean purged
	) {
		this.centre = centre;
		this.range = range;
		this.ability = ability;
		this.parent = parent;
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
	public boolean canRemoveNow() {
		turnsRemaining -= 1;
		return reap();
	}

	@Override public boolean blocksSpace(Player player) {return false;}
	@Override public boolean blocksPath(Player player) {return false;}

	@Override public Stats getStats() {return stats;}
	@Override public MapPoint getPos() {return centre;}
	@Override public double getAttackBuff() {return attackBuff;}
	@Override public double getDefenceBuff() {return defenceBuff;}
	@Override public void dealDamage(int damage) {return;}
	@Override public void defuse() {return;}
	@Override public void cleanse() {return;}
	@Override public void purge() {purged = true;}
	@Override public void applyStatus(StatusEffect status) {return;}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(Character character) {return true;}
	@Override public boolean isDead() {return purged || turnsRemaining <= 0;}
	@Override public SpriteInfo getSprite() {return null;}
	@Override public boolean reap() {return isDead();}

	@Override public boolean hasMana() {return hasMana;}
	@Override public double getChanceBuff() {return chanceBuff;}

	@Override public Zone clone() {
		return new Zone(
			centre, range, turnsRemaining, ability, parent,
			hasMana, chanceBuff, attackBuff, defenceBuff,
			stats, purged);
	}
}

