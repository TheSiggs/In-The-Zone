package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

public class Cover extends StatusEffect {
	@JSON
	public Cover(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn
	) {
		super(info, startTurn);
	}
}

