package nz.dcoder.inthezone.data_model.pure;

public class EffectName extends DatabaseName {
	public EffectName(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof EffectName) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

