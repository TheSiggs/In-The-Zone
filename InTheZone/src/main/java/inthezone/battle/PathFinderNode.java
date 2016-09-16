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
		Node<MapPoint> parent,
		StageInfo terrain,
		Set<MapPoint> obstacles,
		MapPoint start,
		MapPoint goal
	) {
		super(parent, start, goal);
		this.terrain = terrain;
		this.obstacles = obstacles;
		this.width = terrain.w;
		this.height = terrain.h;
	}

	@Override
	public Node<MapPoint> getGoalNode() {
		MapPoint goal = this.getGoal();

		return new PathFinderNode(
			this.getParent(),
			terrain,
			this.obstacles,
			goal,
			goal);
	}

	@Override
	public Set<Node<MapPoint>> getAdjacentNodes() {
		Set<Node<MapPoint>> nodes = new TreeSet<>();

		MapPoint pos = this.getPosition();
		MapPoint goal = this.getGoal();
		MapPoint n = new MapPoint(pos.x, pos.y - 1);
		MapPoint s = new MapPoint(pos.x, pos.y + 1);
		MapPoint e = new MapPoint(pos.x + 1, pos.y);
		MapPoint w = new MapPoint(pos.x - 1, pos.y);

		Node<MapPoint> parent = this.getParent();
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

	private PathFinderNode nextNode(MapPoint x) {
		return new PathFinderNode(this, terrain, obstacles, x, getGoal());
	}

	private boolean canTraverseBoundary(MapPoint from, MapPoint to, SlopeType slope) {
		return canTraverseBoundary(from, to, slope, terrain);
	}

	public static boolean canTraverseBoundary(
		MapPoint from, MapPoint to, StageInfo terrain
	) {
		MapPoint dp = from.subtract(to).normalise();
		SlopeType slope;
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
		MapPoint from, MapPoint to, SlopeType slope, StageInfo terrain
	) {
		Tile tfrom = terrain.getTile(from);
		Tile tto = terrain.getTile(to);

		if (tfrom.slope != SlopeType.NONE) {
			return
				(slope == tfrom.slope && tto.elevation - tfrom.elevation == 1) ||
				(slope == tfrom.slope.opposite() && tto.elevation == tfrom.elevation);
		} else if (tfrom.elevation == tto.elevation) {
			return tto.slope == SlopeType.NONE || tto.slope == slope;
		} else if (tfrom.elevation - tto.elevation == 1) {
			return tto.slope == slope.opposite();
		} else if (tfrom.elevation - tto.elevation == -1) {
			return tto.slope == slope;
		} else return false;
	}

	@Override
	public int compareTo(Node<MapPoint> other) {
		if (this.getCost() != other.getCost()) {
			return super.compareTo(other);

		} else {
			if (this.equals(other)) {
				return 0;

			} else {
				MapPoint n1 = this.getPosition();
				MapPoint n2 = other.getPosition();
				return (n1.x == n2.x) ? (n1.y - n2.y) : (n1.x - n2.x);
			}
		}
	}

	@Override
	public double g() {
		Node<MapPoint> parent = getParent();
		if (parent == null) return 1.0; else return parent.g() + 1.0;
	}

	@Override
	public double h(MapPoint goal) {
		MapPoint here = this.getPosition();
		double xDiff = goal.x - here.x;
		double yDiff = goal.y - here.y;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	@Override
	public String toString() {
		MapPoint here = this.getPosition();
		return "(" + here.x + ", " + here.y + ")";
	}
}

