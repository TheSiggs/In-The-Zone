package nz.dcoder.inthezone.control;

import java.util.stream.Collectors;

import nz.dcoder.inthezone.data_model.BattleController;
import nz.dcoder.inthezone.data_model.Character;
import nz.dcoder.inthezone.data_model.DoAbilityInfo;
import nz.dcoder.inthezone.data_model.DoBattleEnd;
import nz.dcoder.inthezone.data_model.DoCharacterDeath;
import nz.dcoder.inthezone.data_model.DoMoveInfo;
import nz.dcoder.inthezone.data_model.DoObjectDestruction;
import nz.dcoder.inthezone.data_model.Equipment;
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.EquipmentName;
import nz.dcoder.inthezone.data_model.pure.Points;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;
import nz.dcoder.inthezone.graphics.ObjectGraphics;

/**
 *
 * @author denz
 */
public final class Control {
	private final GameActionListener input;
	private final GameDriver driver;
	private final GameState gameState;
	private final Graphics graphics;
	private final UserInterface ui;

	private Turn turn;

	public Control(GameState gameState, Graphics graphics, UserInterface ui) {
		this.gameState = gameState;
		this.graphics = graphics;
		this.ui = ui;
		this.input = ui.getGameActionListener();
		this.driver = ui.getGameDriver();

		initBattleController();
	}

	private final BattleController controller = new BattleController();

	private void initBattleController() {
		controller.onPlayerTurnStart = this::playerTurnStart;
		controller.onAIPlayerTurnStart = this::aiPlayerTurnStart;
		controller.onBattleEnd = this::endBattle;
		controller.onMove = this::move;
		controller.onAbility = this::ability;
		controller.onDeath = this::death;
		controller.onDestruction = this::destruction;
	}

	public BattleController getBattleController() {
		return controller;
	}

	/**
	 * Handle the start of the player's turn.
	 * */
	private void playerTurnStart(Turn turn) {
		driver.startTurn(turn);
		this.turn = turn;

		System.out.println("Player turn starts");

		ui.turnStart(true,
			turn.getPlayerInfo().stream().collect(Collectors.toList()),
			turn.getNPCInfo().stream().collect(Collectors.toList()),
			turn.getItems().getItemInfo());
	}

	private void aiPlayerTurnStart(Turn turn) {
		// normally we would invoke the AI with the new turn object.  Instead,
		// we'll make let the human player take this turn for now.
		
		System.out.println("AI turn starts");
		driver.startTurn(turn);
		this.turn = turn;

		ui.turnStart(true,
			turn.getPlayerInfo().stream().collect(Collectors.toList()),
			turn.getNPCInfo().stream().collect(Collectors.toList()),
			turn.getItems().getItemInfo());
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
			graphics.doRun(cg, move.path, null);

			if (move.enterLeaveManaZone) {
				TurnCharacter tc = turn.turnCharacterAt(
					move.path.get(move.path.size() - 1));
				ui.enterLeaveManaZone(tc.getCharacterInfo(), tc.isOnManaZone());
			}

		} else {
			System.out.println("Short path (length less than 2).  This shouldn't happen");
		}
	}

	// effects that need special handling
	EffectName pushEffect = new EffectName("push");
	EffectName teleportEffect = new EffectName("teleport");

	/**
	 * Handle ability effects
	 * */
	private void ability(DoAbilityInfo action) {
		// Note: agentPos is the current position of the character according to the
		// graphics layer.  agentTarget is the current position of the character
		// according to the model layer.

		CharacterGraphics cg = graphics.getCharacterByPosition(action.agentPos);
		TurnCharacter turnCharacter = turn.turnCharacterAt(action.agentTarget);

		if (turnCharacter == null) {
			throw new RuntimeException("Expected to find character at " +
				action.agentTarget.toString() + " but there was no one there.");
		}

		CharacterInfo info = turnCharacter.getCharacterInfo();
		
		// TODO: revist
		Runnable continuation = null;

		if (action.ability.effect.equals(pushEffect)) {
			// corpse pushing
			ObjectGraphics og = graphics.getObjectByPosition(
				action.targets.iterator().next());
			graphics.doPush(cg, og, action.agentTarget, continuation);

		} else if (action.ability.effect.equals(teleportEffect)) {
			// basic teleporting
			graphics.doTeleport(cg, action.agentTarget, continuation);
			ui.showMessage(info.name.toString() + " uses " +
				action.ability.name.toString() + "!");

		} else {
			// all other effects

			if (action.ability.repeats > 1) {
				continuation = () -> input.getGUIListener().notifyRepeat();
			}

			ui.showMessage(info.name.toString() + " uses " +
				action.ability.name.toString() + "!");

			graphics.doAbility(cg, action.ability.name, continuation);

			for (Position p : action.targets) {
				CharacterGraphics cgTarget = graphics.getCharacterByPosition(p);
				CharacterInfo target = turn.getCharacterAt(p);
				if (target != null) {
					ui.updateHP(target.name, target.hp);
					cgTarget.setHP(target.hp);
				}
			}
		}

		ui.selectCharacter(turn.turnCharacterAt(
				action.agentTarget).getCharacterInfo());

		System.err.println("Character at position " + action.agentPos.toString()
			+ " uses " + action.ability.name.toString()
			+ " targeting " + action.targets.toString());
	}

	/**
	 * Handle a character death event
	 * */
	private void death(DoCharacterDeath d) {
		graphics.killCharacters(d.bodies);
	}

	/**
	 * Handle the destruction of an object
	 * */
	private void destruction(DoObjectDestruction d) {
		graphics.destroyObjects(d.positions);
	}
}

