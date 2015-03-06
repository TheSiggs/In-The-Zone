package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class TeleportAbility extends Ability {
	public static EffectName effectName = new EffectName("teleport");

	public TeleportAbility(AbilityInfo info) {
		super(effectName, info);
	}

	@Override
	public Collection<Position> getAffectedArea(
		Position agentPosition, Position target, Battle battle
	) {
		List<Position> affected = new ArrayList<Position>();
		affected.add(target);
		return affected;
	}

	@Override
	public Collection<Position> getTargets(
		Collection<Position> affected, Battle battle
	) {
		return affected;
	}


	@Override
	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		if (canApplyEffect(agent, pos, battle)) {
			battle.getCharacterAt(agent.getPosition()).position = pos;
		}
	}

	@Override
	public boolean canApplyEffect(
		CanDoAbility agent, Position pos, Battle battle
	) {
		Position apos = agent.getPosition();

		// check range
		if (Math.abs(apos.x - pos.x) + Math.abs(apos.y - pos.y) > info.range) {
			return false;
		}

		Collection<Position> obstacles = battle.terrain.getObstacles();

		if (
			battle.getOccupiedPositions().contains(pos) ||
			obstacles.contains(pos)
		) {
			return false;
		}

		return true;
	}
}

