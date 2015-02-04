package nz.dcoder.inthezone.data_model.pure;

public class EquipmentCategory extends DatabaseName {
	public EquipmentCategory(String name) {
		super(name);
	}

	@Override public boolean equals(Object x) {
		if (x instanceof EquipmentCategory) {
			return super.equals(x);
		} else {
			return false;
		}
	}
}

