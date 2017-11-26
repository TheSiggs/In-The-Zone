package inthezone.battle;

import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate and rank paths
 * */
public class PathGenerator {
	final BattleState battle;
	public PathGenerator(final BattleState battle) {
		this.battle = battle;
	}

	/**
	 * Generate all paths and rank them
	 * @param source the start point
	 * @param target the end point
	 * @param maxCost the maximum cost of the path
	 * @return a ranked list of the paths
	 * */
	public List<List<MapPoint>> generateAll(
		final MapPoint source,
		final MapPoint target,
		final Character player,
		int maxCost
	) {
		final List<MapPoint> init = new ArrayList<>();
		init.add(source);
		return extend(init, target, maxCost)
			.filter(p -> battle.canMove(p))
			.sorted(Comparator.comparingDouble(rankPath(player)))
			.collect(Collectors.toList());
	}

	private static final MapPoint UP = new MapPoint(0, -1);
	private static final MapPoint DOWN = new MapPoint(0, 1);
	private static final MapPoint LEFT = new MapPoint(-1, 0);
	private static final MapPoint RIGHT = new MapPoint(1, 0);

	/**
	 * Generate all the valid extensions of a path (does not rank)
	 * @param start the initial path.  MUST NOT be empty.
	 * @param target the end point
	 * @param maxCost the maximum total cost of any path
	 * @return all the valid extentions
	 * */
	private Stream<List<MapPoint>> extend(
		final List<MapPoint> start, final MapPoint target, int maxCost
	) {
		final MapPoint last = start.get(start.size() - 1);

		if (last.equals(target)) {
			return Stream.of(start);
		} else {
			final List<List<MapPoint>> extensions = new ArrayList<>();

			final Consumer<List<MapPoint>> addExtension = p -> {
				if (battle.pathCost(p) <= maxCost) extensions.add(p);
			};

			extend1(start, last.add(UP)).ifPresent(addExtension);
			extend1(start, last.add(DOWN)).ifPresent(addExtension);
			extend1(start, last.add(LEFT)).ifPresent(addExtension);
			extend1(start, last.add(RIGHT)).ifPresent(addExtension);

			return extensions.stream().flatMap(p -> extend(p, target, maxCost));
		}
	}

	/**
	 * Extend a path by one node.
	 * @return a new path if it is possible to do so
	 * */
	private Optional<List<MapPoint>> extend1(
		final List<MapPoint> path, final MapPoint extn
	) {
		final MapPoint last = path.get(path.size() - 1);
		if (!path.contains(extn) && battle.terrain.terrain.hasTile(extn) &&
			PathFinderNode.canTraverseBoundary(last, extn, battle.terrain.terrain)
		) {
			final List<MapPoint> r = new ArrayList<>(path);
			r.add(extn);
			return Optional.of(r);
		} else {
			return Optional.empty();
		}
	}

	private static final double trapCost = 10d;
	private static final double zoneCost = 10d;

	private ToDoubleFunction<List<MapPoint>> rankPath(final Character player) {
		return p -> {
			final MapPoint first = p.get(0);
			final MapPoint last = p.get(p.size() - 1);
			final MapPoint d = last.subtract(first);
			final double dx = d.x;
			final double dy = d.y;
			final double dtmax = p.size() - 1;

			double totalCost = battle.pathCost(p);

			for (int i = 0; i < p.size(); i++) {
				final MapPoint pt = p.get(i);
				final double t = ((double) i) / dtmax;

				double directness =
					Math.sqrt(
					Math.pow((dx * t) + ((double) first.x) - ((double) pt.x), 2) +
					Math.pow((dy * t) + ((double) first.y) - ((double) pt.y), 2));

				double trapHazard = battle.getTrapAt(pt).map(trap ->
					trap.hurtsPlayer(player)? trapCost : 0d).orElse(0d);

				double zoneHazard = battle.getZoneAt(pt).map(zone ->
					zone.hurtsPlayer(player)? zoneCost : 0d).orElse(0d);

				totalCost += directness + trapHazard + zoneHazard;
			}

			return totalCost;
		};
	}
}

