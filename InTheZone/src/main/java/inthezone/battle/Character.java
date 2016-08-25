package inthezone.battle;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character implements Targetable, Obstacle {
	public final int id; // a unique identifier that can be used to track this character
	public final String name;
 	public final Player player;
	public final SpriteInfo sprite;
	public final Collection<Ability> abilities;
	public final Ability basicAbility;
	private final Stats baseStats;
	private final int maxHP;

	private Optional<StatusEffect> statusBuff = Optional.empty();
	private Optional<StatusEffect> statusDebuff = Optional.empty();

	private MapPoint pos = new MapPoint(0, 0);
	private int ap = 0;
	private int mp = 0;
	private int hp = 0;

	public Character(
		int id,
		String name,
		Player player,
		SpriteInfo sprite,
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
		this.id = id;
		this.name = name;
		this.player = player;
		this.sprite = sprite;
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
			id,
			name,
			player,
			sprite,
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
		CharacterProfile profile, Player player, MapPoint pos, int id
	) {
		this.id = id;
		this.name = profile.rootCharacter.name;
		this.player = player;
		this.sprite = profile.rootCharacter.sprite;
		this.baseStats = profile.getBaseStats();
		this.abilities = profile.abilities.stream()
			.map(i -> new Ability(i)).collect(Collectors.toList());
		this.basicAbility = new Ability(profile.basicAbility);
		this.pos = pos;
		this.maxHP = baseStats.hp;
		this.ap = baseStats.ap;
		this.mp = baseStats.mp;
		this.hp = maxHP;
	}

	public int getAP() {
		return ap;
	}

	public int getMP() {
		return mp;
	}

	public int getHP() {
		return hp;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public void moveTo(MapPoint p) {
		mp -= Math.abs(pos.x - p.x) + Math.abs(pos.y - p.y);
		if (mp < 0) mp = 0;
		this.pos = p;
	}

	public void useAbility(Ability ability) {
		if (!ability.isSubsequent && ability.recursionLevel == 0) {
			ap -= ability.info.ap;
			mp -= ability.info.mp;
			if (ap < 0) ap = 0;
			if (mp < 0) mp = 0;
		}
	}

	public void kill() {
		hp = 0;
	}

	public boolean isDead() {
		return hp == 0;
	}

	public void teleport(MapPoint p) {
		this.pos = p;
	}

	/**
	 * Call this at the start of each turn.
	 * @param player The player who's turn is starting
	 * */
	public void turnReset(Player player) {
		if (this.player == player) {
			Stats stats = getStats();
			ap = stats.ap;
			mp = stats.mp;
		}
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
		System.err.println("HP: " + hp + " after damage " + damage);
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
		return !(hp == 0 || this.player == player);
	}
}

