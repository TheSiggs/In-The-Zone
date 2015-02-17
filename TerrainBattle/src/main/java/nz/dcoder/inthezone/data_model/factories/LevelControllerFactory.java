package nz.dcoder.inthezone.data_model.factories;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import nz.dcoder.inthezone.data_model.Ability;
import nz.dcoder.inthezone.data_model.LevelController;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.utils.UnicodeInputReader;

class LevelControllerFactory {
	private final AbilityFactory abilityFactory;
	private final Map<CharacterName, List<Ability>[]> characterAbilities;
	private final LevelInfo levelInfo;

	public LevelControllerFactory(AbilityFactory abilityFactory)
		throws DatabaseException
	{
		this.abilityFactory = abilityFactory;

		try {
			this.levelInfo = readLevels("levels.csv");
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading levels.csv: " + e.getMessage(), e);
		}

		try {
			this.characterAbilities = readAbilities("characterAbilities.csv");
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading characterAbilities.csv: " + e.getMessage(), e);
		}
	}

	private Map<CharacterName, List<Ability>[]> readAbilities(String filename)
		throws IOException, NumberFormatException, DatabaseNameException
	{
		Map<CharacterName, Map<Integer, List<Ability>>> m =
			new HashMap<CharacterName, Map<Integer, List<Ability>>>();

		RecordValidator validator = new RecordValidator(new String[] {
			"character",
			"level",
			"abilities"});

		InputStream in = this.getClass().getResourceAsStream(
				"/nz/dcoder/inthezone/data/" + filename);
		if (in == null) throw new FileNotFoundException("File not found " + filename);
		InputStreamReader reader = new UnicodeInputReader(in);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

		for(CSVRecord record : parser) {
			validator.validate(record);

			// skip blanks
			if (record.get("character").trim().equals("")) continue;

			List<Ability> abilities = RecordValidator.parseAbilityList(
				abilityFactory, record.get("abilities"));

			CharacterName character = new CharacterName(record.get("character"));
			int level = Integer.parseInt(record.get("level"));

			if (!m.containsKey(character)) {
				m.put(character, new HashMap<Integer, List<Ability>>());
			}

			m.get(character).put(level, abilities);
		}

		Map<CharacterName, List<Ability>[]> r =
			new HashMap<CharacterName, List<Ability>[]>();
		for (CharacterName k : m.keySet()) {
			r.put(k, buildAbilityArray(m.get(k)));
		}

		return r;
	}
	
	private LevelInfo readLevels(String filename)
		throws IOException, NumberFormatException
	{
		Map<Integer, Integer> expMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> hpModMap = new HashMap<Integer, Integer>();

		RecordValidator validator = new RecordValidator(new String[] {
			"level",
			"exp",
			"hpMod"});

		InputStream in = this.getClass().getResourceAsStream(
				"/nz/dcoder/inthezone/data/" + filename);
		if (in == null) throw new FileNotFoundException("File not found " + filename);
		InputStreamReader reader = new UnicodeInputReader(in);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

		for(CSVRecord record : parser) {
			validator.validate(record);

			// skip blanks
			if (record.get("level").trim().equals("")) continue;

			expMap.put(Integer.parseInt(record.get("level")),
				Integer.parseInt(record.get("exp")));

			hpModMap.put(Integer.parseInt(record.get("level")),
				Integer.parseInt(record.get("hpMod")));
		}

		return new LevelInfo(buildIntArray(hpModMap), buildIntArray(expMap));
	}

	private static List<Ability>[] buildAbilityArray(
		Map<Integer, List<Ability>> xs
	) {
		List<Ability>[] r = new List[101];
		List<Ability> def = new ArrayList<Ability>();
		for (int i = 0; i < 101; i++) {
			r[i] = xs.getOrDefault(i, def);
		}
		return r;
	}

	private static int[] buildIntArray(Map<Integer, Integer> xs) {
		int[] r = new int[101];
		int def = 0;
		for (int i = 0; i < 101; i++) {
			r[i] = xs.getOrDefault(i, def);
			def = r[i];
		}
		return r;
	}

	private class LevelInfo {
		public final int[] hpMod;
		public final int[] totalExpRequired;

		public LevelInfo(int[] hpMod, int[] totalExpRequired) {
			this.hpMod = hpMod;
			this.totalExpRequired = totalExpRequired;
		}
	}

	public LevelController newLevelController(CharacterName character) {
		if (!characterAbilities.containsKey(character)) return null;

		LevelController lc = new LevelController(
			levelInfo.hpMod,
			characterAbilities.get(character),
			levelInfo.totalExpRequired);

		return lc;
	}

	/**
	 * Get the number of Exp points that a character starts with
	 * */
	int initExp(CharacterName character, int level) {
		return levelInfo.totalExpRequired[level];
	}
}

