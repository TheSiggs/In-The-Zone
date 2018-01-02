package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.annotations.Field;

public class Stunned extends StatusEffect {
	@JSONConstructor
	public Stunned(
		@Field("info") final StatusEffectInfo info,
		@Field("start") final int startTurn
	) {
		super(info, startTurn);
	}
}

