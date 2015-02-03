package nz.dcoder.inthezone.data_model;

import java.util.Collection;

/**
 * The player's party, and everything they're carrying.
 * */
public class Party {
	public Collection<Item> items;
	// equipment that isn't presently equipt to any character 
	public Collection<Equipment> equipment; 
	public Collection<Character> characters;

	public Party() {
		// TODO: implement this method
		// create an initial party for testing purposes
	}
}

