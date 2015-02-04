package nz.dcoder.inthezone.data_model.pure;

public class CharacterName extends DatabaseName {
	public CharacterName(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof CharacterName) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

