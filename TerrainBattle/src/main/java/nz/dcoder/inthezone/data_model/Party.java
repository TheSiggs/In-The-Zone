package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import nz.dcoder.inthezone.data_model.pure.ItemName;

/**
 * The player's party, and everything they're carrying.
 * */
public class Party {
	final Map<ItemName, List<Item>> items;
	// equipment that isn't presently equipt to any character 
	final Collection<Equipment> equipment; 
	final Collection<Character> characters;

	public Party() {
		items = new HashMap<ItemName, List<Item>>();
		equipment = new ArrayList<Equipment>();
		characters = new ArrayList<Character>();
	}

	/**
	 * Information about items for the GUI
	 * */
	public Map<Item, Integer> getItemInfo() {
		Map<Item, Integer> m = new TreeMap<Item, Integer>();
		items.keySet().stream()
			.forEach(i -> m.put(items.get(i).get(0), items.get(i).size()));
		return m;
	}

	public boolean hasItem(ItemName name) {
		return items.keySet().stream().anyMatch(i -> i.equals(name));
	}

	public void consumeItem(ItemName name) {
		List<Item> l = items.get(name);
		if (l != null) {
			if (l.size() == 1) {
				items.remove(name);
			} else {
				l.remove(0);
			}
		}
	}

	public void gainItem(Item item) {
		List<Item> l = items.get(item.name);
		if (l == null) {
			l = new ArrayList<Item>();
		}
		l.add(item);
	}
}

