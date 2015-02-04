package nz.dcoder.inthezone.data_model.pure;

public class ItemName extends DatabaseName {
	public ItemName(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof ItemName) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

