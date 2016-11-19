package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.data.StatusEffectKind;
import inthezone.battle.status.Debilitated;
import inthezone.battle.status.Imprisoned;
import inthezone.battle.status.Silenced;
import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.Stunned;
import inthezone.battle.status.Vampirism;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character extends Targetable {
	public final int id; // a unique identifier that can be used to track this character
	public final String name;
 	public final Player player;
	public final SpriteInfo sprite;
	public final Collection<Ability> abilities;
	public final Ability basicAbility;
	private final Stats baseStats;

	private Optional<StatusEffect> statusBuff = Optional.empty();
	private Optional<StatusEffect> statusDebuff = Optional.empty();

	private boolean hasMana = false;
	private MapPoint pos = new MapPoint(0, 0);
	private int ap = 0;
	private int mp = 0;
	private int hp = 0;

	private Character(
		int id,
		String name,
		Player player,
		SpriteInfo sprite,
		Collection<Ability> abilities,
		Ability basicAbility,
		Stats baseStats,
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
	@Override public Character clone() {
		return new Character(
			id,
			name,
			player,
			sprite,
			abilities,
			basicAbility,
			baseStats,
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
		this.ap = baseStats.ap;
		this.mp = baseStats.mp;
		this.hp = baseStats.hp;
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
		return baseStats.hp;
	}

	@Override public boolean hasMana() {
		return hasMana;
	}

	public boolean isVampiric() {
		return statusBuff.map(s -> s instanceof Vampirism).orElse(false);
	}

	public boolean isStunned() {
		return statusBuff.map(s -> s instanceof Stunned).orElse(false);
	}

	public boolean isAbilityBlocked(Ability a) {
		if (a.info.type == AbilityType.SKILL) {
			return statusDebuff.map(s -> s instanceof Debilitated).orElse(false);
		} else if (a.info.type == AbilityType.SPELL) {
			return statusDebuff.map(s -> s instanceof Silenced).orElse(false);
		} else {
			return statusDebuff.map(s -> s instanceof Stunned).orElse(false);
		}
	}

	public Optional<StatusEffect> getStatusBuff() {
		return statusBuff;
	}

	public Optional<StatusEffect> getStatusDebuff() {
		return statusDebuff;
	}

	/**
	 * Buff or debuff this character's points. Should be done after the turn reset.
	 * */
	public void pointsBuff(int ap, int mp, int hp) {
		this.ap += ap;
		this.mp += mp;
		this.hp += hp;
		if (this.hp > baseStats.hp) this.hp = baseStats.hp;
		if (this.ap < 0) ap = 0;
		if (this.mp < 0) mp = 0;
		if (this.hp < 0) hp = 0;
	}

	/**
	 * Move the character spending movement points
	 * */
	public void moveTo(MapPoint p, boolean hasMana) {
		if (statusDebuff.map(s -> s instanceof Imprisoned).orElse(false)) {
			return;
		}

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

	public void useItem(Item item) {
		ap = Math.max(0, ap - 1);
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
		if (statusDebuff.map(s -> s instanceof Imprisoned).orElse(false)) {
			return;
		}

		this.hasMana = hasMana;
		this.pos = p;
	}

	/**
	 * Call this at the start of each turn.
	 * @param player The player who's turn is starting
	 * */
	public List<Command> turnReset(Battle battle, Player player) {
		List<Command> r = new ArrayList<>();
		if (this.player != player) return r;

		Stats stats = getStats();
		ap = stats.ap;
		mp = stats.mp;

		// trigger the current zone (if there is one)
		currentZone = Optional.empty();
		r.addAll(triggerZone(battle.battleState));

		// handle status effects
		statusBuff.ifPresent(s -> r.addAll(s.doBeforeTurn(battle, this)));
		statusDebuff.ifPresent(s -> r.addAll(s.doBeforeTurn(battle, this)));

		statusBuff.ifPresent(s -> {
			if (s.canRemoveNow()) this.statusBuff = Optional.empty();
		});
		statusDebuff.ifPresent(s -> {
			lastDebuff = s;
			if (s.canRemoveNow()) this.statusDebuff = Optional.empty();
		});

		return r;
	}

	// need to hold on to the last debuff, because we might need to know what it
	// was even after it's removed.
	private StatusEffect lastDebuff = null;

	/**
	 * Continue the turn reset, after it was interrupted by a trigger.
	 * */
	public List<Command> continueTurnReset(Battle battle) {
		List<Command> r = new ArrayList<>();
		statusDebuff.ifPresent(s -> {
			if (s.isBeforeTurnExhaustive()) r.addAll(s.doBeforeTurn(battle, this));
		});

		if (!statusDebuff.isPresent()) {
			if (lastDebuff != null && lastDebuff.isBeforeTurnExhaustive())
			 r.addAll(lastDebuff.doBeforeTurn(battle, this));
		}
		return r;
	}

	/**
	 * Remove all status effects
	 * */
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

	@Override public double getChanceBuff() {
		return statusBuff.map(s -> s.getChanceBuff()).orElse(0.0);
	}

	@Override public void dealDamage(int damage) {
		hp = Math.min(baseStats.hp, Math.max(0, hp - damage));
		System.err.println("HP: " + hp + " after damage " + damage);
	}

	@Override public void defuse() {return;}

	@Override public void cleanse() {
		statusBuff = Optional.empty();
		statusDebuff = Optional.empty();
	}

	@Override public void purge() {return;}

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

