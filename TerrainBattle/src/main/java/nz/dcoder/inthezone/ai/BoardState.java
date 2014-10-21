/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.ai;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2i;

/**
 *
 * @author informatics-palmerson
 */
public class BoardState {
	//static enum Dir { NORTH, EAST, SOUTH, WEST};
	static int VISITED = -1;

	int board[][];
	Point2i position;
	Point2i start;
	Point2i end;
	List<Point2i> travelledPoints;
	public BoardState(int width, int height, int startX, int startY, int endX, int endY) {
		board = new int[width][height];
		for (int i = 0; i< width; ++i) {
			for (int j = 0; j < height; ++j) {
				board[i][j] = 0;
			}
		}
		travelledPoints = new ArrayList<>();
		start = new Point2i(startX, startY);
		end = new Point2i(endX, endY);
		position = new Point2i(startX, startY);
	}
	public void addObstacle(int x, int y, int value) {
		board[x][y] = value;
	}
	/**
	 * Heuristic distance in squares
	 * @return 
	 */
	public double distanceToEndHeuristic() {
		return Math.sqrt((end.x*end.x) + (end.y*end.y));
		//return Math.abs(end.x - position.x) + Math.abs(end.y - position.y);
	}

	public Point2i move(MoveAction.Dir direction) {
		this.board[position.x][position.y] = VISITED;
		switch (direction) {
			default:
			case NORTH:
				position.y++;
				break;
			case EAST:
				position.x++;
				break;
			case SOUTH:
				position.y--;
				break;
			case WEST:
				position.x--;
				break;
		}
		this.board[position.x][position.y] = VISITED;
		return position;
	}

	@Override
	public boolean equals(Object other) {
		return this.position.equals(((BoardState) other).position);
	}
}
