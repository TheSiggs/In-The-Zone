package inthezone.test;

import inthezone.battle.Battle;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.GameDataFactory;
import isogame.engine.Stage;
import java.io.File;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

public class BattleTest {
	final GameDataFactory testData;
	final Stage checkers;
	final Collection<CharacterInfo> characters;

	public BattleTest() throws Exception {
		testData = new GameDataFactory(Optional.of(new File("src/main/resources/testgamedata")), false, true);
		checkers = testData.getStage("Checkerboard");
		characters = testData.getCharacters();
	}

	@Test
	public void testdata() {
		assertNotNull("Test game data null", testData);
		System.err.println(testData.getVersion());
		assertNotNull("Missing checkers stage", checkers);
		assertEquals("Expected four characters", 5, characters.size());
	}
}

