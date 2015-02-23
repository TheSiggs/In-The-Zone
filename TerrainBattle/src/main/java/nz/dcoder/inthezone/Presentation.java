/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import java.util.ArrayList;
import java.util.List;

import nz.dcoder.inthezone.data_model.BattleController;
import nz.dcoder.inthezone.data_model.Character;
import nz.dcoder.inthezone.data_model.factories.AbilityFactory;
import nz.dcoder.inthezone.data_model.factories.BattleObjectFactory;
import nz.dcoder.inthezone.data_model.factories.CharacterFactory;
import nz.dcoder.inthezone.data_model.factories.DatabaseException;
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;
import nz.dcoder.inthezone.input.GameActionListener;
import nz.dcoder.inthezone.input.Rotating;

/**
 *
 * @author denz
 */
public final class Presentation {

	final GameActionListener input;
	final GameState gameState;
	final Graphics graphics;
	final Main game;

	final AbilityFactory abilityFactory;
	final BattleObjectFactory battleObjectFactory;
	final CharacterFactory characterFactory;

	Presentation(Main game) throws DatabaseException {
		this.game = game;
		this.gameState = game.getGameState();
		this.graphics = new Graphics(game, gameState.terrain);
		this.input = new GameActionListener(game.getInputManager());

		abilityFactory = new AbilityFactory();
		battleObjectFactory = new BattleObjectFactory(abilityFactory);
		characterFactory = new CharacterFactory(
			abilityFactory, battleObjectFactory);

		initBattleController();
		startBattle();
	}

	void startBattle() {
		// create the characters for this battle (ignore the Party class for now)

		List<Character> pcs = new ArrayList<Character>();
		List<Character> npcs = new ArrayList<Character>();

		Position headingN = new Position(0, 1);
		Position headingS = new Position(0, -1);

		for (int x = 0; x < 5; ++x) {
			pcs.add(initGoblin(new Position(x * 2, 9), "belt/D.png", headingN));
		}

		for (int x = 0; x < 5; ++x) {
			npcs.add(initGoblin(new Position(x * 2, 0), "green/D.png", headingS));
		}

		gameState.makeBattle(pcs, npcs, game.getBattleController());
	}

	/**
	 * When we get more sophisticated, we will add another method to init actual
	 * players rather than just goblins.
	 * */
	Character initGoblin(Position p, String texture, Position dp) {
		CharacterGraphics cg = graphics.addGoblin(p, texture);
		cg.setHeading(dp);

		Character r = characterFactory.newCharacter(
			new CharacterName("goblin"), 1);

		if (r == null) {
			throw new RuntimeException("Could not create goblin character");
		}

		return r;
	}

	void simpleUpdate(float tpf) {
		Rotating viewRotating = input.getViewRotating();
		if (viewRotating == Rotating.LEFT) {
			graphics.rotateView(tpf * Graphics.rotationSpeed);
		} else if (viewRotating == Rotating.RIGHT) {
			graphics.rotateView(-tpf * Graphics.rotationSpeed);
		}

		// handle input
		// update HUD
		// update geometry tree
		// perform next animation step
	}

	void initBattleController() {
		BattleController controller = game.getBattleController();
		controller.onPlayerTurnStart = this::playerTurnStart;
	}

	private void playerTurnStart(Turn turn) {
		System.out.println("Player turn starts");
	}
}

