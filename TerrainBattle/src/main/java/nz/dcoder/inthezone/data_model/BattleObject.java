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
	public final boolean isObstacle;
	public final boolean isAttackable;
	public final boolean isPushable;
	public final ObjectAbility ability;

	public Position position;
	public int hitsRemaining;

	public BattleObject (
		BattleObjectName name,
		boolean isObstacle,
		boolean isAttackable,
		boolean isPushable,
		int hitsRemaining,
		ObjectAbility ability
	) {
		this.name = name;
		this.isObstacle = isObstacle;
		this.isAttackable = isAttackable;
		this.isPushable = isPushable;
		this.hitsRemaining = hitsRemaining;
		this.ability = ability;
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

