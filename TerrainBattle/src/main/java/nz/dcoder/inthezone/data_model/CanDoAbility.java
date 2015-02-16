package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Interface for things that can carry out an ability.  Provides parameters
 * needed by the damage formula, but not all parameters.  Parameters referring
 * to the target come from the Character object representing the target.
 * */
interface CanDoAbility {
	public Position getPosition();
	public BaseStats getBaseStats();
	public int getLevel();

	/**
	 * Returns the weapon that the agent is carrying.  Does not return null
	 * */
	public Equipment getWeapon();
}

