package nz.dcoder.inthezone.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.Item;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.BoardGraphics;
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

	/**
	 * Set highlighting for move range.
	 * */
	public void setMoveHighlight() {
		graphics.getBoardGraphics().setStaticHighlight(
			selectedTurnCharacter.getMoveRange(), BoardGraphics.RANGE_COLOR);
	}

	/**
	 * Set highlighting for ability range.
	 * */
	public void setRangeHighlight(AbilityName name) {
		graphics.getBoardGraphics().setStaticHighlight(
			selectedTurnCharacter.getAbilityRange(name), BoardGraphics.RANGE_COLOR);
	}

	/**
	 * Set highlighting for item range.
	 * */
	public void setItemRangeHighlight(Item item) {
		graphics.getBoardGraphics().setStaticHighlight(
			selectedTurnCharacter.getItemRange(item), BoardGraphics.RANGE_COLOR);
	}

	/**
	 * Set highlighting for current path.
	 * */
	public void setPathHighlight(Position p) {
		if (p == null) {
			graphics.getBoardGraphics().clearMovingHighlight();

		} else {
			List<Position> path = selectedTurnCharacter.getMove(null, p);
			graphics.getBoardGraphics().setMovingHighlight(
				path, BoardGraphics.PATH_COLOR);
		}
	}

	private final Collection<CharacterGraphics> targets = new ArrayList<>();

	private void clearTargetsHP() {
		for (CharacterGraphics t : targets) {
			t.hideHP();
		}
		targets.clear();
	}

	private void showTargetsHP(Collection<Position> ts) {
		clearTargetsHP();
		for (Position p : ts) {
			CharacterGraphics cg = graphics.getCharacterByPosition(p);
			cg.showHP();
			targets.add(cg);
		}
	}
	
	/**
	 * Set area of effect highlighting.
	 * */
	public void setAOEHighlight(
		Position p, AbilityName attackWith, Item useItem, boolean isItem
	) {
		if (p == null) {
			graphics.getBoardGraphics().clearMovingHighlight();
			clearTargetsHP();

		} else {
			if (
				(isItem && !selectedTurnCharacter.canUseItem(useItem, p)) ||
				(!isItem && !selectedTurnCharacter.canDoAbility(attackWith, p))
			) {
				graphics.getBoardGraphics().clearMovingHighlight();
			clearTargetsHP();
				
			} else {
				Collection<Position> aoe;
				
				if (isItem) {
					aoe = selectedTurnCharacter.getItemAffectedArea(useItem, p);
					showTargetsHP(selectedTurnCharacter.getItemTargets(useItem, p));
				} else {
					aoe = selectedTurnCharacter.getAffectedArea(attackWith, p);
					showTargetsHP(selectedTurnCharacter.getTargets(attackWith, p));
				}

				graphics.getBoardGraphics().setMovingHighlight(
					aoe, BoardGraphics.TARGET_COLOR);
			}
		}
	}

	public void moveCharacter(Position destination) {
		graphics.getBoardGraphics().clearAllHighlighting();

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
		graphics.getBoardGraphics().clearAllHighlighting();

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
		graphics.getBoardGraphics().clearAllHighlighting();

		if (selectedTurnCharacter != null && target != null) {
			if (!selectedTurnCharacter.canUseItem(item, target)) {
				System.out.println("Cannot target " + target.toString()
					+ " with item " + item.name.toString());
			} else {
				selectedTurnCharacter.useItem(item, target);
				turn.getItems().consumeItem(item.name);
				ui.updateAP(
					selectedTurnCharacter.getName(),
					selectedTurnCharacter.getAP());
				ui.updateItems(turn.getItems().getItemInfo());
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
			graphics.deselectAllCharacters();
			System.out.println("Deselected character");

		} else {
			CharacterInfo info = selectedTurnCharacter.getCharacterInfo();
			ui.selectCharacter(info);
			selectedCharacter.indicateSelected();

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

