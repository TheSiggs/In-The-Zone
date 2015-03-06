package nz.dcoder.inthezone.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;
import java.util.List;
import java.util.Collection;

import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.Points;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.BoardGraphics;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;
import nz.dcoder.inthezone.UserInterface;

/**
 *
 * @author denz
 */
public class GameActionListener implements ActionListener, AnalogListener {
	private final InputManager inputManager;
	private final Graphics graphics;
	private final UserInterface ui;

	/**
	 * Any method that reads or writes these fields must be synchronized
	 * */
	private InputMode leftButtonMode = InputMode.SELECT;
	private Turn turn = null;
	private AbilityName attackWith = null;
	private int repeats = 0;

	public GameActionListener(
		InputManager inputManager, Graphics graphics, UserInterface ui
	) {
		this.inputManager = inputManager;
		this.graphics = graphics;
		this.ui = ui;

		inputManager.addMapping("ForwardsMove", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("RightMove", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("BackwardsMove", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("LeftMove", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("LeftView", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("RightView", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("LeftMouse",
			new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("RightMouse",
			new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

		inputManager.addMapping("MouseMove", new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("MouseMove", new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("MouseMove", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		inputManager.addMapping("MouseMove", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

		inputManager.addListener(this,
				"ForwardsMove",
				"RightMove",
				"BackwardsMove",
				"LeftMove",
				"LeftView",
				"RightView",
				"LeftMouse",
				"RightMouse",
				"MouseMove");
	}

	private Rotating viewRotating = Rotating.NONE;
	private boolean viewLDown = false;
	private boolean viewRDown = false;
	private CharacterGraphics walking = null;
	private boolean isAnimating = false;

	public void notifyAnimationStart() {
		isAnimating = true;
	}

	public void notifyAnimationEnd() {
		isAnimating = false;
	}

	public boolean getIsAnimating() {
		return isAnimating;
	}

	/**
	 * Determine if the view is currently rotating, and in which direction.
	 * */
	public Rotating getViewRotating() {
		return viewRotating;
	}

	private TurnCharacter selectedTurnCharacter;
	private CharacterGraphics selectedCharacter;

	public CharacterGraphics getSelectedCharacter() {
		return selectedCharacter;
	}

	/**
	 * Set the current turn object.
	 * */
	public synchronized void setTurn(Turn turn) {
		this.turn = turn;
	}

	/**
	 * Notify input handler (this) that the user selected the move action.
	 * */
	public synchronized void notifyMove() {
		leftButtonMode = InputMode.MOVE;
	}

	/**
	 * Notify input handler (this) that the user selected the attack action.
	 * */
	public synchronized void notifyTarget(AbilityName ability, int repeats) {
		attackWith = ability;
		leftButtonMode = InputMode.TARGET;
		repeats = repeats - 1;
	}

	/**
	 * End the current turn.  To be called by the GUI.
	 * */
	public synchronized void notifyEndTurn() {
		turn.endTurn();
	}

	/**
	 * Notify input handler (this) that an ability is repeating.
	 * */
	public synchronized void repeatTarget() {
		if (repeats > 0) {
      System.out.println("Ability repeats");
			leftButtonMode = InputMode.TARGET;
			repeats -= 1;
		}
	}

	private int clickCount = 0;

	/**
	 * TODO: Think about action names and possible key mappings. Should be
	 * customisable later but can be static for a start. Maybe better names than
	 * just the key names, because these are actions.
	 *
	 * @param name Action name like "ForwardsView"
	 * @param isPressed whether the key is pressed or not
	 * @param tpf time per frame
	 */
	@Override
	public synchronized void onAction(
		String name, boolean isPressed, float tpf
	) {
		if (isPressed) {
			// actions that work at any time
			// =============================
			if (name.equals("LeftView")) {
				viewRotating = Rotating.LEFT;
				viewLDown = true;
			}
			if (name.equals("RightView")) {
				viewRotating = Rotating.RIGHT;
				viewRDown = true;
			}

			// actions that are mutually exclusive.  For example walking: while a
			// character is walking, nothing else is allowed to happen
			// ==================================================================
			if (isAnimating) return;

			if (name.equals("ForwardsMove")) {
			}
			if (name.equals("RightMove")) {
			}
			if (name.equals("BackwardsMove")) {
			}
			if (name.equals("LeftMove")) {
			}
			if (name.equals("LeftMouse")) {
				switch (leftButtonMode) {
					case SELECT:
						selectCharacterAtMouse();
						break;
					case MOVE:
						moveCharacterToMouse();

						graphics.getBoardGraphics().clearHighlighting();
						leftButtonMode = InputMode.SELECT;
						break;
					case TARGET:
						targetMouse();

						graphics.getBoardGraphics().clearHighlighting();
						leftButtonMode = InputMode.SELECT;
						break;
				}
			}
			if (name.equals("RightMouse")) {
			}
		} else {
			if (name.equals("LeftView")) {
				viewLDown = false;
				if (viewRDown) {
					viewRotating = Rotating.RIGHT;
				} else {
					viewRotating = Rotating.NONE;
				}
			}
			if (name.equals("RightView")) {
				viewRDown = false;
				if (viewLDown) {
					viewRotating = Rotating.LEFT;
				} else {
					viewRotating = Rotating.NONE;
				}
			}
		}
	}

	private Position lastMouse = new Position(0, 0);

	@Override
	public synchronized void onAnalog(String name, float value, float tpf) {
		if (name.equals("MouseMove")) {
			Vector2f mouse = inputManager.getCursorPosition();

			if (leftButtonMode == InputMode.MOVE) {
				Position p = graphics.getBoardByMouse(mouse);

				if (p == null) {
					graphics.getBoardGraphics().clearHighlighting();

				} else if(!p.equals(lastMouse)) {
					lastMouse = p;

					List<Position> path = selectedTurnCharacter.getMove(null, p);
					graphics.getBoardGraphics().highlightTiles(
						path, BoardGraphics.PATH_COLOR);
				}

			} else if (leftButtonMode == InputMode.TARGET) {
				Position p = graphics.getTargetByMouse(mouse);
				
				if (p == null) {
					graphics.getBoardGraphics().clearHighlighting();

				} else if (!p.equals(lastMouse)) {
					lastMouse = p;

					if (!selectedTurnCharacter.canDoAbility(attackWith, p)) {
						graphics.getBoardGraphics().clearHighlighting();
						
					} else {
						Collection<Position> aoe =
							selectedTurnCharacter.getAffectedArea(attackWith, p);
						graphics.getBoardGraphics().highlightTiles(
							aoe, BoardGraphics.TARGET_COLOR);
					}
				}
			}

		}
	}

	private void selectCharacterAtMouse() {
		selectCharacter(graphics.getCharacterByMouse(
			inputManager.getCursorPosition()));

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

	private void moveCharacterToMouse() {
		Position destination =
			graphics.getBoardByMouse(inputManager.getCursorPosition());

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

	private void targetMouse() {
		Position target =	
			graphics.getTargetByMouse(inputManager.getCursorPosition());

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

	private synchronized void selectCharacter(CharacterGraphics cg) {
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
	}
}

