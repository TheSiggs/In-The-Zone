package inthezone.battle;

import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.scene.image.Image;

import inthezone.battle.commands.Command;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.data.StatusEffectKind;
import inthezone.battle.data.StatusEffectType;
import inthezone.battle.status.Debilitated;
import inthezone.battle.status.FearedStatusEffect;
import inthezone.battle.status.Imprisoned;
import inthezone.battle.status.PanickedStatusEffect;
import inthezone.battle.status.Silenced;
import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.Stunned;
import inthezone.battle.status.Vampirism;

public class Character extends Targetable {
	public final int id; // a unique identifier that can be used to track this character
	public final String name;
 	public final Player player;
	public final SpriteInfo sprite;
	public final Image portrait;
	public final Collection<Ability> abilities = new ArrayList<>();
	public final Ability basicAbility;
	private final Stats baseStats;

	private Optional<StatusEffect> statusBuff = Optional.empty();
	private Optional<StatusEffect> statusDebuff = Optional.empty();

	private boolean hasMana = false;
	private boolean hasCover = false;
	private MapPoint pos = new MapPoint(0, 0);
	private int ap = 0;
	private int mp = 0;
	private int hp = 0;

	double revengeBonus = 0;
	public double getRevengeBonus() { return isDead()? 0 : revengeBonus; }

	private Character(
		final int id,
		final String name,
		final Player player,
		final SpriteInfo sprite,
		final Image portrait,
		final Collection<Ability> abilities,
		final Ability basicAbility,
		final Stats baseStats,
		final Optional<StatusEffect> statusBuff,
		final Optional<StatusEffect> statusDebuff,
		final boolean hasMana,
		final boolean hasCover,
		final MapPoint pos,
		final int ap,
		final int mp,
		final int hp,
		final double revengeBonus
	) {
		this.id = id;
		this.name = name;
		this.player = player;
		this.sprite = sprite;
		this.portrait = portrait;
		this.abilities.addAll(abilities);
		this.basicAbility = basicAbility;
		this.baseStats = baseStats;
		this.statusBuff = statusBuff;
		this.statusDebuff = statusDebuff;
		this.hasMana = hasMana;
		this.hasCover = hasCover;
		this.pos = pos;
		this.ap = ap;
		this.mp = mp;
		this.hp = hp;
		this.revengeBonus = revengeBonus;
	}

	@Override public CharacterFrozen freeze() {
		return new CharacterFrozen(this);
	}

	public Character(
		final CharacterProfile profile,
		final Player player,
		final boolean hasMana,
		final MapPoint pos,
		final int id
	) {
		this.id = id;
		this.name = profile.rootCharacter.name;
		this.player = player;
		this.sprite = player == Player.PLAYER_A?
			profile.rootCharacter.spriteA:
			profile.rootCharacter.spriteB;
		this.portrait = profile.rootCharacter.portrait;
		this.baseStats = profile.getBaseStats();
		this.abilities.addAll(profile.abilities.stream()
			.map(i -> new Ability(i)).collect(Collectors.toList()));
		this.basicAbility = new Ability(profile.basicAbility);
		this.hasMana = hasMana;
		this.pos = pos;
		this.ap = baseStats.ap;
		this.mp = baseStats.mp;
		this.hp = baseStats.hp;
	}

	public int getAP() { return ap; }
	public int getMP() { return mp; }
	public int getHP() { return hp; }
	public int getMaxHP() { return getStats().hp; }
	public boolean hasCover() { return hasCover; }
	@Override public boolean hasMana() { return hasMana; }

	public boolean isVampiric() {
		return statusBuff.map(s -> s instanceof Vampirism).orElse(false);
	}

	public boolean isStunned() {
		return statusDebuff.map(s -> s instanceof Stunned).orElse(false);
	}

	public boolean isImprisoned() {
		return statusDebuff.map(s -> s instanceof Imprisoned).orElse(false);
	}

	public boolean isPanicked() {
		return
			statusDebuff.map(s -> s instanceof PanickedStatusEffect).orElse(false);
	}

	public boolean isFeared() {
		return
			statusDebuff.map(s -> s instanceof FearedStatusEffect).orElse(false);
	}

	public boolean isAbilityBlocked(final Ability a) {
		return Character.isAbilityBlocked(
			this.isDead(), this.isStunned(),
			this.statusBuff, this.statusDebuff, a);
	}

