package inthezone.battle;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.Range;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffectFactory;
import java.util.Optional;

public class Ability {
	public final AbilityInfo info;

	public final String rootName;
	public final int subsequentLevel;
	public final int recursionLevel;

	public Ability(AbilityInfo info) {
		this.info = info;
		this.rootName = info.name;
		this.subsequentLevel = 0;
		this.recursionLevel = 0;
	}

	private Ability(
		AbilityInfo info, String name,
		int subsequentLevel, int recursionLevel
	) {
		this.info = info;
		this.rootName = name;
		this.subsequentLevel = subsequentLevel;
		this.recursionLevel = recursionLevel;
	}

	public Ability getMana() {
		return info.mana.map(m -> new Ability(m, rootName, 0, 0)).orElse(this);
	}

	public Optional<Ability> getSubsequent() {
		return info.subsequent.map(i ->
			new Ability(i, rootName, subsequentLevel + 1, 0));
	}

	public Optional<Ability> getRecursion() {
		if (recursionLevel < info.recursion) {
			return Optional.of(new Ability(
				info, rootName, subsequentLevel, recursionLevel + 1));
		} else {
			return Optional.empty();
		}
	}

	public Optional<Ability> getNext(
		boolean mana, int subsequentLevel, int recursionLevel
	) {
		Optional<Ability> r = Optional.of(this);

		if (mana) {
			Optional<Ability> manaAbility =
				r.flatMap(a -> a.info.mana.map(aa -> new Ability(aa)));
			if (manaAbility.isPresent()) r = manaAbility;
		}

		for (int i = 0; i < subsequentLevel; i++) {
			r = r.flatMap(a -> getSubsequent());
		}

		return r.flatMap(a -> {
				if (recursionLevel <= a.info.recursion) {
					return Optional.of(new Ability(
						a.info, rootName, a.subsequentLevel, recursionLevel));
				} else {
					return Optional.empty();
				}
			});
	}

	/**
	 * Determine if this ability can be applied to a particular target.
	 * */
	public boolean canTarget(Character agent, Targetable target) {
		return
			(info.range.targetMode.self && target.getPos().equals(agent.getPos())) ||
			(info.range.targetMode.enemies && target.isEnemyOf(agent)) ||
			(info.range.targetMode.allies && !target.isEnemyOf(agent));
	}

	private final double const_a = 3;
	private final double const_b = 4;
	private final double const_h = 12;
	private final double const_i = 15;

	public double damageFormula(
		double attackBuff, double defenceBuff, Stats a, Stats t
	) {
		double q = info.type == AbilityType.BASIC? 1 : info.eff;
		return
			q * (1 + attackBuff - defenceBuff) *
			(0.9 + (0.2 * Math.random())) *
			(((double) a.attack) - ((double) t.defence)) *
			((const_b * ((double) a.power)) / const_a);
	}

	public double healingFormula(
		double attackBuff, double defenceBuff, Stats a, Stats t
	) {
		return (info.eff * const_h *
			(0.9 + (0.2 * Math.random())) *
			(double) t.hp) / const_i;
	}

	public DamageToTarget computeDamageToTarget(
		Character a, Targetable t
	) {
		Stats aStats = a.getStats();
		Stats tStats = t.getStats();

		double damage = info.heal?
			healingFormula(a.getAttackBuff(), t.getDefenceBuff(), aStats, tStats) :
			damageFormula(a.getAttackBuff(), t.getDefenceBuff(), aStats, tStats);
		int rdamage = (int) Math.ceil(damage);

		double chance = info.chance + a.getChanceBuff();

		return new DamageToTarget(t.getPos(), rdamage,
			imposeEffect(chance, info.statusEffect
				.map(i -> StatusEffectFactory.getEffect(i, rdamage, a))
				.orElse(null)),
			Math.random() < chance, Math.random() < chance);
	}

	private <T> Optional<T> imposeEffect(double p, T effect) {
		return Optional.ofNullable(Math.random() < p ? effect : null);
	}
}

