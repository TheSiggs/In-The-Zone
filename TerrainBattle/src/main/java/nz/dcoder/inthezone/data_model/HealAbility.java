package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class HealAbility extends Ability {
	public static EffectName effectName = new EffectName("heal");

	public HealAbility(AbilityInfo info) {
		super(effectName, info);
	}

	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO: implement this method
		// NOTE: this is where the healing formula goes.  It doesn't have to be
		// the same as the damage formula.
		return;
	}
		
	public void canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO: implement this method
		return;
	}
}

