package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoCharacterDeath {
	public final BattleObjectName body;
	public final Position position;

	public DoCharacterDeath(Position position, BattleObjectName body) {
		this.position = position;
		this.body = body;
	}
}

