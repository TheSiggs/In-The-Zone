package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class PushAbility extends Ability {
	public static EffectName effectName = new EffectName("push");

	public PushAbility(AbilityInfo info) {
		super(effectName, info);
	}

	private Position getPushDestination(
		Position agentPosition, Position target
	) {
		int dx = target.x - agentPosition.x;
		int dy = target.y - agentPosition.y;

		if (dx > 0) return target.add(new Position(1, 0));
		else if (dx < 0) return target.add(new Position(-1, 0));
		else if (dy > 0) return target.add(new Position(0, 1));
		else if (dy < 0) return target.add(new Position(0, -1));
		else return agentPosition;
	}

	@Override
	public Collection<Position> getAffectedArea(
		Position agentPosition, Position target, Battle battle
	) {
		List<Position> affected = new ArrayList<Position>();
		affected.add(target);
		affected.add(getPushDestination(agentPosition, target));
		return affected;
	}

	@Override
	public Collection<Position> getTargets(
		Collection<Position> affected, Battle battle
	) {
		List<Position> targets = new ArrayList<Position>();
		// WARNING: assumes that getAffectedArea puts the target square first
		targets.add(affected.iterator().next());
		return targets;
	}

	@Override
	public void applyEffect(CanDoAbility agent, Position target, Battle battle) {
		if (canApplyEffect(agent, target, battle)) {
			Position apos = agent.getPosition();
			Position pushDestination = getPushDestination(apos, target);
			battle.getObjectAt(target).position = pushDestination;
			battle.getCharacterAt(apos).position = target;
		}
	}

	@Override
	public boolean canApplyEffect(
		CanDoAbility agent, Position target, Battle battle
	) {
		Position apos = agent.getPosition();
		System.out.println("checking push");

		// check range
		if (Math.abs(apos.x - target.x) + Math.abs(apos.y - target.y) > info.range) {
			return false;
		}

		// check direction
		if (!(apos.x == target.x || apos.y == target.y)) {
			return false;
		}

		// check that there is something we can push
		BattleObject obj = battle.getObjectAt(target);
		if (obj == null || !obj.isPushable) {
			return false;
		}

		// check the push is not blocked
		Position pushDestination = getPushDestination(apos, target);
		if (battle.getOccupiedPositions().contains(pushDestination)) {
			return false;
		}

		return true;
	}
}

