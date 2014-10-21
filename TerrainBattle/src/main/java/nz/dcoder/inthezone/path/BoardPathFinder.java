/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.path;

import com.googlecode.npuzzle.logic.astar.AStarTree;
import com.googlecode.npuzzle.logic.astar.Node;
import com.googlecode.npuzzle.logic.astar.Path;
import javax.vecmath.Point2i;

/**
 *
 * @author heuert
 */
public class BoardPathFinder {

	public static void main(String args[]) {
		int board[][] = new int[5][5];
		Node start = new BoardNode(new Point2i(0, 0));
		Node end = new BoardNode(new Point2i(4, 4));
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board.length; y++) {
				board[x][y] = 0;
			}
		}
		BoardNode.board = board;
		AStarTree astar = new AStarTree(1000);
		Path path = astar.findPath(start, end);
		int len = path.getLength();
		for (int i = 0; i < len; ++i) {
			BoardNode node = (BoardNode) path.getNode(i);
			Point2i position = node.getPosition();
			System.out.println(position);
		}
	}
}