	/**
	 * Determine if an ability can be used
	 * */
	public static boolean isAbilityBlocked(
		final boolean isDead, final boolean isStunned,
		final Optional<StatusEffect> buff, final Optional<StatusEffect> debuff,
		final Ability a
	) {
		if (isDead || isStunned) {
			return true;
		} else if (a.info.type == AbilityType.SKILL) {
			return buff.map(s -> s instanceof Debilitated).orElse(false);
		} else if (a.info.type == AbilityType.SPELL) {
			return debuff.map(s -> s instanceof Silenced).orElse(false);
		} else {
			return false;
		}
	}

	public Optional<StatusEffect> getStatusBuff() { return statusBuff; }

	public Optional<StatusEffect> getStatusDebuff() { return statusDebuff; }

	// Points can only be buffed/debuffed once per turn, so we need to track
	// whether or not that's happened.
	private boolean buffedAP = false;
	private boolean buffedMP = false;
	private boolean debuffedAP = false;
	private boolean debuffedMP = false;

	/**
	 * Buff or debuff this character's points.
	 * */
	public void pointsBuff(int ap, int mp, final int hp) {
		if (buffedAP && ap > 0) ap = 0;
		if (buffedMP && mp > 0) mp = 0;
		if (debuffedAP && ap < 0) ap = 0;
		if (debuffedMP && mp < 0) mp = 0;

		if (ap > 0) buffedAP = true;
		if (mp > 0) buffedMP = true;
		if (ap < 0) debuffedAP = true;
		if (mp < 0) debuffedMP = true;

		this.ap += ap;
		this.mp += mp;
		this.hp += hp;
		if (this.hp > getStats().hp) this.hp = getStats().hp;
		if (this.ap < 0) this.ap = 0;
		if (this.mp < 0) this.mp = 0;
		if (this.hp < 0) this.hp = 0;

		if (this.hp == 0) kill();
	}

	private void clampPoints() {
		final Stats stats = getStats();
		if (this.ap < 0) this.ap = 0;
		if (this.mp < 0) this.mp = 0;
		if (this.hp < 0) this.hp = 0;
		if (this.ap > stats.ap) this.ap = stats.ap;
		if (this.mp > stats.mp) this.mp = stats.mp;
		if (this.hp > stats.hp) this.hp = stats.hp;

		if (this.hp == 0) kill();
	}

	/**
	 * Move the character spending movement points
	 * */
	public void moveTo(final MapPoint p, final int mp, final boolean hasMana) {
		if (isDead() ||
			statusDebuff.map(s -> s instanceof Imprisoned).orElse(false))
		{
			return;
		}

		this.hasMana = hasMana;
		this.mp -= mp;
		if (this.mp < 0) this.mp = 0;
		this.pos = p;
	}

	public void useAbility(final Ability ability) {
		if (ability.subsequentLevel == 0) {
			ap -= ability.info.ap;
			mp -= ability.info.mp;
			if (ap < 0) ap = 0;
			if (mp < 0) mp = 0;
		}
	}

	public void useItem(final Item item) {
		ap = Math.max(0, ap - 1);
	}

	public void usePush() {
		ap -= 1;
		if (ap < 0) ap = 0;
	}

	public void kill() {
		hp = 0;
		statusBuff = Optional.empty();
		statusDebuff = Optional.empty();
	}

	@Override public boolean isDead() {
		return hp == 0;
	}

	public void teleport(final MapPoint p, final boolean hasMana) {
		if (isDead() ||
			statusDebuff.map(s -> s instanceof Imprisoned).orElse(false))
		{
			return;
		}

		this.hasMana = hasMana;
		this.pos = p;
	}

