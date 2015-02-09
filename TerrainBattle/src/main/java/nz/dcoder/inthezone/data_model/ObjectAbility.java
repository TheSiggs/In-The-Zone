package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Some battle objects have an ability (e.g. trip mines can deal damage).
 *
 * This is similar to the Item class, which also implements CanDoAbility.  If
 * an object can do an ability, then we need to give it base stats and a level
 * as inputs to the damage formula.
 * */
public class ObjectAbility implements CanDoAbility {
	final Ability ability;
	private final BattleObject obj;

	public ObjectAbility (Ability ability, BattleObject obj) {
		this.ability = ability;
		this.obj = obj;
	}

	@Override public Position getPosition() {
		return obj.position;
	}

	@Override public BaseStats getBaseStats() {
		// TODO: implement this method
		return null;
	} 

	@Override public int getLevel() {
		// TODO: implement this method
		return 0;
	}

	@Override public Collection<Equipment> getWeapons() {
		// TODO: implement this method
		return null;
	}
}

