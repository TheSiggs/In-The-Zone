package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.BaseStats;

/**
 * Interface for things that can carry out an ability.  Provides parameters
 * needed by the damage formula, but not all parameters.  Parameters referring
 * to the target come from the Character object representing the target.
 * */
interface CanDoAbility {
	public BaseStats getBaseStats();
	public int getLevel();

	/**
	 * This is a bit of a misnomer.  For abilities that deal damage, it will
	 * partially determine the amount of damage to do.  For healing it would
	 * either determine the amount of healing that happens, or we might even
	 * ignore equipment for healing abilities.  This picture gets even more
	 * complicated when we add other kinds of ability.  Some clarification may be
	 * needed here.
	 * */
	public Collection<Equipment> getWeapons();
}

