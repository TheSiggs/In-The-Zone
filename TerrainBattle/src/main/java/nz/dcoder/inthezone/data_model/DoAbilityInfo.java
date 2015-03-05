package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoAbilityInfo {
	public final Position agentPos;
	public final Position agentTarget;
	public final Collection<Position> targets;
	public final AbilityInfo ability;

	public DoAbilityInfo(
		Position agentPos,
		Position agentTarget,
		Collection<Position> targets,
		AbilityInfo ability
	) {
		this.agentPos = agentPos;
		this.agentTarget = agentTarget;
		this.targets = targets;
		this.ability = ability;
	}
}

