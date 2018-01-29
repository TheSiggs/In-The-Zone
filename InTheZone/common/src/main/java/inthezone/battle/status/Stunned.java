package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.JSON;
import ssjsjs.annotations.Field;

public class Stunned extends StatusEffect {
	@JSON
	public Stunned(
		@Field("info") final StatusEffectInfo info,
		@Field("start") final int startTurn
	) {
		super(info, startTurn);
	}
}

