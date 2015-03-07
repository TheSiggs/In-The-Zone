package nz.dcoder.inthezone.control;

import java.util.List;

import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.Item;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;

/**
 * This is where every command to the game begins.  From here, we call into the
 * data layer.  The data layer calls into Presentation.java, which calls
 * Graphics.java which does the animations.  Graphics may call back into here
 * (via a continuation) starting the whole cycle all over again.
 * */
public class GameDriver {
	private final Graphics graphics;
	private final UserInterface ui;
	private final GameState gameState;

	public GameDriver(Graphics graphics, GameState gameState, UserInterface ui) {
		this.graphics = graphics;
		this.gameState = gameState;
		this.ui = ui;
	}

	public GameState getGameState() {
		return gameState;
	}

	private Rotating viewRotating = Rotating.NONE;

	/**
	 * Determine if the view is currently rotating, and in which direction.
	 * */
	public Rotating getViewRotating() {
		return viewRotating;
	}

	/**
	 * Set the direction the view is rotating
	 * */
	public void setViewRotating(Rotating viewRotating) {
		this.viewRotating = viewRotating;
	}

	private TurnCharacter selectedTurnCharacter;
	private CharacterGraphics selectedCharacter;

	public TurnCharacter getSelectedTurnCharacter() {
		return selectedTurnCharacter;
	}

	private Turn turn = null;

	/**
	 * Start a turn
	 * */
	public void startTurn(Turn turn) {
		this.turn = turn;
	}

	/**
	 * End the current turn
	 * */
	public void endTurn() {
		turn.endTurn();
	}

	public void moveCharacter(Position destination) {
		if (selectedTurnCharacter != null && destination != null) {
			List<Position> path =
				selectedTurnCharacter.getMove(null, destination);

			if (path == null) {
				System.out.println("Bad path or not enough MP");

			} else {
				selectedTurnCharacter.doMotion(path);
				ui.updateMP(
					selectedTurnCharacter.getName(),
					selectedTurnCharacter.getMP());
			}
		}
	}

	public void targetPosition(AbilityName attackWith, Position target) {
		if (selectedTurnCharacter != null && target != null) {
			if (!selectedTurnCharacter.canDoAbility(attackWith, target)) {
				System.out.println("Cannot target " + target.toString()
					+ " with ability " + attackWith.toString());
			} else {
				selectedTurnCharacter.doAbility(attackWith, target);
				ui.updateAP(
					selectedTurnCharacter.getName(),
					selectedTurnCharacter.getAP());
				ui.updateMP(
					selectedTurnCharacter.getName(),
					selectedTurnCharacter.getMP());
			}
		}
	}

	public void targetItemPosition(Item item, Position target) {
		if (selectedTurnCharacter != null && target != null) {
			if (!selectedTurnCharacter.canUseItem(item, target)) {
				System.out.println("Cannot target " + target.toString()
					+ " with item " + item.name.toString());
			} else {
				selectedTurnCharacter.useItem(item, target);
				gameState.party.consumeItem(item.name);
				ui.updateAP(
					selectedTurnCharacter.getName(),
					selectedTurnCharacter.getAP());
				ui.updateItems(gameState.party.getItemInfo());
			}
		}
	}

	public void selectCharacter(Position p) {
		selectCharacter(graphics.getCharacterByPosition(p));
	}

	public void selectCharacter(CharacterGraphics cg) {
		selectedCharacter = cg;
		if (cg == null) {
			selectedTurnCharacter = null;
		} else {
			selectedTurnCharacter = turn.turnCharacterAt(cg.getPosition());
			if (selectedTurnCharacter == null) {
				System.out.println("cannot select enemy characters");
				selectedCharacter = null;
			}
		}

		if (selectedCharacter == null) {
			ui.deselectCharacter();
			System.out.println("Deselected character");

		} else {
			CharacterInfo info = selectedTurnCharacter.getCharacterInfo();
			ui.selectCharacter(info);

			System.out.println("Selected character " +
				selectedTurnCharacter.getName().toString() + " at " +
				selectedCharacter.getPosition().toString());
			System.out.println(
				"mp: " + info.mp.toString() +
				", ap: " + info.ap.toString() +
				", hp: " + info.hp.toString());
		}
	}
}

