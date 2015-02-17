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
import nz.dcoder.inthezone.data_model.Item;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.ItemName;
import nz.dcoder.inthezone.data_model.utils.UnicodeInputReader;

public class ItemFactory {
	private final Map<ItemName, Item> items;

	public ItemFactory(AbilityFactory abilityFactory) throws DatabaseException {
		items = new HashMap<ItemName, Item>();

		RecordValidator validator = new RecordValidator(new String[] {
			"name", "description", "ability"});

		try {
			InputStream in = this.getClass().getResourceAsStream(
					"/nz/dcoder/inthezone/data/items.csv");
			if (in == null) throw new FileNotFoundException("File not found items.csv");
			InputStreamReader reader = new UnicodeInputReader(in);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

			for(CSVRecord record : parser) {
				validator.validate(record);

				// skip blanks
				if (record.get("name").trim().equals("")) continue;

				AbilityName abilityName = new AbilityName(record.get("ability"));
				Ability ability = abilityFactory.newAbility(abilityName);
				if (ability == null) {
					throw new DatabaseNameException(
						"No such ability " + abilityName.toString());
				}

				Item item = new Item (
					new ItemName(record.get("name")),
					record.get("description"),
					ability);

				items.put(item.name, item);
			}
		} catch (Exception e) {
			throw new DatabaseException(
				"Error reading items.csv: " + e.getMessage(), e);
		}
	}

	public Item newItem(ItemName name) {
		return items.get(name);
	}
}

