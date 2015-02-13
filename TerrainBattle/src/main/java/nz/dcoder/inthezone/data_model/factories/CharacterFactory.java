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
import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;

public class CharacterFactory {
	private final LevelControllerFactory levelControllerFactory;
	private final BattleObjectFactory objectFactory;


	private final Map<CharacterName, String> descriptions;
	private final Map<CharacterName, BaseStats> stats;
	private final Map<CharacterName, BattleObjectName> bodyNames;

	public CharacterFactory(
		AbilityFactory abilityFactory,
		BattleObjectFactory objectFactory
	) throws DatabaseException
	{
		this.levelControllerFactory =
			new LevelControllerFactory(abilityFactory);
		this.objectFactory = objectFactory;

		descriptions = new HashMap<CharacterName, String>();
		stats = new HashMap<CharacterName, BaseStats>();
		bodyNames = new HashMap<CharacterName, BattleObjectName>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name",
			"description",
			// basestats
			"deadBody"
			});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/characters.csv");
			if (in == null) throw new FileNotFoundException("File not found characters.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

			for(CSVRecord record : parser) {
				validator.validate(record);

				// skip blanks
				if (record.get("name").trim().equals("")) continue;

				CharacterName name = new CharacterName(record.get("name"));

				descriptions.put(name, record.get("description"));
				stats.put(name, RecordValidator.parseBaseStats(record));
				bodyNames.put(name, new BattleObjectName(record.get("deadBody")));
			}
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading characters.csv: " + e.getMessage(), e);
		}
	}

	public Character newCharacter(CharacterName name, int level) {
		if (level < 1 || level > LevelController.maxLevel)
			throw new IllegalArgumentException("Level must be between 1 and 100");

		if (!descriptions.containsKey(name) ||
			!stats.containsKey(name) ||
			!bodyNames.containsKey(name)
		) {
			return null;
		} else {
			LevelController lc =
				levelControllerFactory.newLevelController(name);
			Character c = new Character(
				name,
				descriptions.get(name),
				stats.get(name),
				lc,
				objectFactory::newBattleObject,
				bodyNames.get(name));
			c.addExp(levelControllerFactory.initExp(name, level));
			return c;
		}
	}
}

