package nz.dcoder.inthezone.data_model.factories;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import nz.dcoder.inthezone.data_model.Character;
import nz.dcoder.inthezone.data_model.LevelController;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.CharacterName;

public class CharacterFactory {
	private final LevelControllerFactory levelControllerFactory;

	private final Map<CharacterName, String> descriptions;
	private final Map<CharacterName, BaseStats> stats;

	public CharacterFactory(AbilityFactory abilityFactory)
		throws DatabaseException
	{
		this.levelControllerFactory =
			new LevelControllerFactory(abilityFactory);

		descriptions = new HashMap<CharacterName, String>();
		stats = new HashMap<CharacterName, BaseStats>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name",
			"description"
			// basestats
			});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/characters.csv");
			if (in == null) throw new FileNotFoundException("characters.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL);

			for(CSVRecord record : parser) {
				validator.validate(record);

				CharacterName name = new CharacterName(record.get("name"));

				descriptions.put(name, record.get("description"));
				stats.put(name, RecordValidator.parseBaseStats(record));
			}
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading characters.csv: " + e.getMessage(), e);
		}
	}

	public Character newCharacter(CharacterName name, int level) {
		if (!descriptions.containsKey(name) || !stats.containsKey(name)) {
			return null;
		} else {
			LevelController lc =
				levelControllerFactory.newLevelController(name, level);
			return new Character(
				name, descriptions.get(name), stats.get(name), lc);
		}
	}
}

