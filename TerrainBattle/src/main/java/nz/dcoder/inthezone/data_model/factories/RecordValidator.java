package nz.dcoder.inthezone.data_model.factories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

import nz.dcoder.inthezone.data_model.Ability;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.BaseStats;

class RecordValidator {
	private final String[] headerNames;

	public RecordValidator(String[] headerNames) {
		this.headerNames = headerNames;
	}

	public void validate(CSVRecord record) throws IOException {
		for (String header : headerNames) {
			if (!record.isMapped(header)) {
				throw new IOException("Missing expected header " + header);
			}
		}
	}

	public static boolean parseBoolean(String s) throws NumberFormatException {
		String u = s.toUpperCase().trim();
		if (u.equals("TRUE") || u.equals("YES") || u.equals("1")) {
			return true;
		} else if (u.equals("FALSE") || u.equals("NO") || u.equals("0")) {
			return false;
		} else {
			throw new NumberFormatException("Expected a boolean value, saw " + s);
		}
	}

	public static BaseStats parseBaseStats(CSVRecord record)
		throws NumberFormatException, IOException
	{
		// validate that the record contains all the base stats
		String[] headers = {
			"baseAP",
			"baseMP",
			"strength",
			"intelligence",
			"dexterity",
			"guard",
			"spirit",
			"vitality"
		};
		for (String header : headers) {
			if (!record.isMapped(header)) {
				throw new IOException("Missing expected header " + header);
			}
		}

		BaseStats r = new BaseStats(
			Integer.parseInt(record.get("baseAP")),
			Integer.parseInt(record.get("baseMP")),
			Integer.parseInt(record.get("strength")),
			Integer.parseInt(record.get("intelligence")),
			Integer.parseInt(record.get("dexterity")),
			Integer.parseInt(record.get("guard")),
			Integer.parseInt(record.get("spirit")),
			Integer.parseInt(record.get("vitality"))
		);

		return r;
	}

	public static List<Ability> parseAbilityList(
		AbilityFactory abilityFactory, String names
	) throws DatabaseNameException
	{
		String[] ss = names.trim().split(",");
		if (names.trim().equals("")) ss = new String[0];

		List<Ability> abilities = new ArrayList<Ability>();
		for (String s : ss) {
			Ability a = abilityFactory.newAbility(new AbilityName(s));
			if (a == null) {
				throw new DatabaseNameException("No such ability \"" + s + "\" in \"" + names + "\"");
			} else {
				abilities.add(a);
			}
		}

		return abilities;
	}
}

