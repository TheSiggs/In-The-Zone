package nz.dcoder.inthezone.control;

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
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.EquipmentName;
import nz.dcoder.inthezone.data_model.pure.Points;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;
import nz.dcoder.inthezone.graphics.ObjectGraphics;
import nz.dcoder.inthezone.input.GameActionListener;
import nz.dcoder.inthezone.input.Rotating;

/**
 *
 * @author denz
 */
public final class Control {
	private final GameActionListener input;
	private final GameDriver driver;
	private final Graphics graphics;
	private final UserInterface ui;

	private Turn turn;

	public Control(Graphics graphics, UserInterface ui) {
		this.graphics = graphics;
		this.ui = ui;
		this.input = ui.getGameActionListener();
		this.driver = ui.getGameDriver();

		initBattleController();
	}

	/**
	 * Main update loop for the game.  Try to avoid putting things in here, use
	 * controllers instead.
	 * */
	public void simpleUpdate(float tpf) {
		Rotating viewRotating = driver.getViewRotating();
		if (viewRotating == Rotating.LEFT) {
			graphics.rotateView(tpf * Graphics.rotationSpeed);
		} else if (viewRotating == Rotating.RIGHT) {
			graphics.rotateView(-tpf * Graphics.rotationSpeed);
		}
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
			turn.getNPCInfo().stream().collect(Collectors.toList()));
	}

	private void aiPlayerTurnStart(Turn turn) {
		// normally we would invoke the AI with the new turn object.  Instead,
		// we'll make let the human player take this turn for now.
		
		System.out.println("AI turn starts");
		driver.startTurn(turn);
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
			graphics.doRun(cg, move.path, null);

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
		CharacterGraphics cg = graphics.getCharacterByPosition(action.agentPos);

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

		} else {
			// all other effects

			if (action.ability.repeats > 1) {
				continuation = () -> input.getGUIListener().notifyRepeat();
			}

			graphics.doAbility(cg, action.ability.name, continuation);

			for (Position p : action.targets) {
				CharacterInfo target = turn.getCharacterAt(p);
				if (target != null) {
					ui.updateHP(target.name, target.hp);
				}
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

