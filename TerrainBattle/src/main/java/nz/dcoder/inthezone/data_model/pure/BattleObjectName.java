package nz.dcoder.inthezone.data_model.pure;

public class BattleObjectName extends DatabaseName {
	public BattleObjectName(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof BattleObjectName) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

