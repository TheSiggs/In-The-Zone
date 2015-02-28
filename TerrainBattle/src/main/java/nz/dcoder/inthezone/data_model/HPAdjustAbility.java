package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class HPAdjustAbility extends Ability {
	public static EffectName effectName = new EffectName("hpAdjust");

	public HPAdjustAbility(AbilityInfo info) {
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
		final Collection<Character> characterTargets = affected.stream()
				.map(p -> battle.getCharacterAt(p))
				.filter(c -> c != null)
				.collect(Collectors.toList());

		final Collection<BattleObject> objectTargets = affected.stream()
				.map(p -> battle.getObjectAt(p))
				.filter(o -> o != null && o.isAttackable)
				.collect(Collectors.toList());

		int m = (int) info.s;

		// 4) apply the formula to each target.
		for (Character c : characterTargets) {
			c.hp += m;
			if (c.hp > c.getMaxHP()) c.hp = c.getMaxHP();
			else {
				c.hp = 0;
				battle.kill(c);
			}
		}

		// for abilities that do damage, hit target objects also
		if (m < 0) {
			for (BattleObject o : objectTargets) {
				o.hitsRemaining -= 1;
				if (o.hitsRemaining < 0) o.hitsRemaining = 0;
			}
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

