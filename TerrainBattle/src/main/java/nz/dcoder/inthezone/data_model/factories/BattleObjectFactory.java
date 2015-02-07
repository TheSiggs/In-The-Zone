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

import nz.dcoder.inthezone.data_model.Ability;
import nz.dcoder.inthezone.data_model.BattleObject;
import nz.dcoder.inthezone.data_model.ObjectAbility;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.Position;

public class BattleObjectFactory {
	private final Map<BattleObjectName, BattleObjectInfo> objects;

	public BattleObjectFactory(AbilityFactory abilityFactory)
		throws DatabaseException
	{
		objects = new HashMap<BattleObjectName, BattleObjectInfo>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name",
			"isObstacle",
			"isAttackable",
			"isPushable",
			"hits",
			"hasAbility",
			"ability"});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/objects.csv");
			if (in == null) throw new FileNotFoundException("File not found objects.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

			for(CSVRecord record : parser) {
				validator.validate(record);

				// skip blanks
				if (record.get("name").trim().equals("")) continue;

				ObjectAbility oa = null;
				if (RecordValidator.parseBoolean(record.get("hasAbility"))) {
					AbilityName abilityName = new AbilityName(record.get("ability"));
					Ability ability = abilityFactory.newAbility(abilityName);
					if (ability == null) {
						throw new DatabaseNameException(
							"No such ability " + abilityName.toString());
					}
					oa = new ObjectAbility(ability);
				}

				BattleObjectInfo info = new BattleObjectInfo (
					new BattleObjectName(record.get("name")),
					RecordValidator.parseBoolean(record.get("isObstacle")),
					RecordValidator.parseBoolean(record.get("isAttackable")),
					RecordValidator.parseBoolean(record.get("isPushable")),
					oa,
					Integer.parseInt(record.get("hits")));

				objects.put(info.name, info);
			}
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading objects.csv: " + e.getMessage(), e);
		}

	}

	public BattleObject newBattleObject(BattleObjectName name, Position p0) {
		BattleObjectInfo info = objects.get(name);
		if (info != null) {
			BattleObject r = new BattleObject(
				info.name,
				info.isObstacle,
				info.isAttackable,
				info.isPushable,
				info.initHits,
				info.ability);
			r.position = p0;
			return r;
		} else {
			return null;
		}
	}
}

class BattleObjectInfo {
	public final BattleObjectName name;
	public final boolean isObstacle;
	public final boolean isAttackable;
	public final boolean isPushable;
	public final ObjectAbility ability;
	public final int initHits;

	public BattleObjectInfo (
		BattleObjectName name,
		boolean isObstacle,
		boolean isAttackable,
		boolean isPushable,
		ObjectAbility ability,
		int initHits
	) {
		this.name = name;
		this.isObstacle = isObstacle;
		this.isAttackable = isAttackable;
		this.isPushable = isPushable;
		this.initHits = initHits;
		this.ability = ability;
	}
}


