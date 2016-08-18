package inthezone.battle;

import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nz.dcoder.ai.astar.AStarSearch;

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

	public Collection<Character> cloneCharacters() {
		return characters.stream().map(c -> c.clone()).collect(Collectors.toList());
	}

	public boolean canMove(List<MapPoint> path) {
		return true;
	}

	public List<MapPoint> findPath(
		MapPoint start, MapPoint target, Player player
	) {
		Set<MapPoint> obstacles = new HashSet<>(characters.stream()
			.filter(c -> c.blocksPath(player))
			.map(c -> c.getPos()).collect(Collectors.toList()));

		AStarSearch<MapPoint> search = new AStarSearch<>(new PathFinderNode(
			null, terrain.terrain, obstacles,
			terrain.terrain.w, terrain.terrain.h,
			start, target));

		return search.search().stream()
			.map(n -> n.getPosition()).collect(Collectors.toList());
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

