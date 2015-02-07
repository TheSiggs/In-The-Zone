package nz.dcoder.inthezone.data_model.pure;

/**
 * The grid square at which something is located
 * */
public class Position {
	public final int x;
	public final int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Position add(Position p) {
		return new Position(x + p.x, y + p.y);
	}
}

