package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.ai.astar.Node;

import java.util.Set;
import java.util.TreeSet;

public class AStarPositionNode extends Node<Position> {
	private final Set<Position> obstacles;
	private final int width;
	private final int height;

	public AStarPositionNode(
		Node<Position> parent,
		Set<Position> obstacles,
		int width,
		int height,
		Position start,
		Position goal
	) {
		super(parent, start, goal);
		this.obstacles = obstacles;
		this.width = width;
		this.height = height;
	}

	@Override
	public Node<Position> getGoalNode() {
		Position goal = this.getGoal();

		return new AStarPositionNode(
			this.getParent(),
			this.obstacles,
			this.width,
			this.height,
			goal,
			goal);
	}

	@Override
	public Set<Node<Position>> getAdjacentNodes() {
		Set<Node<Position>> nodes = new TreeSet<>();

		Position pos = this.getPosition();
		Position goal = this.getGoal();
		Position n = new Position(pos.x, pos.y - 1);
		Position s = new Position(pos.x, pos.y + 1);
		Position e = new Position(pos.x + 1, pos.y);
		Position w = new Position(pos.x - 1, pos.y);

		Node<Position> parent = this.getParent();
		if (!(parent != null && parent.getPosition().equals(pos))) {
			if (n.y >= 0 && !obstacles.contains(n)) {
				nodes.add(new AStarPositionNode(this, obstacles, width, height, n, goal));
			}
			if (s.y < height && !obstacles.contains(s)) {
				nodes.add(new AStarPositionNode(this, obstacles, width, height, s, goal));
			}
			if (e.x < width && !obstacles.contains(e)) {
				nodes.add(new AStarPositionNode(this, obstacles, width, height, e, goal));
			}
			if (w.x >= 0 && !obstacles.contains(w)) {
				nodes.add(new AStarPositionNode(this, obstacles, width, height, w, goal));
			}
		}

		return nodes;
	}

	@Override
	public int compareTo(Node<Position> other) {
		if (this.getCost() != other.getCost()) {
			return super.compareTo(other);

		} else {
			if (this.equals(other)) {
				return 0;

			} else {
				Position n1 = this.getPosition();
				Position n2 = other.getPosition();
				return (n1.x == n2.x) ? (n1.y - n2.y) : (n1.x - n2.x);
			}
		}
	}

	@Override
	public double g() {
		Node<Position> parent = getParent();
		if (parent == null) return 1.0; else return parent.g() + 1.0;
	}

	@Override
	public double h(Position goal) {
		Position here = this.getPosition();
		double xDiff = goal.x - here.x;
		double yDiff = goal.y - here.y;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	@Override
	public String toString() {
		Position here = this.getPosition();
		return "(" + here.x + ", " + here.y + ")";
	}
}

