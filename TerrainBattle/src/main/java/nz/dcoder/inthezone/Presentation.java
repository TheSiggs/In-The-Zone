package nz.dcoder.inthezone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import nz.dcoder.inthezone.ai.AIPlayer;
import nz.dcoder.inthezone.data_model.BattleController;
import nz.dcoder.inthezone.data_model.Character;
import nz.dcoder.inthezone.data_model.DoAbilityInfo;
import nz.dcoder.inthezone.data_model.DoBattleEnd;
import nz.dcoder.inthezone.data_model.DoCharacterDeath;
import nz.dcoder.inthezone.data_model.DoMoveInfo;
import nz.dcoder.inthezone.data_model.DoObjectDestruction;
import nz.dcoder.inthezone.data_model.Equipment;
import nz.dcoder.inthezone.data_model.factories.AbilityFactory;
import nz.dcoder.inthezone.data_model.factories.BattleObjectFactory;
import nz.dcoder.inthezone.data_model.factories.CharacterFactory;
import nz.dcoder.inthezone.data_model.factories.DatabaseException;
import nz.dcoder.inthezone.data_model.factories.EquipmentFactory;
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.EquipmentName;
import nz.dcoder.inthezone.data_model.pure.Points;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;
import nz.dcoder.inthezone.input.GameActionListener;
import nz.dcoder.inthezone.input.Rotating;

/**
 *
 * @author denz
 */
public final class Presentation {
	private final GameState gameState;
	private final GameActionListener input;
	private final Graphics graphics;
	private final UserInterface ui;

	private final AbilityFactory abilityFactory;
	private final BattleObjectFactory battleObjectFactory;
	private final CharacterFactory characterFactory;
	private final EquipmentFactory equipmentFactory;

	private Turn turn;

	Presentation(
		GameState gameState, Graphics graphics, UserInterface ui
	) throws DatabaseException {
		this.gameState = gameState;
		this.graphics = graphics;
		this.ui = ui;
		this.input = ui.getGameActionListener();

		abilityFactory = new AbilityFactory();
		battleObjectFactory = new BattleObjectFactory(abilityFactory);
		characterFactory = new CharacterFactory(
			abilityFactory, battleObjectFactory);
		equipmentFactory = new EquipmentFactory(abilityFactory);

		initBattleController();
		startBattle();
	}

	void startBattle() {
		// create the characters for this battle (ignore the Party class for now)

		List<Character> pcs = new ArrayList<Character>();
		List<Character> npcs = new ArrayList<Character>();

		Position headingN = new Position(0, -1);
		Position headingS = new Position(0, 1);

		for (int x = 0; x < 5; ++x) {
			pcs.add(initGoblin(new Position(x * 2, 9), x + 1, headingN));
		}

		for (int x = 0; x < 5; ++x) {
			npcs.add(initGoblin(new Position(x * 2, 0), x + 6, headingS));
		}

		gameState.makeBattle(pcs, npcs, controller, new AIPlayer());
	}

	/**
	 * When we get more sophisticated, we will add another method to init actual
	 * players rather than just goblins.
	 * */
	Character initGoblin(Position p, int i, Position dp) {
		CharacterGraphics cg = graphics.addGoblin(p, i);
		cg.setHeading(dp);

		CharacterName name = new CharacterName("goblin " + i);
		Character r = characterFactory.newCharacter(name, 1);
		Equipment weapon = equipmentFactory.newEquipment(
			new EquipmentName("simple weapon"));
		r.equipWeapon(weapon);

		if (r == null) {
			throw new RuntimeException(
				"Could not create character " + name.toString());
		}

		r.position = p;

		return r;
	}

	/**
	 * Main update loop for the game.  Try to avoid putting things in here, use
	 * controllers instead.
	 * */
	void simpleUpdate(float tpf) {
		Rotating viewRotating = input.getViewRotating();
		if (viewRotating == Rotating.LEFT) {
			graphics.rotateView(tpf * Graphics.rotationSpeed);
		} else if (viewRotating == Rotating.RIGHT) {
			graphics.rotateView(-tpf * Graphics.rotationSpeed);
		}
	}

	private final BattleController controller = new BattleController();

	private void initBattleController() {
		controller.onPlayerTurnStart = this::playerTurnStart;
		controller.onBattleEnd = this::endBattle;
		controller.onMove = this::move;
		controller.onAbility = this::ability;
		controller.onDeath = this::death;
		controller.onDestruction = this::destruction;
	}

	/**
	 * Handle the start of the player's turn.
	 * */
	private void playerTurnStart(Turn turn) {
		input.setTurn(turn);
		this.turn = turn;

		System.out.println("Player turn starts");

		ui.turnStart(true,
			turn.getPlayerInfo().stream().collect(Collectors.toList()),
			turn.getNPCInfo().stream().collect(Collectors.toList()));
	}

	private void aiPlayerTurnStart(Turn turn) {
		// normally we would invoke the AI with the new turn object.  Instead,
		// we'll make let the human player take this turn for now.
		
		System.out.println("AI turn starts");
		input.setTurn(turn);
		this.turn = turn;

		ui.turnStart(true,
			turn.getPlayerInfo().stream().collect(Collectors.toList()),
			turn.getNPCInfo().stream().collect(Collectors.toList()));
	}

	/**
	 * Handle end conditions.
	 * */
	private void endBattle(DoBattleEnd end) {
		if (end.playerWins) {
			System.out.println("You win the battle!");
		} else {
			System.out.println("You lose, loser!");
		}
	}

	/**
	 * Handle a move event.
	 * */
	private void move(DoMoveInfo move) {
		if (move.path.size() >= 2) {
			CharacterGraphics cg = graphics.getCharacterByPosition(move.start);
			graphics.doWalk(cg, move.path, null);

		} else {
			System.out.println("Short path (length less than 2).  This shouldn't happen");
		}
	}

	/**
	 * Handle ability effects
	 * */
	private void ability(DoAbilityInfo action) {
		CharacterGraphics cg = graphics.getCharacterByPosition(action.agentPos);

		// special handling for compound, complex, and repeating abilities.
		Runnable continuation = null;
		if (action.ability.repeats > 1) {
			continuation = () -> input.repeatTarget();
		}

		graphics.doAbility(cg, action.ability.name, continuation);

		for (Position p : action.targets) {
			CharacterInfo target = turn.getCharacterAt(p);
			if (target != null) {
				ui.updateHP(target.name, target.hp);
			}
		}

		System.err.println("Character at position " + action.agentPos.toString()
			+ " uses " + action.ability.name.toString()
			+ " targeting " + action.targets.toString());
	}

	/**
	 * Handle a character death event
	 * */
	private void death(DoCharacterDeath d) {
		CharacterGraphics cg = graphics.getCharacterByPosition(d.position);
		graphics.killCharacter(cg, d.body);
	}

	/**
	 * Handle the destruction of an object
	 * */
	private void destruction(DoObjectDestruction d) {
		graphics.destroyObject(graphics.getObjectByPosition(d.position));
	}
}

