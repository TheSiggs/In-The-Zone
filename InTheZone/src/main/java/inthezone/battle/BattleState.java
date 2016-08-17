package inthezone.battle;

import java.util.Collection;
import java.util.List;

import isogame.engine.MapPoint;
import isogame.engine.Stage;

/**
 * This class keeps track of the state of the battle.  It does not know how to
 * perform high level battle operations.
 * */
public class BattleState {
	public final Stage terrain;
	public final Collection<Character> characters;

	public BattleState(Stage terrain, Collection<Character> characters) {
		this.terrain = terrain;
		this.characters = characters;
	}

	public Targetable getTargetableAt(MapPoint x) {
		return null;
	}

	public Character getCharacterAt(MapPoint x) {
		return null;
	}

	public boolean canMove(List<MapPoint> path) {
		return true;
	}

	public List<MapPoint> findPath(MapPoint start, MapPoint target) {
		return null;
	}

	public Collection<Targetable> getAbilityTargets(
		MapPoint agent, Ability ability, MapPoint target
	) {
		return null;
	}

	public boolean canAttack(MapPoint agent, Collection<DamageToTarget> targets) {
		return true;
	}

	public boolean canDoAbility(
		MapPoint agent, Ability ability, Collection<DamageToTarget> targets
	) {
		return true;
	}

	public boolean canUseItem(MapPoint agent, Item item) {
		return true;
	}

	public boolean canPush(MapPoint agent, MapPoint target, boolean effective) {
		return true;
	}
}

