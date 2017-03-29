package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import isogame.engine.MapPoint;
import isogame.engine.SlopeType;
import isogame.engine.Stage;
import isogame.engine.Tile;
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
	public final Collection<Character> characters = new ArrayList<>();

	private final double[] revengeBonus = {0, 0.2, 0.4, 0.9};

	// superlist of all targetables (including characters and obstacles)
	public final Collection<Targetable> targetable = new ArrayList<>();

	// zone mapping
	private final Map<MapPoint, Zone> zoneMap = new HashMap<>();
	private final Collection<Zone> zones = new ArrayList<>();

	private final Set<MapPoint> terrainObstacles;

	public BattleState(Stage terrain, Collection<Character> characters) {
		this.trigger = new Trigger(this);
		this.terrain = terrain;
		this.characters.addAll(characters);
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
		RoadBlock r = new RoadBlock(p, sprites);
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
		Ability a, Optional<Integer> turns, Optional<RoadBlock> bind,
		Character agent
	) {
		// make sure that this zone doesn't overlap an existing zone
		if (range.stream().anyMatch(p -> zoneMap.containsKey(p))) return Optional.empty();

		Zone z = new Zone(centre, range, turns, agent.hasMana(), a, agent);
		zones.add(z);
		for (MapPoint p : range) {
			zoneMap.put(p, z);
			getTargetableAt(p).forEach(t -> t.currentZone = Optional.of(z));
		}

		bind.ifPresent(o -> o.bindZone(z));
		return Optional.of(z);
	}

	/**
	 * To be called once at the start of each turn.
	 * */
	public void notifyTurn() {
		for (Zone z : zones) z.notifyTurn();
	}

	public List<Zone> removeExpiredZones() {
		List<Zone> r = new ArrayList<>();

		for (Zone z : zones) {
			if (z.reap()) {
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

	private Optional<Player> resignedPlayer = Optional.empty();

	/**
	 * Register a resignation.
	 * */
	public void resign(Player player) {
		resignedPlayer = Optional.of(player);
	}

	/**
	 * Determine the outcome of the battle from the point of view of a player.
	 * */
	public Optional<BattleOutcome> getBattleOutcome(Player player) {
		if (resignedPlayer.isPresent()) {
			if (resignedPlayer.get() == player) {
				return Optional.of(BattleOutcome.RESIGN);
			} else {
				return Optional.of(BattleOutcome.OTHER_RESIGNED);
			}
		}

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
		MapPoint start, MapPoint target, Player player, int range
	) {
		List<MapPoint> path = findPath(start, target, player);
		if (canMoveRange(range, path)) return path; else return new ArrayList<>();
	}

	/**
	 * Get a valid prefix of a potentially invalid path.
	 * */
	public List<MapPoint> reduceToValidPath(List<MapPoint> path) {
		final List<MapPoint> r = new ArrayList<>();
		r.addAll(path);

		while (r.size() > 0 && !(isSpaceFree(r.get(r.size() - 1))))
			r.remove(r.size() - 1);

		return r;
	}

	/**
	 * Determine if a move path is valid.  A path is valid if it takes a
	 * character to an unoccupied square and isn't longer than the character's
	 * mp.
	 * */
	public boolean canMove(List<MapPoint> path) {
		if (path.size() < 2) return false;
		return getCharacterAt(path.get(0))
			.map(c ->
				canMoveRange(c.getMP(), path) &&
				path.stream().allMatch(p -> canMoveThrough(p, c.player))
			).orElse(false);
	}

	/**
	 * Calculate the mp cost of a valid path.
	 * */
	public int pathCost(List<MapPoint> path) {
		if (path.size() < 2) return 0;

		int totalCost = 0;

		MapPoint p0 = path.get(0);
		for (MapPoint p : path.subList(1, path.size())) {
			final MapPoint dp = p.subtract(p0);
			final Tile tile = terrain.terrain.getTile(p);
			if (dp.y == 0 && dp.x == 1) {
				totalCost += (tile.slope == SlopeType.E)? 2 : 1;
			} else if (dp.y == 0 && dp.x == -1) {
				totalCost += (tile.slope == SlopeType.W)? 2 : 1;
			} else if (dp.x == 0 && dp.y == 1)  {
				totalCost += (tile.slope == SlopeType.S)? 2 : 1;
			} else if (dp.x == 0 && dp.y == -1) {
				totalCost += (tile.slope == SlopeType.N)? 2 : 1;
			} else {
				totalCost += 1;
			}
			p0 = p;
		}

		return totalCost;
	}

	/**
	 * Determine if a move path is valid, using a fixed range instead of mp.
	 * */
	public boolean canMoveRange(int range, List<MapPoint> path) {
		if (path.size() < 2) return false;
		MapPoint target = path.get(path.size() - 1);
		return !spaceObstacles().contains(target) && pathCost(path) <= range;
	}

	/**
	 * Determine if it is possible to place a character in a particular tile.
	 * */
	public boolean isSpaceFree(MapPoint p) {
		return terrain.terrain.hasTile(p) &&
			!(terrainObstacles.contains(p) ||
				targetable.stream()
					.filter(t -> t.blocksSpace())
					.anyMatch(t -> t.getPos().equals(p)));
	}

	/**
	 * Determine if a player can move through a particular point.
	 * */
	public boolean canMoveThrough(MapPoint p, Player player) {
		return terrain.terrain.hasTile(p) && !movementObstacles(player).contains(p);
	}

	/**
	 * Points that are already occupied.
	 * */
	public Set<MapPoint> spaceObstacles() {
		Set<MapPoint> r = new HashSet<>(targetable.stream()
			.filter(c -> c.blocksSpace())
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
			Set<MapPoint> obstacles = getCharacterAt(castFrom)
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
	public Set<MapPoint> getAffectedArea(
		MapPoint agent, AbilityAgentType agentType,
		Ability ability, Casting casting
	) {
		final int radius = agentType == AbilityAgentType.ZONE? 0 : ability.info.range.radius;

		final Set<MapPoint> r =
			LineOfSight.getDiamond(casting.target, radius).stream()
				.filter(p -> terrain.terrain.hasTile(p) &&
					aoeMinimalPointCost(casting.target, p) <= radius)
				.collect(Collectors.toSet());

		if (agentType == AbilityAgentType.CHARACTER && ability.info.range.piercing) {
			Player player = getCharacterAt(agent).map(c -> c.player)
				.orElseThrow(() -> new RuntimeException(
					"Attempted to get targeting information for a non-existent character"));

			Set<MapPoint> pr = new HashSet<>(r);
			Collection<MapPoint> los =
				LineOfSight.getLOS(casting.castFrom, casting.target, true);
			if (los != null) pr.addAll(los);
			return pr;
		}

		return r;
	}

	public int aoeMinimalPointCost(MapPoint castFrom, MapPoint target) {
		return Math.min(aoePointCost(castFrom, target, true),
			aoePointCost(castFrom, target, false));
	}

	/**
	 * The cost in range points to reach a particular point using are area of
	 * affect.
	 * */
	public int aoePointCost(MapPoint castFrom, MapPoint target, boolean bias) {
		if (castFrom.equals(target)) return 0;
		List<MapPoint> path = LineOfSight.getLOS(castFrom, target, bias);
		if (path.size() < 2) return Integer.MAX_VALUE;

		int totalCost = 0;
		Tile tile0 = terrain.terrain.getTile(path.get(0));
		for (MapPoint p : path.subList(1, path.size())) {
			final Tile tile = terrain.terrain.getTile(p);
			final int elevation0 = tile0.elevation + (tile0.slope == SlopeType.NONE? 0 : 1);
			final int elevation = tile.elevation + (tile.slope == SlopeType.NONE? 0 : 1);
			if (elevation > elevation0) totalCost += 2; else totalCost += 1;
			tile0 = tile;
		}
		return totalCost;
	}


	public Collection<Targetable> getAbilityTargets(
		MapPoint agent, AbilityAgentType agentType,
		Ability ability, Set<MapPoint> area
	) {
		return getAgentAt(agent, agentType).map(a ->
			area.stream()
				.flatMap(p -> getTargetableAt(p).stream())
				.filter(t -> ability.canTarget(
					a instanceof HasParentAgent? ((HasParentAgent) a).getParent() : a, t))
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

