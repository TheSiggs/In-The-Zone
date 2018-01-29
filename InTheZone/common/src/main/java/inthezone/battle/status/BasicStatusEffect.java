package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

public class BasicStatusEffect extends StatusEffect {
	private final double attackBuff;
	private final double defenceBuff;
	private final double chanceBuff;

	@JSON
	public BasicStatusEffect(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn,
		@Field("attackBuff") final double attackBuff,
		@Field("defenceBuff") final double defenceBuff,
		@Field("chanceBuff") final double chanceBuff
	) {
		super(info, startTurn);

		this.attackBuff = attackBuff;
		this.defenceBuff = defenceBuff;
		this.chanceBuff = chanceBuff;
	}

	@Override public double getAttackBuff() {return attackBuff;}
	@Override public double getDefenceBuff() {return defenceBuff;}
	@Override public double getChanceBuff() {return chanceBuff;}
}

