package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;

public class DoAbilityInfo {
	public final Position agentPos;
	public final Position target;
	public final AbilityInfo ability;

	public DoAbilityInfo(
		Position agentPos,
		Position target,
		AbilityInfo ability
	) {
		this.agentPos = agentPos;
		this.target = target;
		this.ability = ability;
	}
}

