package inthezone.battle.status;

import inthezone.battle.data.StatusEffectInfo;

public class BasicStatusEffect extends StatusEffect {
	private final double attackBuff;
	private final double defenceBuff;
	private final double chanceBuff;

	public BasicStatusEffect(
		final StatusEffectInfo info, final int startTurn,
		final double attackBuff, final double defenceBuff, final double chanceBuff
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

