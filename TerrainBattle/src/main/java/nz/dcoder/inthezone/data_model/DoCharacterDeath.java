package nz.dcoder.inthezone.data_model;

import java.util.List;
import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoCharacterDeath {
	public final List<BattleObjectName> bodies;
	public final List<Position> positions;

	public DoCharacterDeath(List<Position> positions, List<BattleObjectName> bodies) {
		this.positions = positions;
		this.bodies = bodies;
	}
}

