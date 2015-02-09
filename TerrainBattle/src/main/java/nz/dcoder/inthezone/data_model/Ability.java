package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.pure.EffectName;

/**
 * Pairs an AbilityInfo with a means to apply the ability's effect.
 * */
abstract public class Ability {
	public final EffectName name;
	public final AbilityInfo info;

	public Ability(EffectName name, AbilityInfo info) {
		this.name = name;
		this.info = info;
	}

	public abstract void applyEffect(CanDoAbility agent, Position pos, Battle battle);
	public abstract boolean canApplyEffect(CanDoAbility agent, Position pos, Battle battle);
}

