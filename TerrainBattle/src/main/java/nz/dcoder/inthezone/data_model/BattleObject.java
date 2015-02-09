package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Objects that are relevant to the battle, but that are not characters.  Examples:
 *   - walls
 *   - corpses
 *   - boulders
 *   - mines and other traps
 * */
public class BattleObject {
	public final BattleObjectName name;

	public final boolean blocksSpace;  // no character can co-occupy a space with this object
	public final boolean blocksPath;   // no path can go through this object

	public final boolean isAttackable;
	public final boolean isPushable;
	public final ObjectAbility ability;

	public Position position;
	public int hitsRemaining;

	public BattleObject (
		BattleObjectName name,
		boolean blocksSpace,
		boolean blocksPath,
		boolean isAttackable,
		boolean isPushable,
		int hitsRemaining,
		Ability ability
	) {
		this.name = name;
		this.blocksSpace = blocksSpace;
		this.blocksPath = blocksPath;
		this.isAttackable = isAttackable;
		this.isPushable = isPushable;
		this.hitsRemaining = hitsRemaining;

		if (ability == null) {
			this.ability = null;
		} else {
			this.ability = new ObjectAbility(ability, this);
		}
	}

	/**
	 * Used e.g. by exploding mines.  Called when a character moves to or through
	 * the position parameter, determines whether or not the object's ability is
	 * triggered on that square.
	 * */
	public boolean mayTriggerAbilityAt(Position position) {
		// TODO: implement this method
		return false;
	}
}

