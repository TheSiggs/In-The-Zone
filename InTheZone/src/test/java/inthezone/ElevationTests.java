package inthezone.test;

import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElevationTests {
	final BattleTest bt;

	final Stage map;

	final MapPoint slope1 = new MapPoint(3, 6);
	final MapPoint slope2 = new MapPoint(4, 6);
	final MapPoint slope3 = new MapPoint(5, 6);

	final MapPoint up1 = new MapPoint(3, 5);
	final MapPoint up2 = new MapPoint(4, 5);
	final MapPoint up3 = new MapPoint(5, 5);

	public ElevationTests() throws Exception {
		bt = new BattleTest();
		map = bt.testData.getStage("Elevation_test");
	}

	public BattleState simpleBattle() {
		List<Character> cs = new ArrayList<>();
		cs.add(new Character(bt.testZan, Player.PLAYER_A, false, bt.zanPos, 0));
		cs.add(new Character(bt.testZan, Player.PLAYER_B, false, bt.zan2Pos, 4));
		return new BattleState(map, cs);
	}

	@Test public void alongSlopes() {
		BattleState battle = simpleBattle();
		final MapPoint start = slope1.add(new MapPoint(0, 1));

		final Character zan = battle.getCharacterAt(bt.zanPos).get();
		assertNotNull(zan);
		zan.teleport(slope1, false);

		final List<MapPoint> path =
			battle.findValidPath(slope1, slope3, Player.PLAYER_A, 2);
		assertEquals(3, path.size());

		final List<MapPoint> path2 =
			battle.findValidPath(slope1, slope1.add(new MapPoint(-2, 0)), Player.PLAYER_A, 2);
		assertTrue(path2.isEmpty());
	}

	@Test public void downCliffs() {
		BattleState battle = simpleBattle();
		final MapPoint destination = up1.add(new MapPoint(-1, 0));

		final Character zan = battle.getCharacterAt(bt.zanPos).get();
		assertNotNull(zan);
		zan.teleport(up1, false);

		final List<MapPoint> path =
			battle.findValidPath(up1, destination, Player.PLAYER_A, 1);
		System.err.println(path);
		assertEquals(2, path.size());

		zan.teleport(destination, false);
		final List<MapPoint> path2 =
			battle.findValidPath(destination, up1, Player.PLAYER_A, 1);
		System.err.println(path2);
		assertTrue(path2.isEmpty());
	}

	@Test public void slopeCost() {
	}
}

