/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.astar;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author denz
 */
public class BoardNode extends Node {

	//public static int board[][];
	public static SortedSet<Tile> tiles;
	private int x;
	private int y;
	public BoardNode(int x, int y, Node parent) {
		super(parent);
		this.x = x;
		this.y = y;
	}
	@Override
	public Set<Node> getAdjacentNodes() {
		Set<Node> nodes = new HashSet<>();
		int smallerX = x-1;
		int smallerY = y-1;
		int greaterX = x+1;
		int greaterY = y+1;

		Tile tile = new Tile(smallerX, y);
		BoardNode parent = (BoardNode) this.getParent();
		int parentX = -1;
		int parentY = -1;
		if (parent != null) {
			parentX = parent.x;
			parentY = parent.y;
		}
		if (!(parentX != -1 && parentX == tile.x && parentY == tile.y) && tiles.contains(tile)) {
			nodes.add(new BoardNode(smallerX, y, this));
		}
		tile = new Tile(x, smallerY);
		if (!(parentX != -1 && parentX == tile.x && parentY == tile.y) && tiles.contains(tile)) {
			nodes.add(new BoardNode(x, smallerY, this));
		}
		tile = new Tile(greaterX, y);
		if (!(parentX != -1 && parentX == tile.x && parentY == tile.y) && tiles.contains(tile)) {
			nodes.add(new BoardNode(greaterX, y, this));
		}
		tile = new Tile(x, greaterY);
		if (!(parentX != -1 && parentX == tile.x && parentY == tile.y) && tiles.contains(tile)) {
			nodes.add(new BoardNode(x, greaterY, this));
		}
		/*
		if (smallerX >= 0 && board[smallerX][y] >= 0) {
			BoardNode newNode = new BoardNode(smallerX, y, this);
			if (!newNode.equals(this.getParent())) {
				nodes.add(newNode);
			}
		}
		if (greaterX < board.length && board[greaterX][y] >= 0) {
			BoardNode newNode = new BoardNode(greaterX, y, this);
			if (!newNode.equals(this.getParent())) {
				nodes.add(newNode);
			}
		}
		if (smallerY >= 0 && board[x][smallerY] >= 0) {
			BoardNode newNode = new BoardNode(x, smallerY, this);
			if (!newNode.equals(this.getParent())) {
				nodes.add(newNode);
			}
		}
		if (greaterY < board.length && board[x][greaterY] >= 0) {
			BoardNode newNode = new BoardNode(x, greaterY, this);
			if (!newNode.equals(this.getParent())) {
				nodes.add(newNode);
			}
		}
		*/
		return nodes;
	}

	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}

	@Override
	public boolean equals(Node other) {
		if (other == null) {
			return false;
		}
		BoardNode o = (BoardNode) other;
		int ox = o.getX();
		int oy = o.getY();
		return this.x == ox && this.y == oy;
	}

	@Override
	public double g() {
		Node parent = getParent();
		if (parent == null) {
			return 1.0;
		}
		return parent.g() + 1.0;
	}

	@Override
	public double h() {
		BoardNode goal = (BoardNode) goalNode;
		double xDiff = goal.x - this.x;
		double yDiff = goal.y - this.y;
		return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
	}

	public String toString() {
		return ""+ x +","+ y;
	}
	
}
