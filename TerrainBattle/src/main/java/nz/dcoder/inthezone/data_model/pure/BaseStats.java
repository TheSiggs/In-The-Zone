package nz.dcoder.inthezone.data_model.pure;

public class BaseStats {
	public final int baseAP;
	public final int baseMP;
	public final int strength;
	public final int intelligence;
	public final int dexterity;
	public final int guard;
	public final int spirit;
	public final int vitality;

	public BaseStats(
		int baseAP,
		int baseMP,
		int strength,
		int intelligence,
		int dexterity,
		int guard,
		int spirit,
		int vitality
	) {
		this.baseAP = baseAP;
		this.baseMP = baseMP;
		this.strength = strength;
		this.intelligence = intelligence;
		this.dexterity = dexterity;
		this.guard = guard;
		this.spirit = spirit;
		this.vitality = vitality;
	}

	public BaseStats add(BaseStats stats) {
		return new BaseStats (
			baseAP + stats.baseAP,
			baseMP + stats.baseMP,
			strength + stats.strength,
			intelligence + stats.intelligence,
			dexterity + stats.dexterity,
			guard + stats.guard,
			spirit + stats.spirit,
			vitality + stats.vitality
		);
	}
}


