package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class TeleportAbility extends Ability {
	public static EffectName effectName = new EffectName("teleport");

	public TeleportAbility(AbilityInfo info) {
		super(effectName, info);
	}

	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO: implement this method
		return;
	}
		
	public void canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO: implement this method
		return;
	}
}

