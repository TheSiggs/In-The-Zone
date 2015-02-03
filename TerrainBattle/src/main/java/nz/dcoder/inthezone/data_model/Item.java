package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.ItemName;

public class Item implements CanDoAbility {
	public final ItemName name;
	public final String description;
	final Ability ability;

	public Item (
		ItemName name,
		String description,
		Ability ability
	) {
		this.name = name;
		this.description = description;
		this.ability = ability;
	}

	@Override public BaseStats getBaseStats() {
		// TODO: implement this method
		// NOTE: an item needs some of the base stats as parameters to the
		// damage formula (assuming we use the same damage formula for items).
		return null;
	} 

	@Override public int getLevel() {
		// TODO: implement this method
		// NOTE: an item needs a level as a parameter to the damage formula.
		return 0;
	} 

	@Override public Collection<Equipment> getWeapons() {
		// TODO: implement this method
		//
		// suggest that we create a 'fake' internal equipment item to represent the
		// attack power of this item, for items that deal damage.
		return null;
	} 
}

