package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.annotations.Field;

public class Silenced extends StatusEffect {
	@JSONConstructor
	public Silenced(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn 
	) {
		super(info, startTurn);
	}
}

