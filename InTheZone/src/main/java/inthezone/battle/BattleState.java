package inthezone.battle;

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

import nz.dcoder.ai.astar.AStarSearch;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.ResignReason;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;

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

	private int turn = 0;
	public int getTurnNumber() { return turn; }

	public BattleState(
		final Stage terrain,
		final Collection<Character> characters
	) {
		this.trigger = new Trigger(this);
		this.terrain = terrain;
		this.characters.addAll(characters);
		this.targetable.addAll(characters);

		terrainObstacles = new HashSet<>(terrain.allSprites.stream()
			.map(s -> s.getPos()).collect(Collectors.toList()));

		updateRevengeBonus();
	}

	/**
	 * Get the revenge bonus for a player.
	 * */
	public double getRevengeBonus(Player p) {
		final long cs = characters.stream()
			.filter(c -> c.player == p && !c.isDead()).count();
		if (cs == 0 || cs > 4) return 0; else return revengeBonus[(int) (4 - cs)];
	}

	/**
	 * Update the revenge bonus parameter of each character.
	 * */
	public void updateRevengeBonus() {
		final double playerARevenge = getRevengeBonus(Player.PLAYER_A);
		final double playerBRevenge = getRevengeBonus(Player.PLAYER_B);
		for (Character c : characters) {
			switch (c.player) {
				case PLAYER_A: c.revengeBonus = playerARevenge; break;
				case PLAYER_B: c.revengeBonus = playerBRevenge; break;
			}
		}
	}

	/**
	 * Determine if a terrain tile is a mana zone
	 * */
	public boolean hasMana(final MapPoint p) {
		return terrain.terrain.hasTile(p) &&
			terrain.terrain.getTile(p).isManaZone;
	}

	/**
	 * Place a new obstacle.
	 * */
	public RoadBlock placeObstacle(
		final MapPoint p,
		final Optional<AbilityInfo> a,
		final StandardSprites sprites
	) {
		final RoadBlock r = new RoadBlock(p, a, sprites);
		targetable.add(r);
		return r;
	}

	/**
	 * Place a new trap
	 * */
	public Trap placeTrap(
		final MapPoint p,
		final Ability a,
		final Character agent,
		final StandardSprites sprites
	) {
		final Trap t = new Trap(p, agent.hasMana(), a, agent, sprites);
		targetable.add(t);
		return t;
	}

	/**
	 * Place a new zone
	 * */
	public Optional<Zone> placeZone(
		final MapPoint centre,
		final Collection<MapPoint> range,
		final Ability a,
		final Optional<Integer> turns,
		final Optional<RoadBlock> bind,
		final Character agent
	) {
		// make sure that this zone doesn't overlap an existing zone
		if (range.stream().anyMatch(p -> zoneMap.containsKey(p)))
			return Optional.empty();

		final Zone z = new Zone(centre, range, turns, agent.hasMana(), a, agent);
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
		turn += 1;
		for (Zone z : zones) z.notifyTurn();
	}

	public List<Zone> removeExpiredZones() {
		final List<Zone> r = new ArrayList<>();

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
	public void removeObstacle(final Targetable t) {
		targetable.remove(t);
	}

	private Optional<Player> resignedPlayer = Optional.empty();
	private ResignReason reason = null;

	/**
	 * Register a resignation.
	 * */
	public void resign(final Player player, final ResignReason reason) {
		this.resignedPlayer = Optional.of(player);
		this.reason = reason;
	}

	/**
	 * Determine the outcome of the battle from the point of view of a player.
	 * */
	public Optional<BattleOutcome> getBattleOutcome(final Player player) {
		if (player == Player.PLAYER_OBSERVER)
			return getBattleOutcome(Player.PLAYER_A);

		if (resignedPlayer.isPresent()) {
			switch (reason) {
				case RESIGNED:
					if (resignedPlayer.get() == player) {
						return Optional.of(BattleOutcome.RESIGN);
					} else {
						return Optional.of(BattleOutcome.OTHER_RESIGNED);
					}
				case LOGGED_OFF:
					return Optional.of(BattleOutcome.OTHER_LOGGED_OUT);
				case ERROR:
					if (resignedPlayer.get() == player) {
						return Optional.of(BattleOutcome.ERROR);
					} else {
						return Optional.of(BattleOutcome.OTHER_ERROR);
					}
				default:
					throw new RuntimeException("Unknown resign type: " + reason);
			}
		}

		final boolean playerDead = characters.stream()
			.filter(c -> c.player == player)
			.allMatch(c -> c.isDead());

		final boolean otherPlayerDead = characters.stream()
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
	public List<? extends Targetable> getTargetableAt(final MapPoint x) {
		final List<Targetable> r = targetable.stream()
			.filter(t -> t.getPos().equals(x)).collect(Collectors.toList());
		if (zoneMap.containsKey(x)) r.add(zoneMap.get(x));
		return r;
	}

	/**
	 * Get the trap at a particular point (if there is one).
	 * */
	public Optional<Trap> getTrapAt(final MapPoint x) {
		return targetable.stream()
			.filter(t -> t instanceof Trap && t.getPos().equals(x))
			.findFirst().map(t -> (Trap) t);
	}

	/**
	 * Get the zone at a particular point (if there is one).
	 * */
	public Optional<Zone> getZoneAt(final MapPoint x) {
		return Optional.ofNullable(zoneMap.get(x));
	}

	private static final Optional<InstantEffectInfo> defuseEffect = Optional.of(
		new InstantEffectInfo(InstantEffectType.DEFUSE, 0));

	/**
	 * Determine if there is a defusing zone at a particular point.
	 * */
	public boolean hasDefusingZone(final MapPoint x) {
		return getZoneAt(x).map(z ->
			z.ability.info.instantBefore.equals(defuseEffect) ||
			z.ability.info.instantAfter.equals(defuseEffect)).orElse(false);
	}

	/**
	 * Get the agent of an ability
	 * */
	public Optional<? extends Targetable> getAgentAt(
		final MapPoint x,
		final AbilityAgentType agentType
	) {
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
	public Optional<Character> getCharacterAt(final MapPoint x) {
		return characters.stream().filter(c -> c.getPos().equals(x)).findFirst();
	}

	public Optional<Character> getCharacterById(int id) {
		return characters.stream().filter(c -> c.id == id).findFirst();
	}

	/**
	 * Find a path that is guaranteed to be valid
	 * @return The empty list if there is no valid path
	 * */
	public List<MapPoint> findValidPath(
		final MapPoint start,
		final MapPoint target,
		final Player player,
		final int range
	) {
		final List<MapPoint> path = findPath(start, target, player);
		if (canMoveRange(range, path)) return path; else return new ArrayList<>();
	}

	/**
	 * Find all the possible paths from the source to the target, ordering them
	 * by desirability.
	 * */
	public List<List<MapPoint>> findAllValidPaths(
		final MapPoint start,
		final MapPoint target,
		final Character subject,
		final int maxCost
	) {
		return pathGenerator.generateAll(start, target, subject, maxCost);
	}

	private PathGenerator pathGenerator = new PathGenerator(this);

	/**
	 * Get a valid prefix of a potentially invalid path.
	 * */
	public List<MapPoint> reduceToValidPath(final List<MapPoint> path) {
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
	public boolean canMove(final List<MapPoint> path) {
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
	public int pathCost(final List<MapPoint> path) {
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
	public boolean canMoveRange(
		final int range, final List<MapPoint> path
	) {
		if (path.size() < 2) return false;
		final MapPoint target = path.get(path.size() - 1);
		return !spaceObstacles().contains(target) && pathCost(path) <= range;
	}

	/**
	 * Determine if it is possible to place a character in a particular tile.
	 * */
	public boolean isSpaceFree(final MapPoint p) {
		return terrain.terrain.hasTile(p) &&
			!(terrainObstacles.contains(p) ||
				targetable.stream()
					.filter(t -> t.blocksSpace())
					.anyMatch(t -> t.getPos().equals(p)));
	}

	/**
	 * Determine if a player can move through a particular point.
	 * */
	public boolean canMoveThrough(final MapPoint p, final Player player) {
		return terrain.terrain.hasTile(p) && !movementObstacles(player).contains(p);
	}

	/**
	 * Points that are already occupied.
	 * */
	public Set<MapPoint> spaceObstacles() {
		final Set<MapPoint> r = new HashSet<>(targetable.stream()
			.filter(c -> c.blocksSpace())
			.map(c -> c.getPos()).collect(Collectors.toList()));
		r.addAll(terrainObstacles);
		return r;
	}

	public Set<MapPoint> allObstacles() {
		final Set<MapPoint> r = new HashSet<>(targetable.stream()
			.map(c -> c.getPos()).collect(Collectors.toList()));
		r.addAll(terrainObstacles);
		return r;
	}

	/**
	 * Obstacles that cannot be moved through.
	 * */
	public Set<MapPoint> movementObstacles(final Player player) {
		final Set<MapPoint> obstacles = new HashSet<>(targetable.stream()
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
		final MapPoint start, final MapPoint target, final Player player
	) {
		if (start.equals(target)) return new ArrayList<>();

		final Set<MapPoint> obstacles = movementObstacles(player);
		
		final AStarSearch<MapPoint> search = new AStarSearch<>(
			new PathFinderNode(null, terrain.terrain, obstacles, start, target));

		final List<MapPoint> r = search.search().stream()
			.map(n -> n.getPosition()).collect(Collectors.toList());
		if (r.size() >= 1 && !r.get(r.size() - 1).equals(target))
			return new ArrayList<>(); else return r;
	}

	/**
	 * Attempt to get a valid LOS path.  Returns null if there is no such path.
	 * */
	private List<MapPoint> getLOS(
		final MapPoint from, final MapPoint to, final Set<MapPoint> obstacles
	) {
		if (!terrain.terrain.hasTile(from) || !terrain.terrain.hasTile(to))
			return null;

		final List<MapPoint> los1 = LineOfSight.getLOS(from, to, true);
		final List<MapPoint> los2 = LineOfSight.getLOS(from, to, false);
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
		final MapPoint agent, final MapPoint castFrom, final Ability ability
	) {
		final Collection<MapPoint> diamond =
			LineOfSight.getDiamond(castFrom, ability.info.range.range).stream()
			.filter(p -> !terrainObstacles.contains(p))
			.collect(Collectors.toList());

		if (!ability.info.range.los) {
			return diamond;
		} else {
			final Set<MapPoint> obstacles = getCharacterAt(castFrom)
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
		final MapPoint agent, final AbilityAgentType agentType,
		final Ability ability, final Casting casting
	) {
		final int radius =
			agentType == AbilityAgentType.ZONE? 0 : ability.info.range.radius;

		final Set<MapPoint> r =
			LineOfSight.getDiamond(casting.target, radius).stream()
				.filter(p -> terrain.terrain.hasTile(p) &&
					aoeMinimalPointCost(casting.target, p) <= radius)
				.collect(Collectors.toSet());

		if (agentType == AbilityAgentType.CHARACTER && ability.info.range.piercing) {
			final Player player = getCharacterAt(agent).map(c -> c.player)
				.orElseThrow(() -> new RuntimeException(
					"Attempted to get targeting information for a non-existent character"));

			final Set<MapPoint> pr = new HashSet<>(r);
			final Collection<MapPoint> los =
				LineOfSight.getLOS(casting.castFrom, casting.target, true);
			if (los != null) pr.addAll(los);
			return pr;
		}

		return r;
	}

	public int aoeMinimalPointCost(
		final MapPoint castFrom, final MapPoint target
	) {
		return Math.min(aoePointCost(castFrom, target, true),
			aoePointCost(castFrom, target, false));
	}

	/**
	 * The cost in range points to reach a particular point using are area of
	 * affect.
	 * */
	public int aoePointCost(
		final MapPoint castFrom, final MapPoint target, final boolean bias
	) {
		if (castFrom.equals(target)) return 0;
		final List<MapPoint> path = LineOfSight.getLOS(castFrom, target, bias);
		if (path.size() < 2 || !terrain.terrain.hasTile(path.get(0)))
			return Integer.MAX_VALUE;
		

		int totalCost = 0;
		Tile tile0 = terrain.terrain.getTile(path.get(0));
		for (MapPoint p : path.subList(1, path.size())) {
			final Tile tile = terrain.terrain.getTile(p);
			final int elevation0 = tile0.elevation +
				(tile0.slope == SlopeType.NONE? 0 : 1);
			final int elevation = tile.elevation +
				(tile.slope == SlopeType.NONE? 0 : 1);
			if (elevation > elevation0) totalCost += 2; else totalCost += 1;
			tile0 = tile;
		}
		return totalCost;
	}


	public Collection<Targetable> getAbilityTargets(
		final MapPoint agent, final AbilityAgentType agentType,
		final Ability ability, final Set<MapPoint> area
	) {
		return getAgentAt(agent, agentType).map(a ->
			area.stream()
				.flatMap(p -> getTargetableAt(p).stream())
				.filter(t -> ability.canTarget(a, t) && ability.canAffect(t))
				.collect(Collectors.toList())
		).orElse(new ArrayList<>());
	}

	public Collection<MapPoint> getItemArea(final MapPoint p) {
		final Item item = new HealthPotion();
		return LineOfSight.getDiamond(p, 1).stream()
			.filter(t -> terrain.terrain.hasTile(t) && item.canAffect(this, t))
			.collect(Collectors.toList());
	}
}

