package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.Optional;

public class Character implements Targetable {
	private final Player player;
	private final Stats baseStats;

	private final Collection<Ability> abilities;

	private Optional<StatusEffect> statusBuff;
	private Optional<StatusEffect> statusDebuff;

	private MapPoint pos;
	private int hp;
	private final int maxHP;

	public Character(
		Player player, MapPoint pos, Stats baseStats,
		Collection<Ability> abilities
	) {
		this.player = player;
		this.baseStats = baseStats;
		this.pos = pos;
		this.maxHP = baseStats.hp;
		this.hp = maxHP;
		this.abilities = abilities;
	}

	/* Make a clone of the character at a new position.
	 * The clone is reset to default stats.
	 * */
	public Character cloneTo(MapPoint pos, Player newPlayer) {
		return new Character (newPlayer, pos, baseStats, abilities);
	}

	@Override public Stats getStats() {
		return baseStats;
	}

	@Override public MapPoint getPos() {
		return pos;
	}

	@Override public double getAttackBuff() {
		double buff = statusBuff.map(s -> s.attackBuff).orElse(0.0);
		double debuff = statusDebuff.map(s -> s.attackBuff).orElse(0.0);
		return buff - debuff;
	}

	@Override public double getDefenceBuff() {
		double buff = statusBuff.map(s -> s.defenceBuff).orElse(0.0);
		double debuff = statusDebuff.map(s -> s.defenceBuff).orElse(0.0);
		return buff - debuff;
	}

	@Override public void dealDamage(int damage) {
		hp = Math.max(0, hp - damage);
	}

	@Override public boolean isPushable() {
		return true;
	}

	@Override public Player getPlayer() {
		return player;
	}
}

