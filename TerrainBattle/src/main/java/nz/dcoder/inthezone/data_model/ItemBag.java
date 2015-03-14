package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.dcoder.inthezone.data_model.pure.ItemName;

/**
 * A class to track what items a party has, and how many of each.
 * */
public class ItemBag {
	final Map<ItemName, List<Item>> items;

	public ItemBag() {
		items = new HashMap<>();
	}

	/**
	 * Information about items for the GUI
	 *
	 * @return The items in this bag, plus how many of each.
	 */
	public Map<Item, Integer> getItemInfo() {
		Map<Item, Integer> m = new HashMap<>();
		items.keySet().stream()
			.forEach(i -> m.put(items.get(i).get(0), items.get(i).size()));
		return m;
	}

	/**
	 * Do we have any of a particular item?
	 * @param name The name of the item we're looking for
	 * */
	public boolean hasItem(ItemName name) {
		return items.keySet().stream().anyMatch(i -> i.equals(name));
	}

	/**
	 * Remove an item from the bag.
	 * @param name The name of the item to consume
	 * */
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

	/**
	 * Add an item to the bag.
	 * @param item The item to add
	 * */
	public void gainItem(Item item) {
		List<Item> l = items.get(item.name);
		if (l == null) {
			l = new ArrayList<Item>();
			items.put(item.name, l);
		}
		l.add(item);
	}
}

