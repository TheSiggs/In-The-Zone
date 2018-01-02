package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSONConstructor;

public class Cover extends StatusEffect {
	@JSONConstructor
	public Cover(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn
	) {
		super(info, startTurn);
	}
}

