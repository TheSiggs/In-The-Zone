package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

public class DamageAbility extends Ability {
	public DamageAbility(AbilityInfo info) {
		super(new EffectName("damage"), info);
	}

	public void applyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO: implement this method
		// NOTE: this is where the damage formula goes.  The algorithm looks
		// something like:
		// 1) determine the affected squares using areaOfEffect and piercing
		// 2) find the targets (i.e. the characters on the affected squares)
		// 3) gather the parameters from the agent doing the ability and
		//    the targets of the ability
		// 4) apply the damage formula to each target.  In the case of
		//    two weapons we could apply it twice, or apply it once
		//    using the sum of the attack strength of the two weapons.
		return;
	}
		
	public void canApplyEffect(CanDoAbility agent, Position pos, Battle battle) {
		// TODO: implement this method
		return;
	}
}

