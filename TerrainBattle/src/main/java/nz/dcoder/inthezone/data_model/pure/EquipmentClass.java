package nz.dcoder.inthezone.data_model.pure;

public enum EquipmentClass {
	WEAPON, ARMOUR, OTHER;

	public static EquipmentClass parseEquipmentClass(String s0)
		throws NumberFormatException
	{
		String s = s0.trim().toUpperCase();
		if (s.equals("WEAPON")) return WEAPON;
		else if (s.equals("ARMOUR")) return ARMOUR;
		else if (s.equals("OTHER")) return OTHER;
		else throw new NumberFormatException(
			"\"" + s + "\" is not a valid equipment class");
	}
}

