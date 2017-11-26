package inthezone.battle;

import isogame.engine.MapPoint;
import isogame.engine.SlopeType;
import isogame.engine.StageInfo;
import isogame.engine.Tile;
import java.util.Set;
import java.util.TreeSet;
import nz.dcoder.ai.astar.Node;

public class PathFinderNode extends Node<MapPoint> {
	private final StageInfo terrain;
	private final Set<MapPoint> obstacles;
	private final int width;
	private final int height;

	public PathFinderNode(
		final Node<MapPoint> parent,
		final StageInfo terrain,
		final Set<MapPoint> obstacles,
		final MapPoint start,
		final MapPoint goal
	) {
		super(parent, start, goal);
		this.terrain = terrain;
		this.obstacles = obstacles;
		this.width = terrain.w;
		this.height = terrain.h;
	}

	@Override
	public Node<MapPoint> getGoalNode() {
		final MapPoint goal = this.getGoal();

		return new PathFinderNode(
			this.getParent(),
			terrain,
			this.obstacles,
			goal,
			goal);
	}

	@Override
	public Set<Node<MapPoint>> getAdjacentNodes() {
		final Set<Node<MapPoint>> nodes = new TreeSet<>();

		final MapPoint pos = this.getPosition();
		final MapPoint goal = this.getGoal();
		final MapPoint n = new MapPoint(pos.x, pos.y - 1);
		final MapPoint s = new MapPoint(pos.x, pos.y + 1);
		final MapPoint e = new MapPoint(pos.x + 1, pos.y);
		final MapPoint w = new MapPoint(pos.x - 1, pos.y);

		final Node<MapPoint> parent = this.getParent();
		if (!(parent != null && parent.getPosition().equals(pos))) {
			if (n.y >= 0 && !obstacles.contains(n) && canTraverseBoundary(pos, n, SlopeType.N)) {
				nodes.add(nextNode(n));
			}
			if (s.y < height && !obstacles.contains(s) && canTraverseBoundary(pos, s, SlopeType.S)) {
				nodes.add(nextNode(s));
			}
			if (e.x < width && !obstacles.contains(e) && canTraverseBoundary(pos, e, SlopeType.E)) {
				nodes.add(nextNode(e));
			}
			if (w.x >= 0 && !obstacles.contains(w) && canTraverseBoundary(pos, w, SlopeType.W)) {
				nodes.add(nextNode(w));
			}
		}

		return nodes;
	}

	private PathFinderNode nextNode(final MapPoint x) {
		return new PathFinderNode(this, terrain, obstacles, x, getGoal());
	}

	private boolean canTraverseBoundary(final MapPoint from, final MapPoint to, final SlopeType slope) {
		return canTraverseBoundary(from, to, slope, terrain);
	}

	public static boolean canTraverseBoundary(
		final MapPoint from, final MapPoint to, final StageInfo terrain
	) {
		final MapPoint dp = to.subtract(from).normalise();
		final SlopeType slope;
		if (dp.x == 1) slope = SlopeType.E;
		else if (dp.x == -1) slope = SlopeType.W;
		else if (dp.y == 1) slope = SlopeType.S;
		else if (dp.y == -1) slope = SlopeType.N;
		else slope = SlopeType.NONE;
		return canTraverseBoundary(from, to, slope, terrain);
	}

	/**
	 * Determine if the elevation rules allow us to pass from one point to another.
	 * */
	private static boolean canTraverseBoundary(
		final MapPoint from,
		final MapPoint to,
		final SlopeType slope,
		final StageInfo terrain
	) {
		final Tile tfrom = terrain.getTile(from);
		final Tile tto = terrain.getTile(to);

		return
			(tfrom.elevation > tto.elevation) ||
			(tfrom.elevation == tto.elevation &&
				(tto.slope == SlopeType.NONE || tto.slope == slope || tto.slope == tfrom.slope)) ||
			((tto.elevation - tfrom.elevation) == 1 && tfrom.slope == slope);
	}

	@Override
	public int compareTo(final Node<MapPoint> other) {
		if (this.getCost() != other.getCost()) {
			return super.compareTo(other);

		} else {
			if (this.equals(other)) {
				return 0;

			} else {
				final MapPoint n1 = this.getPosition();
				final MapPoint n2 = other.getPosition();
				return (n1.x == n2.x) ? (n1.y - n2.y) : (n1.x - n2.x);
			}
		}
	}

	@Override
	public double g() {
		final Node<MapPoint> parent = getParent();
		if (parent == null) return 1.0; else return parent.g() + 1.0;
	}

	@Override
	public double h(final MapPoint goal) {
		final MapPoint here = this.getPosition();
		final double xDiff = goal.x - here.x;
		final double yDiff = goal.y - here.y;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	@Override
	public String toString() {
		final MapPoint here = this.getPosition();
		return "(" + here.x + ", " + here.y + ")";
	}
}

