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

		final double dsdp;

		if (dx == 0 || dy == 0) {
			dsdp = 0;
		} else if (Math.abs(dx) >= Math.abs(dy)) {
			dsdp = Math.abs((double) dy / (double) dx);
		} else {
			dsdp = Math.abs((double) dx / (double) dy);
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

		final Position dp;
		final Position ds;

		if (dx > 0 && dy >= 0) { 
			// quadrant 0
			if (Math.abs(dx) >= Math.abs(dy)) {
				// octant 0
				dp = new Position(1, 0);
				ds = new Position(0, 1);
			} else {
				// octant 1
				dp = new Position(0, 1);
				ds = new Position(1, 0);
			}

		} else if (dx <= 0 && dy > 0) {
			// quadrant 1
			if (Math.abs(dx) <= Math.abs(dy)) {
				// octant 2
				dp = new Position(0, 1);
				ds = new Position(-1, 0);
			} else {
				// octant 3
				dp = new Position(-1, 0);
				ds = new Position(0, 1);
			}

		} else if (dx < 0 && dy <= 0) {
			// quadrant 2
			if (Math.abs(dx) > Math.abs(dy)) {
				// octant 4
				dp = new Position(-1, 0);
				ds = new Position(0, -1);
			} else {
				// octant 5
				dp = new Position(0, -1);
				ds = new Position(-1, 0);
			} 

		} else if (dx >= 0 && dy < 0) {
			// quadrant 3
			if (Math.abs(dx) < Math.abs(dy)) {
				// octant 6
				dp = new Position(0, -1);
				ds = new Position(1, 0);
			} else {
				// octant 7
				dp = new Position(1, 0);
				ds = new Position(0, -1);
			}

		} else {
			// it must be that dx == 0 and dy == 0.
			// just make up some values in this case
			dp = new Position(0, 0);
			ds = new Position(0, 0);
		}

		if (dy >= 0) {
			return LineOfSight.getLOS0(p0, p1, dp, ds, dsdp, bias);
		} else {
			return LineOfSight.getLOS0(p0, p1, dp, ds, dsdp, !bias);
		}
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
		double dsdp,   // gradient of the line
		boolean bias   // true to prefer the primary direction
	) {
		final List<Position> r = new ArrayList<Position>();

		double e = 0;
		Position p = p0;
		r.add(p);

		// This is essentially Bresenham's line algorithm modified to give a
		// manhatten path
		while (!p.equals(p1)) {
			e += dsdp;
			if (e < 1) {
				// primary direction only
				p = p.add(dp);
				r.add(p);
			} else {
				// primary and secondary directions
				if (bias) {
					p = p.add(dp);
					r.add(p);
					p = p.add(ds);
					r.add(p);
				} else {
					p = p.add(ds);
					r.add(p);
					p = p.add(dp);
					r.add(p);
				}
				e -= 1;
			}
		}

		return r;
	}
}

