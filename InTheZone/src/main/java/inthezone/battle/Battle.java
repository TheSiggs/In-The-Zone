package inthezone.battle;

import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.List;

/**
 * This class processes battle commands.
 * */
public class Battle {
	public BattleState battleState;

	public Battle(BattleState battleState) {
		this.battleState = battleState;
	}

	/**
	 * Perform a move operation on a character.  Assumes the path has been fully
	 * validated.
	 * */
	public void doMove(List<MapPoint> path) {
		battleState.getCharacterAt(path.get(0)).ifPresent(c -> {
			c.moveTo(path.get(path.size() - 1));
		});
	}

	public void doAttack(MapPoint agent, Collection<DamageToTarget> targets) {
	}

	public void doAbility(
		MapPoint agent, Ability ability, Collection<DamageToTarget> targets
	) {
	}

	public void doUseItem(MapPoint agent, Item item) {
	}

	public void doPush(MapPoint agent, MapPoint target, boolean effective) {
	}
}

