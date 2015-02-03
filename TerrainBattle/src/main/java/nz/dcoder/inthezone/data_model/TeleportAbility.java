package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class TeleportAbility extends Ability {
	public TeleportAbility(AbilityInfo info) {
		super(new EffectName("teleport"), info);
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

