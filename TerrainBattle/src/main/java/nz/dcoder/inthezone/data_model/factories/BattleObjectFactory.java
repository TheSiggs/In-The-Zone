package nz.dcoder.inthezone.data_model.factories;

import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.BattleObject;

public class BattleObjectFactory {
	private final AbilityFactory abilityFactory;

	public BattleObjectFactory() {
		this.abilityFactory = new AbilityFactory();
	}

	public BattleObject newBattleObject(BattleObjectName name) {
		return null;
	}
}

