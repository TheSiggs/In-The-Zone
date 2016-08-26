package inthezone.battle;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.Range;
import inthezone.battle.data.Stats;
import java.util.Optional;

public class Ability {
	public final AbilityInfo info;

	public final boolean isSubsequent;
	public final int recursionLevel;

	public Ability(AbilityInfo info) {
		this.info = info;
		this.isSubsequent = false;
		this.recursionLevel = 0;
	}

	private Ability(
		AbilityInfo info, boolean isSubsequent, int recursionLevel
	) {
		this.info = info;
		this.isSubsequent = isSubsequent;
		this.recursionLevel = recursionLevel;
	}

	public Optional<Ability> getSubsequent() {
		return info.subsequent.map(i -> new Ability(i, true, 0));
	}

	public Optional<Ability> getNextRecursion() {
		if (recursionLevel < info.recursion) {
			return Optional.of(new Ability(info, isSubsequent, recursionLevel + 1));
		} else {
			return Optional.empty();
		}
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

		return new DamageToTarget(t.getPos(), (int) Math.ceil(damage),
			imposeEffect(info.chance, info.statusEffect.orElse(null)),
			imposeEffect(info.chance, info.instantBefore.orElse(null)),
			imposeEffect(info.chance, info.instantAfter.orElse(null)));
	}

	private <T> T imposeEffect(double p, T effect) {
		return Math.random() < p ? effect : null;
	}
}

