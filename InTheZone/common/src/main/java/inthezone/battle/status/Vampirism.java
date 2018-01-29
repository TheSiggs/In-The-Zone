package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.JSON;
import ssjsjs.annotations.Field;

public class Vampirism extends StatusEffect {
	@JSON
	public Vampirism(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn
	) {
		super(info, startTurn);
	}
}

