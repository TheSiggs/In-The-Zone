package inthezone.client.test;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.RoadBlock;
import inthezone.battle.Trap;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.StatusEffectFactory;
import inthezone.comptroller.InfoPush;
import inthezone.comptroller.InfoTargeting;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
			new File("build/resources/main/testgamedata2")).getAbsoluteFile()), false, true);
		checkers = testData.getStage("Checkerboard");
		characters = testData.getCharacters();

		testZan = theWorks(testData.getCharacter("Zan"));
		testDan = theWorks(testData.getCharacter("Daniel"));
		testKieren = theWorks(testData.getCharacter("Kieren"));
		testNettis = theWorks(testData.getCharacter("Nettis"));
	}

	private CharacterProfile theWorks(final CharacterInfo i) throws Exception {
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

	@Test
	public void cannotPushPullImprisoned() throws Exception {
		final Battle b = simpleBattle();
		final Character zan = b.battleState.getCharacterAt(zanPos).get();
		final Character dan = b.battleState.getCharacterAt(danPos).get();
		final Character kieren = b.battleState.getCharacterAt(kierenPos).get();

		assertNotNull(zan);
		assertNotNull(dan);
		assertNotNull(kieren);

		// imprison dan
		dan.applyStatus(b,
			StatusEffectFactory.getEffect(new StatusEffectInfo("imprisoned"),
			0, 0, Optional.empty()));

		kieren.teleport(dan.getPos().add(new MapPoint(2, 0)), false);

		// check that we cannot push dan
		final InfoPush pushTargeting = new InfoPush(zan.freeze());
		pushTargeting.completeAction(b);
		final Collection<MapPoint> pts2 = pushTargeting.complete.get();
		assertFalse(pts2.contains(dan.getPos()));

		// check that we cannot pull dan, but we can pull zan
		final InfoTargeting targeting = new InfoTargeting(
			kieren.freeze(),
			kieren.getPos(),
			new HashSet<>(),
			kieren.abilities.stream().filter(a ->
				a.info.name.equals("Warrior's challenge")).findFirst().get());
		targeting.completeAction(b);
		final Collection<MapPoint> pts = targeting.complete.get();
		assertFalse(pts.contains(dan.getPos()));
		assertTrue(pts.contains(zan.getPos()));
	}
}

