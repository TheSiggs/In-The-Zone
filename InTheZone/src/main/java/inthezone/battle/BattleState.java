package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.dcoder.ai.astar.AStarSearch;

/**
 * This class keeps track of the state of the battle.  It does not know how to
 * perform high level battle operations.
 * */
public class BattleState {
	public final Trigger trigger;

	public final Stage terrain;
	public final Collection<Character> characters;

	private final double[] revengeBonus = {0, 0.2, 0.4, 0.9};

	// superlist of all targetables (including characters and obstacles)
	public final Collection<Targetable> targetable;

	// zone mapping
	private final Map<MapPoint, Zone> zoneMap = new HashMap<>();
	private final Collection<Zone> zones = new ArrayList<>();

	private final Set<MapPoint> terrainObstacles;

	public BattleState(Stage terrain, Collection<Character> characters) {
		this.trigger = new Trigger(this);
		this.terrain = terrain;
		this.characters = characters;
		this.targetable = new ArrayList<>();
		this.targetable.addAll(characters);

		terrainObstacles = new HashSet<>(terrain.allSprites.stream()
			.map(s -> s.pos).collect(Collectors.toList()));
	}

	/**
	 * Get the revenge bonus for a player
	 * */
	public double getRevengeBonus(Player p) {
		long cs = characters.stream().filter(c -> c.player == p).count();
		if (cs == 0 || cs > 4) return 0; else return revengeBonus[(int) (4 - cs)];
	}

	/**
	 * Determine if a terrain tile is a mana zone
	 * */
	public boolean hasMana(MapPoint p) {
		return terrain.terrain.hasTile(p) &&
			terrain.terrain.getTile(p).isManaZone;
	}

	/**
	 * Place a new obstacle.
	 * */
	public RoadBlock placeObstacle(MapPoint p, StandardSprites sprites) {
		RoadBlock r = new RoadBlock(p, Optional.empty(), sprites);
		targetable.add(r);
		return r;
	}

	/**
	 * Place a new trap
	 * */
	public Trap placeTrap(
		MapPoint p, Ability a, Character agent, StandardSprites sprites
	) {
		Trap t = new Trap(p, agent.hasMana(), a, agent, sprites);
		targetable.add(t);
		return t;
	}

	/**
	 * Place a new zone
	 * */
	public Optional<Zone> placeZone(
		MapPoint centre, Collection<MapPoint> range,
		Ability a, int turns, Character agent
	) {
		// make sure that this zone doesn't overlap an existing zone
		if (range.stream().anyMatch(p -> zoneMap.containsKey(p))) return Optional.empty();

		Zone z = new Zone(centre, range, Optional.of(turns), agent.hasMana(), a, agent);
		zones.add(z);
		for (MapPoint p : range) zoneMap.put(p, z);
		return Optional.of(z);
	}

	/**
	 * To be called once at the start of each turn to remove expired zones.
	 * */
	public List<Zone> removeExpiredZones() {
		List<Zone> r = new ArrayList<>();

		for (Zone z : zones) {
			if (z.canRemoveNow()) {
				r.add(z);
				for (MapPoint p : z.range) zoneMap.remove(p);
			}
		}

		zones.removeAll(r);
		return r;
	}

	/**
	 * Remove a targetable object.
	 * */
	public void removeObstacle(Targetable t) {
		targetable.remove(t);
	}

