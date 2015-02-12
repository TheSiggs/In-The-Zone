package nz.dcoder.inthezone.data_model.pure;

import java.util.ArrayList;
import java.util.List;

public class LineOfSight {
	/**
	 * Get the squares that occur along the line of sight of a character.
	 *
	 * Returns a manhatten path, which means that diagonal moves are forbidden.
	 *
	 * @param p0 The position of the character
	 * @param p1 The square the character is targeting
	 * @param bias Sometimes there are two equally good approximations to a
	 * straight line.  Imagine yourself standing at the same position as the
	 * character, and facing the target square.  For cases where the bias
	 * parameter is relevant, The manhatten line-of-sight path may run either to
	 * the left of the euclidian line, or to the right of it.  If the bias
	 * parameter is true, then you get the right path, otherwise you get the left
	 * path.
	 *
	 * @return A manhatten path that approximates the line of sight of a
	 * character
	 * */
	public static List<Position> getLOS(
		Position p0,   // start position
		Position p1,   // target position
		boolean bias   // true for anticlockwise bias, false for clockwise bias
	) {
		final int dx = p1.x - p0.x;
		final int dy = p1.y - p0.y;

		final int m = Math.abs(dx) + Math.abs(dy) + 1;
		final double dl;

		if (dx == 0 || dy == 0) {
			dl = 0;
		} else if (Math.abs(dx) >= Math.abs(dy)) {
			dl = (double) m / (double) (Math.abs(dy) + 1);
		} else {
			dl = (double) m / (double) (Math.abs(dx) + 1);
		}

		// quadrants:
		//  1|0
		//  -+-
		//  2|3
		//
		//  octants
		//  3\2|1/0
		//  ---+---
		//  4/5|6\7

		final double l;
		final Position dp;
		final Position ds;

		if (dx > 0 && dy >= 0) { 
			// quadrant 0
			if (Math.abs(dx) > Math.abs(dy)) {
				// octant 0
				dp = new Position(1, 0);
				ds = new Position(0, 1);
				if (bias) l = Math.ceil(dl); else l = Math.floor(dl);
			} else if (Math.abs(dx) < Math.abs(dy)) {
				// octant 1
				dp = new Position(0, 1);
				ds = new Position(1, 0);
				if (bias) l = Math.floor(dl); else l = Math.ceil(dl);
			} else {
				// |dx| == |dy|
				l = 2;
				if (bias) {
					dp = new Position(1, 0);
					ds = new Position(0, 1);
				} else {
					dp = new Position(0, 1);
					ds = new Position(1, 0);
				}
			}

		} else if (dx <= 0 && dy > 0) {
			// quadrant 1
			if (Math.abs(dx) < Math.abs(dy)) {
				// octant 2
				dp = new Position(0, 1);
				ds = new Position(-1, 0);
				if (bias) l = Math.ceil(dl); else l = Math.floor(dl);
			} else if (Math.abs(dx) > Math.abs(dy)) {
				// octant 3
				dp = new Position(-1, 0);
				ds = new Position(0, 1);
				if (bias) l = Math.floor(dl); else l = Math.ceil(dl);
			} else {
				// |dx| == |dy|
				l = 2;
				if (bias) {
					dp = new Position(0, 1);
					ds = new Position(-1, 0);
				} else {
					dp = new Position(-1, 0);
					ds = new Position(0, 1);
				}
			}

		} else if (dx < 0 && dy <= 0) {
			// quadrant 2
			if (Math.abs(dx) > Math.abs(dy)) {
				// octant 4
				dp = new Position(-1, 0);
				ds = new Position(0, -1);
				if (bias) l = Math.ceil(dl); else l = Math.floor(dl);
			} else if (Math.abs(dx) < Math.abs(dy)) {
				// octant 5
				dp = new Position(0, -1);
				ds = new Position(-1, 0);
				if (bias) l = Math.floor(dl); else l = Math.ceil(dl);
			} else {
				// |dx| == |dy|
				l = 2;
				if (bias) {
					dp = new Position(-1, 0);
					ds = new Position(0, -1);
				} else {
					dp = new Position(0, -1);
					ds = new Position(-1, 0);
				}
			}

		} else if (dx >= 0 && dy < 0) {
			// quadrant 3
			if (Math.abs(dx) < Math.abs(dy)) {
				// octant 6
				dp = new Position(0, -1);
				ds = new Position(1, 0);
				if (bias) l = Math.ceil(dl); else l = Math.floor(dl);
			} else if (Math.abs(dx) > Math.abs(dy)) {
				// octant 7
				dp = new Position(1, 0);
				ds = new Position(0, -1);
				if (bias) l = Math.floor(dl); else l = Math.ceil(dl);
			} else {
				// |dx| == |dy|
				l = 2;
				if (bias) {
					dp = new Position(0, -1);
					ds = new Position(1, 0);
				} else {
					dp = new Position(1, 0);
					ds = new Position(0, -1);
				}
			}

		} else {
			// it must be that dx == 0 and dy == 0.
			// just make up some values in this case
			dp = new Position(0, 0);
			ds = new Position(0, 0);
			l = 0;
		}

		return LineOfSight.getLOS0(p0, p1, dp, ds, (int) l);
	}

	/**
	 * Get all the squares in a manhatten circle (which is of course a diamond)
	 *
	 * @param p0 The centre of the diamond
	 * @param size The radius of the diamond.  Radius 0 gives just the centre
	 * point.  Radius 1 would give the four squares top, left, bottom and right
	 * of the centre, plus the centre.
	 *
	 * @return A list of points that represent the perimeter and area of the
	 * diamond
	 * */
	public static List<Position> getDiamond(Position p0, int size) {
		List<Position> r = new ArrayList<Position>();
		for (int y = -size; y <= size; y++) {
			int l = size - Math.abs(y);
			for (int x = -l; x <= l; x++) {
				r.add(p0.add(new Position(x, y)));
			}
		}
		return r;
	}

	private static List<Position> getLOS0(
		Position p0,   // start position
		Position p1,   // target position
		Position dp,   // primary displacement
		Position ds,   // secondary displacement
		int l          // length of line segments
	) {
		final List<Position> r = new ArrayList<Position>();
		final boolean flat;
		if (dp.y == 0) flat = true; else flat = false;

		Position p = p0;

		r.add(p);
		while (!p.equals(p1)) {
			for (int i = l; i > 1; i--) {
				if (p.equals(p1)) return r;
				p = p.add(dp);
				r.add(p);
			}

			if ((flat && p.y == p1.y) || ((!flat) && p.x == p1.x)) {
				while (!p.equals(p1)) {
					p = p.add(dp);
					r.add(p);
				}
			} else {
				p = p.add(ds);
				r.add(p);
			}
		}

		return r;
	}
}

