package nz.dcoder.inthezone.data_model.factories;

import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;
import nz.dcoder.inthezone.data_model.Ability;
import nz.dcoder.inthezone.data_model.AbilityInfo;
import nz.dcoder.inthezone.data_model.DamageAbility;
import nz.dcoder.inthezone.data_model.HealAbility;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.TeleportAbility;

public class AbilityFactory {
	private final Map<EffectName, Function<AbilityInfo, Ability>> effects;

	public AbilityFactory() {
		effects = new HashMap<EffectName, Function<AbilityInfo, Ability>>();
		effects.put(DamageAbility.effectName, DamageAbility::new);
		effects.put(HealAbility.effectName, HealAbility::new);
		effects.put(TeleportAbility.effectName, TeleportAbility::new);
	}

	public Ability newAbility(AbilityName name) {
		return null;
	}
}

