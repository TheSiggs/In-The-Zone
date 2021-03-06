package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.dcoder.ai.astar.BoardState;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents the map
 * */
public class Terrain {
	// TODO: implement this class.  It will need a constructor that tells us
	// where the mana zones are, and we might put elevation information in this
	// class also

	private final BoardState boardState;
	private final List<Position> obstacles = new ArrayList<Position>();

	public Terrain() {
		boardState = new BoardState().load("board.map");

		int width = boardState.getWidth();
		int height = boardState.getHeight();

		for (int x = -1; x < width+1; x++) {
			for (int y = -1; y < height+1; y++) {
				if (x==-1 || x==width || y==-1 || y==height || boardState.get(x, y) == 1) {
					obstacles.add(new Position(x, y));
				}
			}
		}
	}

	/**
	 * Get positions on the terrain where players cannot go.
	 * */
	public Collection<Position> getObstacles() {
		return obstacles;
	}

	/**
	 * These will have to be replaced with something more sophisticated in the
	 * actual game.
	 * */
	public int getBoardState(int x, int y) {
		return boardState.get(x, y);
	}

	public int getWidth() {
		return boardState.getWidth();
	}

	public int getHeight() {
		return boardState.getHeight();
	}

	/**
	 * Determine if a square is a mana zone
	 * */
	public boolean isManaZone(Position pos) {
		return boardState.get(pos.x, pos.y) == 2;
	}
}

