package inthezone.test;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.RoadBlock;
import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.StatusEffectFactory;
import inthezone.battle.Trap;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

public class BattleTest {
	final GameDataFactory testData;
	final Stage checkers;
	final Collection<CharacterInfo> characters;

	final CharacterProfile testZan;
	final CharacterProfile testDan;
	final CharacterProfile testKieren;
	final CharacterProfile testNettis;

	final MapPoint zanPos = new MapPoint(0, 3);
	final MapPoint danPos = new MapPoint(0, 4);
	final MapPoint kierenPos = new MapPoint(0, 5);
	final MapPoint nettisPos = new MapPoint(0, 6);

	final MapPoint zan2Pos = new MapPoint(7, 3);
	final MapPoint dan2Pos = new MapPoint(7, 4);
	final MapPoint kieren2Pos = new MapPoint(7, 5);
	final MapPoint nettis2Pos = new MapPoint(7, 6);

	public BattleTest() throws Exception {
		testData = new GameDataFactory(Optional.of((
			new File("build/resources/main/testgamedata")).getAbsoluteFile()), false, true);
		checkers = testData.getStage("Checkerboard");
		characters = testData.getCharacters();

		testZan = theWorks(testData.getCharacter("Zan"));
		testDan = theWorks(testData.getCharacter("Daniel"));
		testKieren = theWorks(testData.getCharacter("Kieren"));
		testNettis = theWorks(testData.getCharacter("Nettis"));
	}

	private CharacterProfile theWorks(CharacterInfo i) throws Exception {
		return new CharacterProfile(i, i.abilities,
			i.abilities.stream().filter(a -> a.type == AbilityType.BASIC).findFirst().get(), 0, 0);
	}

	public Battle simpleBattle() throws Exception {
		final Collection<Character> cs = new ArrayList<>();
		cs.add(new Character(testZan, Player.PLAYER_A, false, zanPos, 0));
		cs.add(new Character(testDan, Player.PLAYER_A, false, danPos, 1));
		cs.add(new Character(testKieren, Player.PLAYER_A, false, kierenPos, 2));
		cs.add(new Character(testNettis, Player.PLAYER_A, false, nettisPos, 3));

		cs.add(new Character(testZan, Player.PLAYER_B, false, zan2Pos, 4));
		cs.add(new Character(testDan, Player.PLAYER_B, false, dan2Pos, 5));
		cs.add(new Character(testKieren, Player.PLAYER_B, false, kieren2Pos, 5));
		cs.add(new Character(testNettis, Player.PLAYER_B, false, nettis2Pos, 6));

		final BattleState state = new BattleState(checkers, cs);
		return new Battle(state, testData.getStandardSprites());
	}

	private static <A> Collection<A> single(A a) {
		final Collection<A> r = new ArrayList<>();
		r.add(a);
		return r;
	}

	@Test
	public void testdata() throws Exception {
		assertNotNull("Test game data null", testData);
		System.err.println(testData.getVersion());
		assertNotNull("Missing checkers stage", checkers);
		assertEquals("Expected four characters", 5, characters.size());
		Battle b = simpleBattle();
		assertNotNull("Failed to construct battle", b);
	}

	@Test
	public void validTrapPlacement() throws Exception {
		final Battle b = simpleBattle();
		final Character dan = b.battleState.getCharacterAt(danPos).get();
		assertNotNull(dan);
		assertEquals("Daniel", dan.name);

		final Ability trap1 =
			dan.abilities.stream().filter(a -> a.info.name.equals("Trap1")).findFirst().get();
		assertNotNull(trap1);

		final List<Trap> r1 = b.createTrap(trap1, dan, single(new MapPoint(2, 4)));
		assertEquals(1, r1.size());

		final List<Trap> r2 = b.createTrap(trap1, dan, single(new MapPoint(2, 4)));
		assertTrue(r2.isEmpty());

		final List<Trap> r3 = b.createTrap(trap1, dan, single(zanPos));
		assertTrue(r3.isEmpty());

		final RoadBlock obstacle = b.battleState.placeObstacle(
			new MapPoint(3, 4), Optional.empty(), testData.getStandardSprites());
		final List<Trap> r4 = b.createTrap(trap1, dan, single(new MapPoint(3, 4)));
		assertTrue(r4.isEmpty());

		final Trap t = r1.get(0);
		assertEquals(new MapPoint(2, 4), t.getPos());
	}

	@Test
	public void acceleratedSlowed() throws Exception {
		final Battle b = simpleBattle();
		final Character dan = b.battleState.getCharacterAt(danPos).get();
		assertNotNull(dan);

		assertEquals(6, dan.getMP());
		assertEquals(6, dan.getStats().mp);

		final StatusEffect accelerated = StatusEffectFactory.getEffect(
			new StatusEffectInfo("accelerated"), b.battleState.getTurnNumber(), 0, Optional.empty());
		assertNotNull(accelerated);

		dan.applyStatus(b, accelerated);
		assertEquals(7, dan.getMP());
		assertEquals(7, dan.getStats().mp);

		dan.cleanse();
		assertEquals(6, dan.getMP());
		assertEquals(6, dan.getStats().mp);

		dan.applyStatus(b, accelerated);
		assertEquals(6, dan.getMP());
		assertEquals(7, dan.getStats().mp);

		// restart the turn and observe that accelerated has been correctly applied
		assertTrue(b.doTurnStart(Player.PLAYER_A).isEmpty());
		assertTrue(b.getTurnStart(Player.PLAYER_A).isEmpty());
		assertEquals(7, dan.getMP());
		assertEquals(7, dan.getStats().mp);

		// reapply the status effect when the character has 0 mp
		final MapPoint p2 = danPos.add(new MapPoint(5, 2));
		dan.moveTo(p2, 7, false);
		assertEquals(p2, dan.getPos());
		assertEquals(0, dan.getMP());
		dan.applyStatus(b, accelerated);
		assertEquals(0, dan.getMP());
		assertEquals(7, dan.getStats().mp);
	}
}

