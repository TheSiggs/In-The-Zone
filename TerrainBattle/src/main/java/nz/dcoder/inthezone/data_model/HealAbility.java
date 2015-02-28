package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nz.dcoder.inthezone.data_model.formulas.FormulaException;
import nz.dcoder.inthezone.data_model.formulas.HealingFormula;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class HealAbility extends Ability {
	public static EffectName effectName = new EffectName("heal");

	private final HealingFormula healing = new HealingFormula();

	public HealAbility(AbilityInfo info) {
		super(effectName, info);
	}

	@Override
	public void applyEffect(
		CanDoAbility agent, Position target, Battle battle
	) {
		if (!canApplyEffect(agent, target, battle)) return;

		// 1) determine the affected squares using areaOfEffect and piercing
		final Collection<Position> affected =
			getAffectedArea(agent.getPosition(), target, battle);

		// 2) find the targets (i.e. the characters on the affected squares)
		final List<Character> targets = affected.stream()
			.map(p -> battle.getCharacterAt(p))
			.filter(c -> c != null).collect(Collectors.toList());

		// 3) gather the parameters from the agent doing the ability and
		BaseStats stats = agent.getBaseStats();
		Equipment weapon = agent.getWeapon();

		// 4) apply the formula to each target.
		for (Character c : targets) {
			BaseStats tstats = c.getBaseStats();

			healing.setVariable("c", info.s);
			healing.setVariable("agent_strength", stats.strength);
			healing.setVariable("agent_intelligence", stats.intelligence);
			healing.setVariable("agent_level", agent.getLevel());
			healing.setVariable("agent_physicalWeapon", weapon.physical);
			healing.setVariable("agent_magicalWeapon", weapon.magical);
			healing.setVariable("target_guard", tstats.guard);
			healing.setVariable("target_spirit", tstats.spirit);
			healing.setVariable("target_level", c.getLevel());

			try {
				c.hp += (int) healing.evaluate();
			} catch (FormulaException e) {
				throw new RuntimeException(
					"Runtime error evaluating healing formula: " + e.getMessage(), e);
			}

			if (c.hp > c.getMaxHP()) c.hp = c.getMaxHP();
		}
	}

	@Override
	public Collection<Position> getTargets(
		Collection<Position> affected, Battle battle
	) {
		return affected.stream()
			.map(p -> battle.getCharacterAt(p))
			.filter(c -> c != null)
			.map(c -> c.position)
			.collect(Collectors.toList());
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
		if (info.requiresLOS && !hasLineOfSight(apos, target, battle)) {
			return false;
		}

		// restrict piercing to cardinal directions
		if (info.isPiercing && apos.x != target.x && apos.y != target.y) {
			return false;
		}

		return true;
	}
}

