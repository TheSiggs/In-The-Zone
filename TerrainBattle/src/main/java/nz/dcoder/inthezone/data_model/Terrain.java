package nz.dcoder.inthezone.data_model;

import java.util.SortedSet;
import java.util.TreeSet;
import nz.dcoder.ai.astar.BoardState;
import nz.dcoder.ai.astar.Tile;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents the map
 * */
public class Terrain {
	// TODO: implement this class.  It will need a constructor that tells us
	// where the mana zones are, and we might put elevation information in this
	// class also

	// public for now.  This will probably change when A* gets refactored
	public final SortedSet<Tile> defaultBoardTiles = new TreeSet<>();

	private final BoardState boardState;

	public Terrain() {
		boardState = new BoardState().load("board.map");

		int width = boardState.getWidth();
		int height = boardState.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (boardState.get(x, y) == 0) {
					defaultBoardTiles.add(new Tile(x, y));
				}
			}
		}
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

	public boolean isManaZone(Position pos) {
		// TODO: implement this method
		return false;
	}
}

