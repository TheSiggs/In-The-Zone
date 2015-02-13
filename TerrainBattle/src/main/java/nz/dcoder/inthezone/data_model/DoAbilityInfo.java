package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoAbilityInfo {
	public final Position agentPos;
	public final Collection<Position> targets;
	public final AbilityInfo ability;

	public DoAbilityInfo(
		Position agentPos,
		Collection<Position> targets,
		AbilityInfo ability
	) {
		this.agentPos = agentPos;
		this.targets = targets;
		this.ability = ability;
	}
}

