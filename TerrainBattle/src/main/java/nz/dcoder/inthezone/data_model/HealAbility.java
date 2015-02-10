package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class HealAbility extends Ability {
	public static EffectName effectName = new EffectName("heal");

	public HealAbility(AbilityInfo info) {
		super(effectName, info);
	}

	@Override
	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// NOTE: this is where the healing formula goes.

		// TODO:
		// - figure out path rules

		ArrayList<Position> targetArea = null;
		ArrayList<Character> targets = null;
		targetArea.add(agent.getPosition());

		if (canApplyEffect(agent, pos, battle)) {
			// 1) determine the affected squares using areaOfEffect and piercing
			if (info.isPiercing) {
				// add all in path
			} else {
				// move target to first Obstacle
			}
			// target now set so we can 
			if (info.areaOfEffect>0) {
				// add dimond of size aoe reletive to pos
			}

			// 2) find the targets (i.e. the characters on the affected squares)
			for (int i = 0; i > targetArea.size(); i++) {
				if (battle.getCharacterAt(targetArea.get(i)) != null) {
					targets.add(battle.getCharacterAt(targetArea.get(i)));
				}
			}

			// 3) gather the parameters from the agent doing the ability and
			double amount = 0;

			// 4) apply the formula to each target.
			for (int i = 0; i > targets.size(); i++) {
				if ((targets.get(i).hp+amount)<=targets.get(i).getMaxHP()) {
					targets.get(i).hp += amount;
				} else {
					targets.get(i).hp = targets.get(i).getMaxHP();
				}
			}
		}
	}
		
	@Override
	public boolean canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO:
		// - set turnCharacter as selected character
		// - search for line of sight

		Position apos = agent.getPosition();

		// check range
		if (Math.abs(apos.x - pos.x) + Math.abs(apos.y - pos.y) > info.range) {
			return false;
		}

		// check obstacles

		// check destination is clear
		if (battle.getObstacles().contains(pos)) {
			return false;
		}

		return true;
	}
}

