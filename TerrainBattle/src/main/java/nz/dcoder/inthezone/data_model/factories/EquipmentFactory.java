package nz.dcoder.inthezone.data_model.factories;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import nz.dcoder.inthezone.data_model.Ability;
import nz.dcoder.inthezone.data_model.Equipment;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.EquipmentCategory;
import nz.dcoder.inthezone.data_model.pure.EquipmentClass;
import nz.dcoder.inthezone.data_model.pure.EquipmentName;
import nz.dcoder.inthezone.data_model.utils.UnicodeInputReader;

public class EquipmentFactory {
	private final Map<EquipmentName, Equipment> equipment;

	public EquipmentFactory(AbilityFactory abilityFactory)
		throws DatabaseException
	{
		equipment = new HashMap<EquipmentName, Equipment>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name",
			"description",
			"isHidden",
			"isDual",
			"physical",
			"magical",
			// base stats validated separately
			"abilities",
			"class",
			"category"});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/equipment.csv");
			if (in == null) throw new FileNotFoundException("File not found equipment.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

			for(CSVRecord record : parser) {
				validator.validate(record);

				// skip blanks
				if (record.get("name").trim().equals("")) continue;

				// parse the abilities list
				List<Ability> abilities = RecordValidator.parseAbilityList(
					abilityFactory, record.get("abilities"));

				// construct the equipment
				Equipment item = new Equipment(
					new EquipmentName(record.get("name")),
					record.get("description"),
					RecordValidator.parseBoolean(record.get("isHidden")),
					RecordValidator.parseBoolean(record.get("isDual")),
					Integer.parseInt(record.get("physical")),
					Integer.parseInt(record.get("magical")),
					RecordValidator.parseBaseStats(record),
					EquipmentClass.parseEquipmentClass(record.get("class")),
					new EquipmentCategory(record.get("category")),
					abilities);

				equipment.put(item.name, item);
			}
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading equipment.csv: " + e.getMessage(), e);
		}
	}

	public Equipment newEquipment(EquipmentName name) {
		return equipment.get(name);
	}
}

