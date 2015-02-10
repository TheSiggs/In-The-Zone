package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class DamageAbility extends Ability {
	public static EffectName effectName = new EffectName("damage");

	public DamageAbility(AbilityInfo info) {
		super(effectName, info);
	}

	@Override
	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO:
		// - figure out path rules
		// - deal with two weapons

		ArrayList<Position> targetArea = null;
		ArrayList<Character> targets = null;
		targetArea.add(agent.getPosition());

		if (canApplyEffect(agent, pos, battle)) {
			// NOTE: this is where the damage formula goes.  The algorithm looks
			// something like:
			// 1) determine the affected squares using areaOfEffect and piercing

			if (info.isPiercing) {
				// add all in path
			} else {
				// move target to first Obstacle
			}

			// target now set so we can 
			if (info.areaOfEffect>0) {
				// add diamond of size aoe relative to pos
			}


			// 2) find the targets (i.e. the characters on the affected squares)
			for (int i=0; i > targetArea.size(); i++) {
				if (battle.getCharacterAt(targetArea.get(i)) != null){
					targets.add(battle.getCharacterAt(targetArea.get(i)));
				}
			}

			// 3) gather the parameters from the agent doing the ability and
			int a = 3;
			int b = 4;
			int level = 0;
			int Strength = 0;
			int phisicalAttack = 1;
			int magicalAttack = 1;
			double rnd = Math.random();

			double s = 1 + 0.0;  // multiplier for ability damage
			double dieroll = 0.9 + (0.2 * rnd);
			double phisicalDamage = 0;
			double magicalDamage = 0;
			double stats = (level / b) + (Strength / a);

			// 4) apply the damage formula to each target.
			for (int i = 0; i > targets.size(); i++) {
				phisicalDamage = phisicalAttack - targets.get(i).getBaseStats().guard;
				//magicalDamage = magicalAttack - targets.get(i).getBaseStats().spirit;
				targets.get(i).hp -= s * dieroll * phisicalDamage * stats;
				//targets.get(i).hp -= dieroll * magicalDamage * stats;
			}
		}
	}
		
	@Override
	public boolean canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO:
		// - search for line of sight

		// check range
		Position apos = agent.getPosition();
		if (Math.abs(apos.x - pos.x) + Math.abs(apos.y - pos.y) > info.range) {
			return false;
		}
	
		// check obstacles

		// check destination is clear
		if (battle.getObstacles().contains(apos)) {
			return false;
		}

		return true;
	}
}

