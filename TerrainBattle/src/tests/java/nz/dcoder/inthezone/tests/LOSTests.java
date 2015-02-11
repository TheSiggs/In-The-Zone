package nz.dcoder.inthezone.tests;

import nz.dcoder.inthezone.data_model.*;
import nz.dcoder.inthezone.data_model.pure.*;

import java.util.List;
import org.junit.Test;
import static	org.junit.Assert.*;

public class LOSTests {
	@Test public void testLOSNear() {
		Position p0 = new Position(1, 1);
		Position n = p0.add(new Position(0, 1));
		Position s = p0.add(new Position(0, -1));
		Position e = p0.add(new Position(1, 0));
		Position w = p0.add(new Position(-1, 0));
		Position ne = p0.add(new Position(1, 1));
		Position nw = p0.add(new Position(-1, 1));
		Position se = p0.add(new Position(1, -1));
		Position sw = p0.add(new Position(-1, -1));

		List<Position> path;

		path = LineOfSight.getLOS(p0, p0, true);
		assertTrue("0 length path", hasEndpoints(path, p0, p0));

		path = LineOfSight.getLOS(p0, n, true);
		assertTrue("length 1 path n", hasEndpoints(path, p0, n));
		assertEquals("length 1 path n size", 2, path.size());

		path = LineOfSight.getLOS(p0, s, true);
		assertTrue("length 1 path s", hasEndpoints(path, p0, s));
		assertEquals("length 1 path s size", 2, path.size());

		path = LineOfSight.getLOS(p0, e, true);
		assertTrue("length 1 path e", hasEndpoints(path, p0, e));
		assertEquals("length 1 path e size", 2, path.size());

		path = LineOfSight.getLOS(p0, w, true);
		assertTrue("length 1 path w", hasEndpoints(path, p0, w));
		assertEquals("length 1 path w size", 2, path.size());

		// anticlockwise bias
		path = LineOfSight.getLOS(p0, ne, true);
		assertTrue("anticlockwise bias ne", hasPoints(path, p0, e, ne));
		assertEquals("anticlockwise bias ne size", 3, path.size());

		path = LineOfSight.getLOS(p0, nw, true);
		assertTrue("anticlockwise bias nw", hasPoints(path, p0, n, nw));
		assertEquals("anticlockwise bias nw size", 3, path.size());

		path = LineOfSight.getLOS(p0, se, true);
		assertTrue("anticlockwise bias se", hasPoints(path, p0, s, se));
		assertEquals("anticlockwise bias se size", 3, path.size());

		path = LineOfSight.getLOS(p0, sw, true);
		assertTrue("anticlockwise bias sw", hasPoints(path, p0, w, sw));
		assertEquals("anticlockwise bias sw size", 3, path.size());

		// clockwise bias
		path = LineOfSight.getLOS(p0, ne, false);
		assertTrue("clockwise bias ne", hasPoints(path, p0, n, ne));
		assertEquals("clockwise bias ne size", 3, path.size());

		path = LineOfSight.getLOS(p0, nw, false);
		assertTrue("clockwise bias nw", hasPoints(path, p0, w, nw));
		assertEquals("clockwise bias nw size", 3, path.size());

		path = LineOfSight.getLOS(p0, se, false);
		assertTrue("clockwise bias se", hasPoints(path, p0, e, se));
		assertEquals("clockwise bias se size", 3, path.size());

		path = LineOfSight.getLOS(p0, sw, false);
		assertTrue("clockwise bias sw", hasPoints(path, p0, s, sw));
		assertEquals("clockwise bias sw size", 3, path.size());
	}

	private boolean hasEndpoints(List<Position> path, Position p0, Position p1) {
		return
			path.stream().anyMatch(x -> x.equals(p0)) &&
			path.stream().anyMatch(x -> x.equals(p1));
	}

	private boolean hasPoints(List<Position> path, Position p0, Position i, Position p1) {
		return
			path.stream().anyMatch(x -> x.equals(p0)) &&
			path.stream().anyMatch(x -> x.equals(i)) &&
			path.stream().anyMatch(x -> x.equals(p1));
	}

	@Test public void testLOSFar() {
		Position p0 = new Position(0, 0);
		Position nfar = new Position(0, 3);
		Position sfar = new Position(0, -3);
		Position efar = new Position(3, 0);
		Position wfar = new Position(-3, 0);

		List<Position> path;

		path = LineOfSight.getLOS(p0, nfar, true);
		assertEquals("path length 4 nfar", 4, path.size());
		assertEquals("path length 4 nfar start", p0, path.get(0));
		assertEquals("path length 4 nfar end", nfar, path.get(3));

		path = LineOfSight.getLOS(p0, sfar, true);
		assertEquals("path length 4 sfar", 4, path.size());
		assertEquals("path length 4 sfar start", p0, path.get(0));
		assertEquals("path length 4 sfar end", sfar, path.get(3));

		path = LineOfSight.getLOS(p0, efar, true);
		assertEquals("path length 4 efar", 4, path.size());
		assertEquals("path length 4 efar start", p0, path.get(0));
		assertEquals("path length 4 efar end", efar, path.get(3));

		path = LineOfSight.getLOS(p0, wfar, true);
		assertEquals("path length 4 wfar", 4, path.size());
		assertEquals("path length 4 wfar start", p0, path.get(0));
		assertEquals("path length 4 wfar end", wfar, path.get(3));
	}

