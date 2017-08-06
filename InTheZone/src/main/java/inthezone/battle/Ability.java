package inthezone.battle;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.Range;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffectFactory;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.Optional;

public class Ability {
	public final AbilityInfo info;

	public final boolean isMana;
	public final String rootName;
	public final int subsequentLevel;

	@Override public String toString() {
		return "(ability " + rootName + ":" + info.name +
			", mana:" + isMana + ", subsequentLevel:" + subsequentLevel + ")";
	}

	public Ability(AbilityInfo info) {
		this.info = info;
		this.rootName = info.name;
		this.subsequentLevel = 0;
		this.isMana = false;
	}

	private Ability(
		final AbilityInfo info, final String name,
		final int subsequentLevel, final boolean isMana
	) {
		this.info = info;
		this.rootName = name;
		this.subsequentLevel = subsequentLevel;
		this.isMana = isMana;
	}

	public Ability getMana() {
		return info.mana.map(m -> new Ability(m, rootName, 0, true)).orElse(this);
	}

	public Optional<Ability> getSubsequent() {
		return info.subsequent.map(i ->
			new Ability(i, rootName, subsequentLevel + 1, isMana));
	}

	public Optional<Ability> getNext(
		final boolean mana, final int subsequentLevel
	) {
		Optional<Ability> r = Optional.of(this);

		if (mana) {
			final Optional<Ability> manaAbility =
				r.flatMap(a -> a.info.mana.map(aa ->
					new Ability(aa, rootName, 0, true)));
			if (manaAbility.isPresent()) r = manaAbility;
		}

		for (int i = 0; i < subsequentLevel; i++) {
			r = r.flatMap(a -> a.getSubsequent());
		}

		return r;
	}

	/**
	 * Determine if this ability can be applied to a particular target.
	 * @param agent The doer of the ability
	 * @param target The target of the ability
	 * @return true if agent can target the target, otherwise false.
	 * */
	public boolean canTarget(final Targetable agent, final Targetable target) {
		if (target.getPos().equals(agent.getPos())) {
			return info.range.targetMode.self;
		} else if (agent instanceof Character) {
			return
				(info.range.targetMode.enemies && target.isEnemyOf((Character) agent)) ||
				(info.range.targetMode.allies && !target.isEnemyOf((Character) agent));
		} else if (agent instanceof HasParentAgent) {
			return canTarget(((HasParentAgent) agent).getParent(), target);
		} else {
			return true;
		}
	}

	private static final double const_a = 20;
	private static final double const_b = 5;
	private static final double const_h = 12;
	private static final double const_i = 75;
	private static final double const_m = 0.5;
	private static final double const_f = 0.3;

	/**
	 * The damage formula.
	 * */
	public static double damageFormulaStatic(
		final double q,
		final double manaBonus,
		final double r,
		final double attackBuff,
		final double defenceBuff,
		final Stats a, Stats t
	) {
		return
			q * (1 + manaBonus + attackBuff - defenceBuff + r) *
			(0.9 + (0.2 * Math.random())) *
			(((double) a.attack) - ((double) t.defence)) *
			((const_b * ((double) a.power)) / const_a);
	}

	private double damageFormula(
		final boolean agentHasMana,
		final double r,
		final double attackBuff,
		final double defenceBuff,
		final Stats a, Stats t
	) {
		double q = info.type == AbilityType.BASIC? 1 : info.eff;
		double manaBonus = agentHasMana && !isMana? const_m : 0;
		return damageFormulaStatic(q, manaBonus, r, attackBuff, defenceBuff, a, t);
	}


	private double healingFormula(final boolean manaBonus, final double q, Stats t) {
		return (const_h * (q + (manaBonus? const_m : 0)) *
			(0.9 + (0.2 * Math.random())) *
			(double) t.hp) / const_i;
	}

	/**
	 * @param a Agent
	 * @param t Target
	 * @param r Revenge bonus
	 * */
	public DamageToTarget computeDamageToTarget(
		final Targetable a, final Targetable t, final MapPoint castFrom, final double r
	) {
		final Stats aStats = a.getStats();
		final Stats tStats = t.getStats();

		double damage = info.heal?
			healingFormula(a.hasMana(), info.eff, tStats) :
			damageFormula(a.hasMana(), r, a.getAttackBuff(), t.getDefenceBuff(), aStats, tStats);
		if (t.isDead() && info.heal) damage = 0; // healing doesn't affect the dead
		final int rdamage = ((int) Math.ceil(damage)) * (info.heal? -1 : 1);

		final double chance = info.chance + a.getChanceBuff();

		final Optional<Character> characterAgent =
			Optional.ofNullable(a instanceof Character? (Character) a : null);

		return new DamageToTarget(new Casting(castFrom, t.getPos()),
			t instanceof Trap, t instanceof Zone, rdamage,
			imposeEffect(chance, info.statusEffect
				.map(i -> StatusEffectFactory.getEffect(i, rdamage, characterAgent))
				.orElse(null)),
			Math.random() < chance, Math.random() < chance);
	}

	public DamageToTarget computeVampirismEffect(
		final BattleState battle, final Character a, final Collection<DamageToTarget> targets
	) {
		final long characterTargets = targets.stream().filter(d ->
			d.damage > 0 && battle.getCharacterAt(d.target.target).isPresent()
		).count();
		final double qh = ((double) characterTargets) * const_f * info.eff;
		final int damage =
			(int) (-1d * Math.ceil(healingFormula(a.hasMana(), qh, a.getStats())));

		return new DamageToTarget(new Casting(a.getPos(), a.getPos()),
			false, false, damage, Optional.empty(), false, false);
	}

	private <T> Optional<T> imposeEffect(final double p, final T effect) {
		return Optional.ofNullable(Math.random() < p ? effect : null);
	}
}

