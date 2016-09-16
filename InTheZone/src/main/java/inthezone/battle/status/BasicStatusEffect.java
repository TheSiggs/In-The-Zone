package inthezone.battle.status;

public class BasicStatusEffect extends StatusEffect {
	private final double attackBuff;
	private final double defenceBuff;
	private final double chanceBuff;

	public BasicStatusEffect(
		double attackBuff, double defenceBuff, double chanceBuff
	) {
		super();

		this.attackBuff = attackBuff;
		this.defenceBuff = defenceBuff;
		this.chanceBuff = chanceBuff;
	}

	@Override public double getAttackBuff() {return attackBuff;}
	@Override public double getDefenceBuff() {return defenceBuff;}
	@Override public double getChanceBuff() {return chanceBuff;}
}

