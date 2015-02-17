package nz.dcoder.inthezone.data_model.pure;

public enum AbilityClass {
	PHYSICAL, MAGICAL;

	public static AbilityClass parseAbilityClass(String s0)
		throws NumberFormatException
	{
		String s = s0.trim().toUpperCase();
		if (s.equals("PHYSICAL")) return PHYSICAL;
		else if (s.equals("MAGICAL")) return MAGICAL;
		else throw new NumberFormatException(
			"\"" + s + "\" is not a valid ability class");
	}
}


