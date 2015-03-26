package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.Position;

public class DoAbilityInfo {
	public final Position agentPos;      // position of the character before the ability happens
	public final Position agentTarget;   // position of the character after the ability happens
	public final Collection<Position> targets;   // positions with targeted characters/objects
	public final AbilityInfo ability;            // the ability that was applied

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

