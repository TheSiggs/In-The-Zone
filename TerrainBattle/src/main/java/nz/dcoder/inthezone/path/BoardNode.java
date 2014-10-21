/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.path;

import com.googlecode.npuzzle.logic.astar.Node;
import java.util.Set;
import java.util.TreeSet;
import javax.vecmath.Point2i;
/**
 *
 * @author heuert
 */
public class BoardNode extends Node{

	public static int board[][];
	private Point2i position;
	public BoardNode(Point2i position) {
		this.position = position;
	}
	@Override
	public Set<Node> getNeighbours() {
		int x = getPosition().x;
		int y = getPosition().y;
		int newX = x+1;
		int newY = y+1;
		Set<Node> nodes = new TreeSet<>();
		if (newX < board.length && board[newX][y] == 0) {
			nodes.add(new BoardNode(new Point2i(newX, y)));
		}
		if (newY < board[0].length && board[x][newY] == 0) {
			nodes.add(new BoardNode(new Point2i(x, newY)));
		}
		newX = x-1;
		newY = y-1;
		if (newX >= 0 && board[newX][y] == 0) {
			nodes.add(new BoardNode(new Point2i(newX, y)));
		}
		if (newY >= 0 && board[x][newY] == 0) {
			nodes.add(new BoardNode(new Point2i(x, newY)));
		}
		return nodes;
		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public float costDifference(Node node) {
		//BoardNode a = (BoardNode) this;
		BoardNode b = (BoardNode) node;
		double diffX = this.getPosition().x - b.getPosition().x;
		double diffY = this.getPosition().y - b.getPosition().y;
		return (float) Math.sqrt(diffX*diffX + diffY*diffY);
	}

	@Override
	public int compareTo(Node o) {
		float diff = this.costDifference((BoardNode) o);
		return diff == 0f ? 0 : (diff < 0f ? -1 : 1);
	}

	/**
	 * @return the position
	 */
	public Point2i getPosition() {
		return position;
	}
	
}
