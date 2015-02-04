package nz.dcoder.inthezone.data_model.pure;

public class AbilityName extends DatabaseName {
	public AbilityName(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof AbilityName) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

