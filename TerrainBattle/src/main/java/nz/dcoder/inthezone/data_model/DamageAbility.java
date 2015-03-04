package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nz.dcoder.inthezone.data_model.formulas.Formula;
import nz.dcoder.inthezone.data_model.formulas.FormulaException;
import nz.dcoder.inthezone.data_model.formulas.MagicalDamage;
import nz.dcoder.inthezone.data_model.formulas.PhysicalDamage;
import nz.dcoder.inthezone.data_model.pure.AbilityClass;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.LineOfSight;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DamageAbility extends Ability {
	public static EffectName effectName = new EffectName("damage");

	private final PhysicalDamage physD = new PhysicalDamage();
	private final MagicalDamage magD = new MagicalDamage();

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
		BaseStats stats = agent.getBaseStats();
		Equipment weapon = agent.getWeapon();
	
		// 4) apply the damage formula to each target.
		for (Character c : characterTargets) {
			BaseStats tstats = c.getBaseStats();
			Collection<Equipment> armour = c.getArmour();

			int physDef = armour.stream()
				.collect(Collectors.summingInt(a -> a.physical));
			int magDef = armour.stream()
				.collect(Collectors.summingInt(a -> a.magical));

			Formula d;
			if (info.aClass == AbilityClass.PHYSICAL) {
				d = physD;
			} else if (info.aClass == AbilityClass.MAGICAL) {
				d = magD;
			} else {
				throw new RuntimeException(
					"Don't know how to do damage with an ability of class "
					+ info.aClass.toString());
			}

			d.setVariable("s", info.s);
			d.setVariable("agent_strength", stats.strength);
			d.setVariable("agent_intelligence", stats.intelligence);
			d.setVariable("agent_level", agent.getLevel());
			d.setVariable("agent_physicalWeapon", weapon.physical);
			d.setVariable("agent_magicalWeapon", weapon.magical);
			d.setVariable("target_guard", tstats.guard);
			d.setVariable("target_spirit", tstats.spirit);
			d.setVariable("target_level", c.getLevel());
			d.setVariable("target_physicalArmour", physDef);
			d.setVariable("target_magicalArmour", magDef);

			try {
				int damage = (int) d.evaluate();
				System.err.println("damage: " + damage);
				c.hp -= damage;
			} catch (FormulaException e) {
				throw new RuntimeException(
					"Error evaluating damage formula: " + e.getMessage(), e);
			}

			if (c.hp < 0) {
				c.hp = 0;
				battle.kill(c);
			}
		}

		for (BattleObject o : objectTargets) {
			o.hitsRemaining -= 1;
			if (o.hitsRemaining < 0) o.hitsRemaining = 0;
			battle.grimReaper();
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

		// You can't attack yourself
		if (apos.equals(target)) {
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

