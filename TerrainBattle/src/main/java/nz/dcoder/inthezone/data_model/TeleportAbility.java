package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class TeleportAbility extends Ability {
	public static EffectName effectName = new EffectName("teleport");

	public TeleportAbility(AbilityInfo info) {
		super(effectName, info);
	}

	@Override
	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		if (canApplyEffect(agent, pos, battle)) {
			battle.getCharacterAt(agent.getPosition()).position = pos;
		}
	}

	@Override
	public boolean canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
		Position apos = agent.getPosition();

		// check range
		if (Math.abs(apos.x - pos.x) + Math.abs(apos.y - pos.y) > info.range) {
			return false;
		}

		if (battle.getOccupiedPositions().contains(pos)) {
			return false;
		}

		return true;
	}
}

