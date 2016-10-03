package inthezone.battle;

import inthezone.battle.commands.Command;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.data.StatusEffectKind;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character implements Targetable {
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

	private boolean hasMana = false;
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
		boolean hasMana,
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
		this.hasMana = hasMana;
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
			hasMana,
			pos,
			ap,
			mp,
			hp
		);
	}

	public Character(
		CharacterProfile profile,
		Player player,
		boolean hasMana,
		MapPoint pos,
		int id
	) {
		this.id = id;
		this.name = profile.rootCharacter.name;
		this.player = player;
		this.sprite = profile.rootCharacter.sprite;
		this.baseStats = profile.getBaseStats();
		this.abilities = profile.abilities.stream()
			.map(i -> new Ability(i)).collect(Collectors.toList());
		this.basicAbility = new Ability(profile.basicAbility);
		this.hasMana = hasMana;
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

	public boolean hasMana() {
		return hasMana;
	}

	/**
	 * Buff or debuff this character's points. Should be done after the turn reset.
	 * */
	public void pointsBuff(int ap, int mp, int hp) {
		this.ap += ap;
		this.mp += mp;
		this.hp += hp;
		if (this.hp > maxHP) this.hp = maxHP;
		if (this.ap < 0) ap = 0;
		if (this.mp < 0) mp = 0;
		if (this.hp < 0) hp = 0;
	}

	public void moveTo(MapPoint p, boolean hasMana) {
		this.hasMana = hasMana;
		mp -= Math.abs(pos.x - p.x) + Math.abs(pos.y - p.y);
		if (mp < 0) mp = 0;
		this.pos = p;
	}

	public void useAbility(Ability ability) {
		if (ability.subsequentLevel == 0 && ability.recursionLevel == 0) {
			ap -= ability.info.ap;
			mp -= ability.info.mp;
			if (ap < 0) ap = 0;
			if (mp < 0) mp = 0;
		}
	}

	public void usePush() {
		ap -= 1;
		if (ap < 0) ap = 0;
	}

	public void kill() {
		hp = 0;
	}

	@Override public boolean isDead() {
		return hp == 0;
	}

	public void teleport(MapPoint p, boolean hasMana) {
		this.hasMana = hasMana;
		this.pos = p;
	}

	/**
	 * Call this at the start of each turn.
	 * @param player The player who's turn is starting
	 * */
	public List<Command> turnReset(Battle battle, Player player) {
		if (this.player == player) {
			Stats stats = getStats();
			ap = stats.ap;
			mp = stats.mp;
		}

		// handle status effects
		List<Command> r = new ArrayList<>();
		statusBuff.ifPresent(s -> r.addAll(s.doBeforeTurn(battle, this)));
		statusDebuff.ifPresent(s -> r.addAll(s.doBeforeTurn(battle, this)));

		statusBuff.ifPresent(s -> {
			if (s.canRemoveNow()) this.statusBuff = Optional.empty();
		});
		statusDebuff.ifPresent(s -> {
			if (s.canRemoveNow()) this.statusDebuff = Optional.empty();
		});

		return r;
	}

	/**
	 * Remove all status effects
	 * */
	public void cleanse() {
		statusBuff = Optional.empty();
		statusDebuff = Optional.empty();
	}

	@Override public Stats getStats() {
		return baseStats;
	}

	@Override public MapPoint getPos() {
		return pos;
	}

	@Override public double getAttackBuff() {
		double buff = statusBuff.map(s -> s.getAttackBuff()).orElse(0.0);
		double debuff = statusDebuff.map(s -> s.getAttackBuff()).orElse(0.0);
		return buff - debuff;
	}

	@Override public double getDefenceBuff() {
		double buff = statusBuff.map(s -> s.getDefenceBuff()).orElse(0.0);
		double debuff = statusDebuff.map(s -> s.getDefenceBuff()).orElse(0.0);
		return buff - debuff;
	}

	public double getChanceBuff() {
		return statusBuff.map(s -> s.getChanceBuff()).orElse(0.0);
	}

	@Override public void dealDamage(int damage) {
		hp = Math.max(0, hp - damage);
		System.err.println("HP: " + hp + " after damage " + damage);
	}

	@Override public void applyStatus(StatusEffect status) {
		final StatusEffectInfo info = status.getInfo();
		if (info.kind == StatusEffectKind.BUFF) {
			statusBuff = Optional.of(status);
		} else {
			statusDebuff = Optional.of(status);
		}
	}

	@Override public boolean isPushable() {
		return true;
	}

	@Override public boolean isEnemyOf(Character c) {
		return player != c.player;
	}

	@Override public boolean reap() {return false;}

	@Override public SpriteInfo getSprite() {return sprite;}

	@Override public boolean blocksSpace(Player player) {
		return true;
	}

	@Override public boolean blocksPath(Player player) {
		return !(hp == 0 || this.player == player);
	}
}

