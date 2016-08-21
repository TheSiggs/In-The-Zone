package inthezone.battle;

import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

	private final Set<MapPoint> terrainObstacles;

	public BattleState(Stage terrain, Collection<Character> characters) {
		this.terrain = terrain;
		this.characters = characters;

		terrainObstacles = new HashSet<>(terrain.sprites.keySet());
	}

	public Targetable getTargetableAt(MapPoint x) {
		return null;
	}

	/**
	 * Get the character at a particular point.
	 * */
	public Optional<Character> getCharacterAt(MapPoint x) {
		return characters.stream().filter(c -> c.getPos().equals(x)).findFirst();
	}

	/**
	 * Get a copy of the characters list.  Prevents unintentional and potentially
	 * unsafe mutation of the original character data.
	 * */
	public Collection<Character> cloneCharacters() {
		return characters.stream().map(c -> c.clone()).collect(Collectors.toList());
	}

	/**
	 * Find a path that is guaranteed to be valid
	 * @return The empty list if there is no valid path
	 * */
	public List<MapPoint> findValidPath(
		MapPoint start, MapPoint target, Player player
	) {
		List<MapPoint> path = findPath(start, target, player);
		if (canMove(path)) return path; else return new ArrayList<>();
	}

	/**
	 * Determine if a path is valid.  A path is valid if it takes a character to
	 * an unoccupied square and isn't longer than the character's mp.
	 * */
	public boolean canMove(List<MapPoint> path) {
		if (path.size() < 2) return false;
		return getCharacterAt(path.get(0)).map(c -> {
			MapPoint target = path.get(path.size() - 1);

			return !spaceObstacles(c.player).contains(target) &&
				path.size() - 1 <= c.getMP();
		}).orElse(false);
	}

	private Set<MapPoint> spaceObstacles(Player player) {
		Set<MapPoint> r = new HashSet<>(characters.stream()
			.filter(c -> c.blocksSpace(player))
			.map(c -> c.getPos()).collect(Collectors.toList()));
		r.addAll(terrainObstacles);
		return r;
	}

	/**
	 * Find a path between two points.
	 * @param player The player that has to traverse this path
	 * @return A short path from start to target, or an empty list if there is no
	 * path.
	 * */
	public List<MapPoint> findPath(
		MapPoint start, MapPoint target, Player player
	) {
		if (start.equals(target)) return new ArrayList<>();

		Set<MapPoint> obstacles = new HashSet<>(characters.stream()
			.filter(c -> c.blocksPath(player))
			.map(c -> c.getPos()).collect(Collectors.toList()));
		obstacles.addAll(terrainObstacles);

		AStarSearch<MapPoint> search = new AStarSearch<>(new PathFinderNode(
			null, terrain.terrain, obstacles,
			terrain.terrain.w, terrain.terrain.h,
			start, target));

		List<MapPoint> r = search.search().stream()
			.map(n -> n.getPosition()).collect(Collectors.toList());
		if (r.size() >= 1 && !r.get(r.size() - 1).equals(target))
			return new ArrayList<>(); else return r;
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

