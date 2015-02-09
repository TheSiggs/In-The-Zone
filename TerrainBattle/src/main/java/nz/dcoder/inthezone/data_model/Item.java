package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.ItemName;

public class Item {
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

	@Override public boolean equals(Object obj) {
		return name.equals(obj);
	}

	@Override public int hashCode() {
		return name.hashCode();
	}
}

