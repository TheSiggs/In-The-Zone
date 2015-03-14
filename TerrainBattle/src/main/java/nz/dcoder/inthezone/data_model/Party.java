package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.dcoder.inthezone.data_model.pure.ItemName;

/**
 * The player's party, and everything they're carrying.
 * */
public class Party {
	// equipment that isn't presently equipt to any character 
	private final Collection<Equipment> equipment; 
	private final Collection<Character> characters;
	private final ItemBag items;

	public Party() {
		equipment = new ArrayList<>();
		characters = new ArrayList<>();
		items = new ItemBag();
	}

	public ItemBag getItemBag() {
		return items;
	}
}

