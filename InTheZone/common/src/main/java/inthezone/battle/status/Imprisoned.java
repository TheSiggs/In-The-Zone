package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.JSON;
import ssjsjs.annotations.Field;

public class Imprisoned extends StatusEffect {
	@JSON
	public Imprisoned(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn
	) {
		super(info, startTurn);
	}
}

