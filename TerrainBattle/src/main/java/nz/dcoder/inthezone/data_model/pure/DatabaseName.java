package nz.dcoder.inthezone.data_model.pure;

/**
 * superclass for names of things (e.g. CharacterName)
 * */
public abstract class DatabaseName {
	private final String name;

	public DatabaseName(String name) {
		this.name = name.trim();
	}

	@Override public int hashCode() {
		return name.hashCode();
	}

	@Override public String toString() {
		return name;
	}

	@Override public boolean equals(Object x) {
		if (x instanceof DatabaseName) {
			return this.name.equals(((DatabaseName) x).name);
		} else {
			return false;
		}
	}
}

