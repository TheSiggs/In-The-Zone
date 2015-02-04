package nz.dcoder.inthezone.data_model.pure;

public class EquipmentName extends DatabaseName {
	public EquipmentName(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof EquipmentName) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

