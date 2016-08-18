package inthezone.battle;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character implements Targetable, Obstacle {
	public final String name;
 	public final Player player;
	public final Collection<Ability> abilities;
	public final Ability basicAbility;
	private final Stats baseStats;
	private final int maxHP;

	private Optional<StatusEffect> statusBuff;
	private Optional<StatusEffect> statusDebuff;

	private MapPoint pos;
	private int ap;
	private int mp;
	private int hp;

	public Character(
		String name,
		Player player,
		Collection<Ability> abilities,
		Ability basicAbility,
		Stats baseStats,
		int maxHP,
		Optional<StatusEffect> statusBuff,
		Optional<StatusEffect> statusDebuff,
		MapPoint pos,
		int ap,
		int mp,
		int hp
	) {
		this.name = name;
		this.player = player;
		this.abilities = abilities;
		this.basicAbility = basicAbility;
		this.baseStats = baseStats;
		this.maxHP = maxHP;
		this.statusBuff = statusBuff;
		this.statusDebuff = statusDebuff;
		this.pos = pos;
		this.ap = ap;
		this.mp = mp;
		this.hp = hp;
	}

	/**
	 * Create a copy of this character.
	 * */
	public Character clone() {
		return new Character(
			name,
			player,
			abilities,
			basicAbility,
			baseStats,
			maxHP,
			statusBuff,
			statusDebuff,
			pos,
			ap,
			mp,
			hp
		);
	}

	public Character(
		CharacterProfile profile, Player player, MapPoint pos
	) {
		this.name = profile.rootCharacter.name;
		this.player = player;
		this.baseStats = profile.getBaseStats();
		this.abilities = profile.abilities.stream()
			.map(i -> new Ability(i)).collect(Collectors.toList());
		this.basicAbility = new Ability(profile.basicAbility);
		this.pos = pos;
		this.maxHP = baseStats.hp;
		this.hp = maxHP;
	}

	public int getAP() {
		return ap;
	}

	public int getMP() {
		return mp;
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

	@Override public boolean blocksSpace(Player player) {
		return true;
	}

	@Override public boolean blocksPath(Player player) {
		return this.player != player;
	}
}