	/**
	 * Determine the outcome of the battle from the point of view of a player.
	 * */
	public Optional<BattleOutcome> getBattleOutcome(Player player) {
		boolean playerDead = characters.stream()
			.filter(c -> c.player == player)
			.allMatch(c -> c.isDead());

		boolean otherPlayerDead = characters.stream()
			.filter(c -> c.player != player)
			.allMatch(c -> c.isDead());

		if (playerDead && otherPlayerDead) {
			return Optional.of(BattleOutcome.DRAW);
		} else if (playerDead) {
			return Optional.of(BattleOutcome.LOSE);
		} else if (otherPlayerDead) {
			return Optional.of(BattleOutcome.WIN);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Get targetable objects at a particular point (if there is one).
	 * */
	public List<? extends Targetable> getTargetableAt(MapPoint x) {
		List<Targetable> r = targetable.stream()
			.filter(t -> t.getPos().equals(x)).collect(Collectors.toList());
		if (zoneMap.containsKey(x)) r.add(zoneMap.get(x));
		return r;
	}

	/**
	 * Get the trap at a particular point (if there is one).
	 * */
	public Optional<Trap> getTrapAt(MapPoint x) {
		return targetable.stream()
			.filter(t -> t instanceof Trap && t.getPos().equals(x))
			.findFirst().map(t -> (Trap) t);
	}

	/**
	 * Get the zone at a particular point (if there is one).
	 * */
	public Optional<Zone> getZoneAt(MapPoint x) {
		return Optional.ofNullable(zoneMap.get(x));
	}

	/**
	 * Get the agent of an ability
	 * */
	public Optional<? extends Targetable> getAgentAt(MapPoint x, AbilityAgentType agentType) {
		switch (agentType) {
			case CHARACTER: return getCharacterAt(x);
			case TRAP: return getTrapAt(x);
			case ZONE: return getZoneAt(x);
			default: return Optional.empty();
		}
	}

	/**
	 * Get the character at a particular point (if there is one).
	 * */
	public Optional<Character> getCharacterAt(MapPoint x) {
		return characters.stream().filter(c -> c.getPos().equals(x)).findFirst();
	}

	/**
	 * Get a copy of the characters list.  Prevents unintentional and potentially
	 * unsafe mutation of the original character data.
	 * */
	public List<Character> cloneCharacters() {
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

	/**
	 * Determine if it is possible to place a character in a particular tile.
	 * */
	public boolean isSpaceFree(MapPoint p) {
		return terrain.terrain.hasTile(p) &&
			!(terrainObstacles.contains(p) ||
				targetable.stream().anyMatch(c -> c.getPos().equals(p)));
	}

	/**
	 * Points that are already occupied.
	 * */
	public Set<MapPoint> spaceObstacles(Player player) {
		Set<MapPoint> r = new HashSet<>(targetable.stream()
			.filter(c -> c.blocksSpace(player))
			.map(c -> c.getPos()).collect(Collectors.toList()));
		r.addAll(terrainObstacles);
		return r;
	}

	public Set<MapPoint> allObstacles() {
		Set<MapPoint> r = new HashSet<>(targetable.stream()
			.map(c -> c.getPos()).collect(Collectors.toList()));
		r.addAll(terrainObstacles);
		return r;
	}

	/**
	 * Obstacles that cannot be moved through.
	 * */
	public Set<MapPoint> movementObstacles(Player player) {
		Set<MapPoint> obstacles = new HashSet<>(targetable.stream()
			.filter(c -> c.blocksPath(player))
			.map(c -> c.getPos()).collect(Collectors.toList()));
		obstacles.addAll(terrainObstacles);
		return obstacles;
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

		Set<MapPoint> obstacles = movementObstacles(player);
		
		AStarSearch<MapPoint> search = new AStarSearch<>(new PathFinderNode(
			null, terrain.terrain, obstacles, start, target));

		List<MapPoint> r = search.search().stream()
			.map(n -> n.getPosition()).collect(Collectors.toList());
		if (r.size() >= 1 && !r.get(r.size() - 1).equals(target))
			return new ArrayList<>(); else return r;
	}

	/**
	 * Attempt to get a valid LOS path.  Returns null if there is no such path.
	 * */
	private List<MapPoint> getLOS(
		MapPoint from, MapPoint to, Set<MapPoint> obstacles
	) {
		if (!terrain.terrain.hasTile(from) || !terrain.terrain.hasTile(to)) return null;

		List<MapPoint> los1 = LineOfSight.getLOS(from, to, true);
		List<MapPoint> los2 = LineOfSight.getLOS(from, to, false);
		los1.remove(los1.size() - 1); // we only need LOS up to the square
		los2.remove(los2.size() - 1); // the square itself may be blocked

		if (!los1.stream().anyMatch(lp -> obstacles.contains(lp))) {
			los1.add(to);
			return los1;
		} else if (!los2.stream().anyMatch(lp -> obstacles.contains(lp))) {
			los2.add(to);
			return los2;
		} else {
			return null;
		}
	}

	/**
	 * Get all the tiles that can be targeted by an ability.
	 * */
	public Collection<MapPoint> getTargetableArea(
		MapPoint agent, MapPoint castFrom, Ability ability
	) {
		Collection<MapPoint> diamond =
			LineOfSight.getDiamond(castFrom, ability.info.range.range);

		if (!ability.info.range.los) {
			return diamond;
		} else {
			Set<MapPoint> obstacles = getCharacterAt(agent)
				.map(c -> movementObstacles(c.player))
				.orElse(allObstacles());
			
			// check line of sight
			return diamond.stream().filter(p ->
				getLOS(castFrom, p, obstacles) != null).collect(Collectors.toList());
		}
	}

	/**
	 * Get all the tiles that will be affected by an ability.
	 * */
	public Collection<MapPoint> getAffectedArea(
		MapPoint agent, AbilityAgentType agentType,
		MapPoint castFrom, Ability ability, MapPoint target
	) {
		int radius = agentType == AbilityAgentType.ZONE? 0 : ability.info.range.radius;

		Collection<MapPoint> r =
			LineOfSight.getDiamond(target, radius).stream()
				.filter(p -> terrain.terrain.hasTile(p))
				.collect(Collectors.toList());

		if (agentType == AbilityAgentType.CHARACTER && ability.info.range.piercing) {
			Player player = getCharacterAt(agent).map(c -> c.player)
				.orElseThrow(() -> new RuntimeException(
					"Attempted to get targeting information for a non-existent character"));

			Set<MapPoint> pr = new HashSet<>(r);
			Collection<MapPoint> los = getLOS(castFrom, target, movementObstacles(player));
			if (los != null) pr.addAll(los);
			return pr;
		}

		return r;
	}

	public Collection<Targetable> getAbilityTargets(
		MapPoint agent, AbilityAgentType agentType,
		MapPoint castFrom, Ability ability, MapPoint target
	) {
		return getAgentAt(agent, agentType).map(a ->
			getAffectedArea(agent, agentType, castFrom, ability, target).stream()
				.flatMap(p -> getTargetableAt(p).stream())
				.filter(t -> ability.canTarget(a, t))
				.collect(Collectors.toList())
		).orElse(new ArrayList<>());
	}

	public Collection<MapPoint> getItemArea(MapPoint p) {
		Item item = new HealthPotion();
		return LineOfSight.getDiamond(p, 1).stream()
			.filter(t -> terrain.terrain.hasTile(t) && item.canAffect(this, t))
			.collect(Collectors.toList());
	}
}