	Position[] o0a = {new Position(0, 0), new Position( 1,  0), new Position( 2,  0), new Position( 2,  1), new Position( 3,  1)};
	Position[] o1a = {new Position(0, 0), new Position( 0,  1), new Position( 1,  1), new Position( 1,  2), new Position( 1,  3)};
	Position[] o2a = {new Position(0, 0), new Position( 0,  1), new Position( 0,  2), new Position(-1,  2), new Position(-1,  3)};
	Position[] o3a = {new Position(0, 0), new Position(-1,  0), new Position(-1,  1), new Position(-2,  1), new Position(-3,  1)};
	Position[] o4a = {new Position(0, 0), new Position(-1,  0), new Position(-2,  0), new Position(-2, -1), new Position(-3, -1)};
	Position[] o5a = {new Position(0, 0), new Position( 0, -1), new Position(-1, -1), new Position(-1, -2), new Position(-1, -3)};
	Position[] o6a = {new Position(0, 0), new Position( 0, -1), new Position( 0, -2), new Position( 1, -2), new Position( 1, -3)};
	Position[] o7a = {new Position(0, 0), new Position( 1,  0), new Position( 1, -1), new Position( 2, -1), new Position( 3, -1)};

	Position[] o0c = {new Position(0, 0), new Position( 1,  0), new Position( 1,  1), new Position( 2,  1), new Position( 3,  1)};
	Position[] o1c = {new Position(0, 0), new Position( 0,  1), new Position( 0,  2), new Position( 1,  2), new Position( 1,  3)};
	Position[] o2c = {new Position(0, 0), new Position( 0,  1), new Position(-1,  1), new Position(-1,  2), new Position(-1,  3)};
	Position[] o3c = {new Position(0, 0), new Position(-1,  0), new Position(-2,  0), new Position(-2,  1), new Position(-3,  1)};
	Position[] o4c = {new Position(0, 0), new Position(-1,  0), new Position(-1, -1), new Position(-2, -1), new Position(-3, -1)};
	Position[] o5c = {new Position(0, 0), new Position( 0, -1), new Position( 0, -2), new Position(-1, -2), new Position(-1, -3)};
	Position[] o6c = {new Position(0, 0), new Position( 0, -1), new Position( 1, -1), new Position( 1, -2), new Position( 1, -3)};
	Position[] o7c = {new Position(0, 0), new Position( 1,  0), new Position( 2,  0), new Position( 2, -1), new Position( 3, -1)};

	@Test public void testLOSDirection() {
		assertTrue("testLOSDirection octant 0a", testLOSArray(o0a, true));
		assertTrue("testLOSDirection octant 1a", testLOSArray(o1a, true));
		assertTrue("testLOSDirection octant 2a", testLOSArray(o2a, true));
		assertTrue("testLOSDirection octant 3a", testLOSArray(o3a, true));
		assertTrue("testLOSDirection octant 4a", testLOSArray(o4a, true));
		assertTrue("testLOSDirection octant 5a", testLOSArray(o5a, true));
		assertTrue("testLOSDirection octant 6a", testLOSArray(o6a, true));
		assertTrue("testLOSDirection octant 7a", testLOSArray(o7a, true));

		assertTrue("testLOSDirection octant 0c", testLOSArray(o0c, false));
		assertTrue("testLOSDirection octant 1c", testLOSArray(o1c, false));
		assertTrue("testLOSDirection octant 2c", testLOSArray(o2c, false));
		assertTrue("testLOSDirection octant 3c", testLOSArray(o3c, false));
		assertTrue("testLOSDirection octant 4c", testLOSArray(o4c, false));
		assertTrue("testLOSDirection octant 5c", testLOSArray(o5c, false));
		assertTrue("testLOSDirection octant 6c", testLOSArray(o6c, false));
		assertTrue("testLOSDirection octant 7c", testLOSArray(o7c, false));
	}

	private boolean testLOSArray(Position[] pos, boolean bias) {
		Position[] check = LineOfSight.getLOS(pos[0], pos[4], bias).toArray(pos);
		for (int i = 0; i < 5; i++) { 
			if (!pos[i].equals(check[i])) return false;
		}
		return true;
	}

	@Test public void testDiamond() {
		Position p0 = new Position(1, 1);
		Position n = p0.add(new Position(0, 1));
		Position s = p0.add(new Position(0, -1));
		Position e = p0.add(new Position(1, 0));
		Position w = p0.add(new Position(-1, 0));

		List<Position> diamond;

		diamond = LineOfSight.getDiamond(p0, 0);
		assertEquals("diamond size 0 perimeter size", 1, diamond.size());
		assertEquals("diamond size 0 perimeter", p0, diamond.get(0));

		diamond = LineOfSight.getDiamond(p0, 1);
		assertEquals("diamond size 1 perimeter size", 4, diamond.size());
		assertTrue(
			"diamond size 1 perimeter",
			diamond.stream().anyMatch(x -> x.equals(n)) &&
			diamond.stream().anyMatch(x -> x.equals(s)) &&
			diamond.stream().anyMatch(x -> x.equals(e)) &&
			diamond.stream().anyMatch(x -> x.equals(w)));
	}
}

