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
import inthezone.battle.data.StatusEffectType;
import inthezone.battle.status.Debilitated;
import inthezone.battle.status.Imprisoned;
import inthezone.battle.status.Silenced;
import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.Stunned;
import inthezone.battle.status.Vampirism;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import javafx.scene.image.Image;
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

	private Character(
		int id,
		String name,
		Player player,
		SpriteInfo sprite,
		Image portrait,
		Collection<Ability> abilities,
		Ability basicAbility,
		Stats baseStats,
		Optional<StatusEffect> statusBuff,
		Optional<StatusEffect> statusDebuff,
		boolean hasMana,
		boolean hasCover,
		MapPoint pos,
		int ap,
		int mp,
		int hp
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
			portrait,
			abilities,
			basicAbility,
			baseStats,
			statusBuff,
			statusDebuff,
			hasMana,
			hasCover,
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
	public void pointsBuff(int ap, int mp, int hp) {
		if (isDead()) return;

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
	public void moveTo(MapPoint p, int mp, boolean hasMana) {
		if (statusDebuff.map(s -> s instanceof Imprisoned).orElse(false)) {
			return;
		}

		this.hasMana = hasMana;
		this.mp -= mp;
		if (this.mp < 0) this.mp = 0;
		this.pos = p;
	}

	public void useAbility(Ability ability) {
		if (ability.subsequentLevel == 0) {
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
		ap = 0;
		mp = 0;
		statusBuff = Optional.empty();
		statusDebuff = Optional.empty();
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
		final List<Command> r = new ArrayList<>();
		if (this.player != player) return r;

		final Stats stats = getStats();
		ap = isDead()? 0 : stats.ap;
		mp = isDead()? 0 : stats.mp;

		// reset the point buff/debuff checks
		buffedAP = false;
		buffedMP = false;
		debuffedAP = false;
		debuffedMP = false;

		// handle status effects (but not feared or panicked)
		hasCover = false;
		statusBuff.ifPresent(s -> {
			r.addAll(s.doBeforeTurn(battle, this));
			if (s.canRemoveNow()) this.statusBuff = Optional.empty();
		});

		if (statusDebuff.map(x -> !x.info.type.causesMovement()).orElse(false)) {
			statusDebuff.ifPresent(s -> {
				r.addAll(s.doBeforeTurn(battle, this));
				lastDebuff = s;
				if (s.canRemoveNow()) this.statusDebuff = Optional.empty();
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
				lastDebuff = s;
				if (s.canRemoveNow()) this.statusDebuff = Optional.empty();
			});
		}

		return r;
	}

	// need to hold on to the last debuff, because we might need to know what it
	// was even after it's removed.
	private StatusEffect lastDebuff = null;

	/**
	 * Continue the turn reset, after it was interrupted by a trigger.
	 * */
	public List<Command> continueTurnReset(Battle battle) {
		final List<Command> r = new ArrayList<>();
		statusDebuff.ifPresent(s -> {
			if (s.isBeforeTurnExhaustive()) r.addAll(s.doBeforeTurn(battle, this));
		});

		if (!statusDebuff.isPresent()) {
			if (lastDebuff != null && lastDebuff.isBeforeTurnExhaustive())
			 r.addAll(lastDebuff.doBeforeTurn(battle, this));
		}
		return r;
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

	@Override public void dealDamage(int damage) {
		if (hasCover || isDead()) return;
		hp = Math.min(getStats().hp, Math.max(0, hp - damage));
		if (hp == 0) kill();
		System.err.println("HP: " + hp + " after damage " + damage);
	}

	@Override public void defuse() { return; }

	@Override public void cleanse() {
		lastDebuff = null;
		statusBuff = Optional.empty();
		statusDebuff = Optional.empty();
		clampPoints();
	}

	@Override public void revive() {
		hp = Math.max(1, hp);
	}

	@Override public void purge() { return; }

	@Override public void applyStatus(Battle battle, StatusEffect status) {
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

	@Override public boolean isPushable() { return true; }

	@Override public boolean isEnemyOf(Character c) {
		return player != c.player;
	}

	@Override public boolean reap() { return false; }

	@Override public SpriteInfo getSprite() { return sprite; }

	@Override public boolean blocksSpace() { return true; }

	@Override public boolean blocksPath(Player player) {
		return !(isDead() || this.player == player);
	}
}