	public void push(final MapPoint p, final boolean hasMana) {
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
	public List<Command> turnReset(final Battle battle, final Player player) {
		final List<Command> r = new ArrayList<>();
		if (this.player != player) return r;

		// handle status effects (but not feared or panicked)
		statusBuff.ifPresent(s -> {
			r.addAll(s.doBeforeTurn(battle, this));
		});

		if (statusDebuff.map(x -> !x.info.type.causesMovement()).orElse(false)) {
			statusDebuff.ifPresent(s -> {
				r.addAll(s.doBeforeTurn(battle, this));
			});
		}

		clampPoints();

		// trigger the current zone (if there is one)
		currentZone = Optional.empty();
		r.addAll(triggerZone(battle.battleState));

		// handle feared and panicked
		if (statusDebuff.map(x -> x.info.type.causesMovement()).orElse(false)) {
			statusDebuff.ifPresent(s -> {
				r.addAll(s.doBeforeTurn(battle, this));
			});
		}

		return r;
	}

	/**
	 * Continue the turn reset, after it was interrupted by a trigger.
	 * */
	public List<Command> continueTurnReset(final Battle battle) {
		final List<Command> r = new ArrayList<>();
		statusDebuff.ifPresent(s -> {
			if (s.isBeforeTurnExhaustive()) r.addAll(s.doBeforeTurn(battle, this));
		});
		return r;
	}

	/**
	 * Update the status effects on this character
	 * */
	public void cleanupStatus(final Battle battle, final Player player) {
		statusBuff.ifPresent(s -> {
			if (s.canRemoveNow(battle.battleState.getTurnNumber()))
				statusBuff = Optional.empty();
		});
		statusDebuff.ifPresent(s -> {
			if (s.canRemoveNow(battle.battleState.getTurnNumber()))
				statusDebuff = Optional.empty();
		});

		if (this.player == player) {
			// reset the points
			final Stats stats = getStats();
			ap = stats.ap;
			mp = stats.mp;

			// reset the point buff/debuff checks
			buffedAP = false;
			buffedMP = false;
			debuffedAP = false;
			debuffedMP = false;

			// do the side status buff/debuff side-effects
			hasCover = false;
			statusBuff.ifPresent(s -> s.doBeforeTurn(battle, this));
			statusDebuff.ifPresent(s -> s.doBeforeTurn(battle, this));
			clampPoints();
		}
	}

	@Override public Stats getStats() {
		final Stats b = statusBuff.map(s -> s.getBaseStatsBuff()).orElse(new Stats());
		final Stats d = statusDebuff.map(s -> s.getBaseStatsBuff()).orElse(new Stats());
		return baseStats.add(b).add(d);
	}

	@Override public MapPoint getPos() { return pos; }

	@Override public double getAttackBuff() {
		final double buff = statusBuff.map(s -> s.getAttackBuff()).orElse(0.0);
		final double debuff = statusDebuff.map(s -> s.getAttackBuff()).orElse(0.0);
		return buff - debuff;
	}

	@Override public double getDefenceBuff() {
		final double buff = statusBuff.map(s -> s.getDefenceBuff()).orElse(0.0);
		final double debuff = statusDebuff.map(s -> s.getDefenceBuff()).orElse(0.0);
		return buff - debuff;
	}

	@Override public double getChanceBuff() {
		return statusBuff.map(s -> s.getChanceBuff()).orElse(0.0);
	}

	@Override public void dealDamage(final int damage) {
		if (hasCover || isDead()) return;
		hp = Math.min(getStats().hp, Math.max(0, hp - damage));
		if (hp == 0) kill();
		System.err.println("HP: " + hp + " after damage " + damage);
	}

	@Override public void defuse() { return; }

	@Override public void cleanse() {
		statusBuff = Optional.empty();
		statusDebuff = Optional.empty();
		clampPoints();
	}

	@Override public void revive() {
		hp = Math.max(1, hp);
	}

	@Override public void purge() { return; }

	@Override public void applyStatus(
		final Battle battle, final StatusEffect status
	) {
		if (isDead()) return;

		final StatusEffectInfo info = status.getInfo();

		if (info.type == StatusEffectType.COVER) {
			hasCover = true;

		} else if (info.kind == StatusEffectKind.BUFF) {
			if (!statusBuff.map(s -> s.info.equals(status.info)).orElse(false)) {
				statusBuff = Optional.empty();
				clampPoints();
				status.doNow(this);
				statusBuff = Optional.of(status);
			}

		} else {
			if (!statusDebuff.map(s -> s.info.equals(status.info)).orElse(false)) {
				statusDebuff = Optional.empty();
				clampPoints();
				status.doNow(this);
				statusDebuff = Optional.of(status);
			}
		}
	}

	@Override public boolean isPushable() { return !isImprisoned(); }

	@Override public boolean isEnemyOf(Character c) {
		return player != c.player;
	}

	@Override public boolean reap() { return false; }

	@Override public SpriteInfo getSprite() { return sprite; }

	@Override public boolean blocksSpace() { return true; }

	@Override public boolean blocksPath(final Player player) {
		return !(isDead() || this.player == player);
	}

	@Override public boolean isAffectedBy(final StatusEffectInfo status) {
		return true;
	}

	@Override public boolean isAffectedBy(final InstantEffectInfo instant) {
		final InstantEffectType t = instant.type;
		return
			t == InstantEffectType.PUSH && !isImprisoned() ||
			t == InstantEffectType.PULL && !isImprisoned() ||
			t == InstantEffectType.TELEPORT && !isImprisoned() ||
			t == InstantEffectType.MOVE && !isImprisoned() ||
			t == InstantEffectType.REVIVE ||
			t == InstantEffectType.CLEANSE;
	}
}

