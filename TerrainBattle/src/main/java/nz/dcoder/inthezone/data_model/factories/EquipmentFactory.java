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

public class EquipmentFactory {
	private final Map<EquipmentName, Equipment> equipment;

	public EquipmentFactory(AbilityFactory abilityFactory) {
		equipment = new HashMap<EquipmentName, Equipment>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name",
			"description",
			"isHidden",
			"amount",
			// base stats validated separately
			"abilities",
			"class",
			"category"});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/equipment.csv");
			if (in == null) throw new FileNotFoundException("equipment.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL);

			for(CSVRecord record : parser) {
				validator.validate(record);

				// parse the abilities list
				String[] ss = record.get("abilities").split(",");
				List<Ability> abilities = new ArrayList<Ability>();
				for (String s : ss) {
					Ability a = abilityFactory.newAbility(new AbilityName(s.trim()));
					if (a == null) {
						throw new DatabaseNameException("No such ability " + s);
					} else {
						abilities.add(a);
					}
				}

				// construct the equipment
				Equipment item = new Equipment(
					new EquipmentName(record.get("name")),
					record.get("description"),
					RecordValidator.parseBoolean(record.get("isHidden")),
					Integer.parseInt(record.get("amount")),
					RecordValidator.parseBaseStats(record),
					EquipmentClass.parseEquipmentClass(record.get("class")),
					new EquipmentCategory(record.get("category")),
					abilities);

				equipment.put(item.name, item);
			}
		} catch (IOException e) {
			System.err.println("ERROR: IO error reading equipment.csv");
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println("ERROR: equipment.csv is malformed");
			e.printStackTrace();
		} catch (DatabaseNameException e) {
			System.err.println("ERROR: equipment.csv is malformed");
			e.printStackTrace();
		}
	}

	public Equipment newEquipment(EquipmentName name) {
		return equipment.get(name);
	}
}

