package nz.dcoder.inthezone.data_model.factories;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import nz.dcoder.inthezone.data_model.Ability;
import nz.dcoder.inthezone.data_model.AbilityInfo;
import nz.dcoder.inthezone.data_model.DamageAbility;
import nz.dcoder.inthezone.data_model.HealAbility;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.TeleportAbility;
import nz.dcoder.inthezone.data_model.utils.UnicodeInputReader;

public class AbilityFactory {
	private final Map<AbilityName, Ability> abilities;

	public AbilityFactory() throws DatabaseException {
		// the effects database
		Map<EffectName, Function<AbilityInfo, Ability>> effects;
		effects = new HashMap<EffectName, Function<AbilityInfo, Ability>>();
		effects.put(DamageAbility.effectName, DamageAbility::new);
		effects.put(HealAbility.effectName, HealAbility::new);
		effects.put(TeleportAbility.effectName, TeleportAbility::new);

		abilities = new HashMap<AbilityName, Ability>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name",
			"cost",
			"s",
			"range",
			"areaOfEffect",
			"hasAOEShading",
			"isPiercing",
			"requiresLOS",
			"requiresMana",
			"repeats",
			"class",
			"effect"});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/abilities.csv");
			if (in == null) throw new FileNotFoundException("File not found abilities.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

			for(CSVRecord record : parser) {
				validator.validate(record);

				// skip blanks
				if (record.get("name").trim().equals("")) continue;

				AbilityInfo info = new AbilityInfo (
					new AbilityName(record.get("name")),
					Integer.parseInt(record.get("cost")),
					Double.parseDouble(record.get("s")),
					Integer.parseInt(record.get("range")),
					Integer.parseInt(record.get("areaOfEffect")),
					RecordValidator.parseBoolean(record.get("hasAOEShading")),
					RecordValidator.parseBoolean(record.get("isPiercing")),
					RecordValidator.parseBoolean(record.get("requiresLOS")),
					RecordValidator.parseBoolean(record.get("requiresMana")),
					Integer.parseInt(record.get("repeats")),
					record.get("class"),
					new EffectName(record.get("effect")));

				if (!effects.containsKey(info.effect)) {
					throw new DatabaseNameException(
						"No such effect " + info.effect.toString());
				}

				abilities.put(info.name, effects.get(info.effect).apply(info));
			}
		} catch (NumberFormatException e) {
			throw new DatabaseException(
				"Error reading abilities.csv: " + e.getMessage() + " expected number", e);
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading abilities.csv: " + e.getMessage(), e);
		}
	}

	public Ability newAbility(AbilityName name) {
		return abilities.get(name);
	}
}

