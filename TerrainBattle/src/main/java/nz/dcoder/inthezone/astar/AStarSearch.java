/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author denz
 */
public class AStarSearch {
	private Node startNode;
	private Node goalNode;
	public AStarSearch(Node startNode, Node goalNode) {
		this.startNode = startNode;
		this.goalNode = goalNode;
		Node.goalNode = goalNode;
	}
	public List<Node> search() {
		List<Node> nodeList = new ArrayList<>();
		SortedSet<Node> open = new TreeSet<>();
		Set<Node> closed = new HashSet<>();
		Node current;// = startNode;
		open.add(startNode);
		//closed.add(startNode);
		Node lastNode = null;
		while (!open.isEmpty()) {
			current = open.first();
			if (current.equals(goalNode)) {
				// path complete
				lastNode = current;
				break;
			} else {
				closed.add(current);
				open.remove(current);
				for (Node node : current.getAdjacentNodes()) {
					if (!open.contains(node) && !closed.contains(node)) {
						// move it to the open list and calculate cost
						node.calculateCost();
						//closed.remove(node);
						open.add(node);
					}
				}
			}
		}
		Node parent;
		do {
			parent = lastNode.getParent();
			nodeList.add(lastNode);
			lastNode = parent;
		} while (parent != null);
		Collections.reverse(nodeList);
		return nodeList;
	}
}
