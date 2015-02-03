package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.EquipmentCategory;
import nz.dcoder.inthezone.data_model.pure.EquipmentClass;
import nz.dcoder.inthezone.data_model.pure.EquipmentName;

/**
 * Items that exist permanently, and that a character may equip. Equipment
 * gives characters their attack power and defence power.  It may also give
 * buffs, extra abilities, or both.
 * */
public class Equipment {
	public final EquipmentName name;
	public final String description;
	public final boolean isHidden;
	public final int amount;
	public final BaseStats buffs;
	public final EquipmentClass eClass;
	public final EquipmentCategory category;

	/**
	 * invisible to the presentation layer.  Use getAbilities to get information
	 * about special abilities associated with equipment
	 * */
	final Collection<Ability> abilities;

	public Equipment(
		EquipmentName name,
		String description,
		boolean isHidden,
		int amount,
		BaseStats buffs,
		EquipmentClass eClass,
		EquipmentCategory category,
		Collection<Ability> abilities
	) {
		this.name = name;
		this.description = description;
		this.isHidden = isHidden;
		this.amount = amount;
		this.buffs = buffs;
		this.eClass = eClass;
		this.category = category;
		this.abilities = abilities;
	}

	public Collection<AbilityInfo> getAbilities() {
		List<AbilityInfo> r = new ArrayList<AbilityInfo>();
		for(Ability a : abilities) {
			r.add(a.info);
		}
		return r;
	}
}

