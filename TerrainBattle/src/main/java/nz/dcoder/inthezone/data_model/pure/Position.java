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

	@Override public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public Position add(Position p) {
		return new Position(x + p.x, y + p.y);
	}

	public Position sub(Position p) {
		return new Position(x - p.x, y - p.y);
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position p = (Position) obj;
			return p.x == x && p.y == y;
		} else {
			return false;
		}
	}

	@Override public int hashCode() {
		int m = 31;
		int r = 1;
		r = m * r + x;
		r = m * r + y;
		return r;
	}
}

