package nz.dcoder.inthezone.data_model;

import java.util.List;
import nz.dcoder.inthezone.data_model.pure.BattleObjectInfo;

public class DoCharacterDeath {
	public final List<BattleObjectInfo> bodies;

	public DoCharacterDeath(List<BattleObjectInfo> bodies) {
		this.bodies = bodies;
	}
}

