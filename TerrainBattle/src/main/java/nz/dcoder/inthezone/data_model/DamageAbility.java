package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.LineOfSight;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DamageAbility extends Ability {
	public static EffectName effectName = new EffectName("damage");

	public DamageAbility(AbilityInfo info) {
		super(effectName, info);
	}

	static final private double a = 3;
	static final private double b = 4;

	@Override
	public void applyEffect(CanDoAbility agent, Position target, Battle battle) {
		if (!canApplyEffect(agent, target, battle)) return;

		// 1) determine the affected squares using areaOfEffect and piercing
		final Collection<Position> affected =
			getAffectedArea(agent.getPosition(), target, battle);

		// 2) find the targets (i.e. the characters on the affected squares)
		final Collection<Character> characterTargets = affected.stream()
				.map(p -> battle.getCharacterAt(p))
				.filter(c -> c != null)
				.collect(Collectors.toList());

		final Collection<BattleObject> objectTargets = affected.stream()
				.map(p -> battle.getObjectAt(p))
				.filter(o -> o != null && o.isAttackable)
				.collect(Collectors.toList());

		// 3) gather the parameters from the agent doing the ability and ...
		double dieroll = 0.9 + (0.2 * Math.random());
		BaseStats stats = agent.getBaseStats();
		double s = 1; // TODO: what is s
		double physicalMod =
			((double) agent.getLevel() / b) + ((double) stats.strength / a);
		double magicalMod =
			((double) agent.getLevel() / b) + ((double) stats.intelligence / a);

		Equipment weapon = agent.getWeapons().iterator().next();
	
		// 4) apply the damage formula to each target.
		for (Character c : characterTargets) {
			// TODO: magical damage, physical damage, or both?
			// TODO: compute defence stats properly
			double physicalDamage = weapon.physical - c.getBaseStats().guard;
			double magicalDamage = weapon.magical - c.getBaseStats().spirit;

			c.hp -= s * dieroll * physicalDamage * physicalMod;
			c.hp -= dieroll * magicalDamage * magicalMod;
			if (c.hp < 0) battle.kill(c);
		}

		for (BattleObject o : objectTargets) {
			o.hitsRemaining -= 1;
			if (o.hitsRemaining < 0) o.hitsRemaining = 0;
		}
	}

	@Override
	public boolean canApplyEffect(
		CanDoAbility agent, Position target, Battle battle
	) {
		// check range
		Position apos = agent.getPosition();
		if (Math.abs(apos.x - target.x) + Math.abs(apos.y - target.y) > info.range) {
			return false;
		}
	
		// check LOS
		if (!info.canPassObstacles && !hasLineOfSight(apos, target, battle)) {
			return false;
		}

		return true;
	}
}

