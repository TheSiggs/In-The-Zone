/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.astar;

import java.util.Set;

/**
 *
 * @author denz
 */
public abstract class Node implements Comparable<Node>{
	public static Node goalNode;
	protected double cost;
	private Node parent;
	public abstract Set<Node> getAdjacentNodes();
	public abstract boolean equals(Node other);

	public Node(Node parent) {
		this.parent = parent;
	}

	public void calculateCost() {
		cost = g() + h();
	}

	public abstract double g();

	public abstract double h();

	@Override
	public int compareTo(Node other) {
		double oCost = other.getCost();
		return cost == oCost ? 0 : cost < oCost ? -1 : 1;
	}

	public double getCost() {
		return this.cost;
	}

	/**
	 * @return the parent
	 */
	public Node getParent() {
		return parent;
	}
}
