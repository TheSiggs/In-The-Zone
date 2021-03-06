package nz.dcoder.inthezone.control;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;

import nz.dcoder.inthezone.data_model.Item;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;

/**
 *
 * @author denz
 */
public class GameActionListener implements ActionListener, AnalogListener {
	private final InputManager inputManager;
	private final Graphics graphics;

	private final GameDriver driver;

	public GameActionListener(
		InputManager inputManager, Graphics graphics, GameDriver driver
	) {
		this.inputManager = inputManager;
		this.graphics = graphics;
		this.driver = driver;

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

	private boolean viewLDown = false;
	private boolean viewRDown = false;
	private boolean isAnimating = false;

	/**
	 * Notify the input system when animation begins.  It is not possible to
	 * launch an attack etc. while an animation is playing.
	 * */
	public void notifyAnimationStart() {
		isAnimating = true;
	}

	/**
	 * Notify the input system when an animation ends.
	 * */
	public void notifyAnimationEnd() {
		isAnimating = false;
	}

	public boolean getIsAnimating() {
		return isAnimating;
	}

	// Access to this object must be synchronized
	GUIListener inputState = new GUIListener();

	public GUIListener getGUIListener() {
		return inputState;
	}

	/**
	 * Use an inner class to restrict the GUI to invoking only the permitted
	 * methods, and to enforce synchronization.
	 * */
	public class GUIListener {
		private InputMode leftButtonMode = InputMode.SELECT;
		private int repeats = 0;
		private AbilityName attackWith;
		private Item useItem;

		/**
		 * Notify input handler (this) that the user selected the move action.
		 * */
		public synchronized void notifyMove() {
			leftButtonMode = InputMode.MOVE;
			driver.setMoveHighlight();
		}

		public synchronized void notifyItem(Item item) {
			useItem = item;
			leftButtonMode = InputMode.ITEM_TARGET;
			driver.setItemRangeHighlight(item);
		}

		/**
		 * Notify input handler (this) that the user selected the attack action.
		 * */
		public synchronized void notifyTarget(AbilityName ability, int repeats) {
			attackWith = ability;
			leftButtonMode = InputMode.TARGET;
			repeats = repeats - 1;
			driver.setRangeHighlight(ability);
		}

		/**
		 * Notify input handler that an ability is repeating.
		 * */
		public synchronized void notifyRepeat() {
			if (repeats > 0) {
				System.out.println("Ability repeats");
				leftButtonMode = InputMode.TARGET;
				repeats -= 1;

				driver.setRangeHighlight(attackWith);
			}
		}

		/**
		 * End the current turn.  To be called by the GUI.
		 * */
		public synchronized void notifyEndTurn() {
			leftButtonMode = InputMode.SELECT;
			driver.endTurn();
		}
	}

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
	public void onAction(
		String name, boolean isPressed, float tpf
	) {
		if (isPressed) {
			// actions that work at any time
			// =============================
			if (name.equals("LeftView")) {
				graphics.boardSpinner.setDirection(Rotating.LEFT);
				viewLDown = true;
			}
			if (name.equals("RightView")) {
				graphics.boardSpinner.setDirection(Rotating.RIGHT);
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
				synchronized(inputState) {
					switch (inputState.leftButtonMode) {
						case SELECT:
							selectCharacterAtMouse();
							break;
						case MOVE:
							moveCharacterToMouse();
							inputState.leftButtonMode = InputMode.SELECT;
							break;
						case TARGET:
							targetMouse();
							inputState.leftButtonMode = InputMode.SELECT;
							break;
						case ITEM_TARGET:
							targetItemMouse();
							inputState.leftButtonMode = InputMode.SELECT;
							break;
					}
				}
			}
			if (name.equals("RightMouse")) {
			}
		} else {
			if (name.equals("LeftView")) {
				viewLDown = false;
				if (viewRDown) {
					graphics.boardSpinner.setDirection(Rotating.RIGHT);
				} else {
					graphics.boardSpinner.setDirection(Rotating.NONE);
				}
			}
			if (name.equals("RightView")) {
				viewRDown = false;
				if (viewLDown) {
					graphics.boardSpinner.setDirection(Rotating.LEFT);
				} else {
					graphics.boardSpinner.setDirection(Rotating.NONE);
				}
			}
		}
	}

	private Position lastMouse = new Position(0, 0);

	@Override
	public void onAnalog(String name, float value, float tpf) {
		if (name.equals("MouseMove")) {
			Vector2f mouse = inputManager.getCursorPosition();
			TurnCharacter selected = driver.getSelectedTurnCharacter();
			onMouseMove(mouse, selected);
		}
	}

	private void onMouseMove(Vector2f mouse, TurnCharacter selected) {
		synchronized(inputState) {
			boolean isItem = inputState.leftButtonMode == InputMode.ITEM_TARGET;

			if (inputState.leftButtonMode == InputMode.SELECT) {
				CharacterGraphics cg = graphics.getCharacterByMouse(mouse, true);
				Position p = null;
				if (cg != null) p = cg.getPosition();

				if ((p == null && lastMouse != null) || (p != null && !p.equals(lastMouse))) {
					lastMouse = p;
					driver.setMouseOverCharacter(cg);
				}

			} else if (inputState.leftButtonMode == InputMode.MOVE) {
				Position p = graphics.getBoardByMouse(mouse);

				if (p == null || !p.equals(lastMouse)) {
					lastMouse = p;
					driver.setPathHighlight(p);
				}

			} else if (inputState.leftButtonMode == InputMode.TARGET || isItem) {
				Position p = graphics.getTargetByMouse(mouse);

				if (p == null || !p.equals(lastMouse)) {
					lastMouse = p;
					driver.setAOEHighlight(p, inputState.attackWith,
						inputState.useItem, isItem);
				}
			}
		}
	}

	public void selectCharacterAtMouse() {
		driver.selectCharacter(graphics.getCharacterByMouse(
			inputManager.getCursorPosition(), false));
	}

	public void moveCharacterToMouse() {
		Position destination =
			graphics.getBoardByMouse(inputManager.getCursorPosition());
		driver.moveCharacter(destination);
	}

	public void targetMouse() {
		synchronized(inputState) {
			Position target =	
				graphics.getTargetByMouse(inputManager.getCursorPosition());
			driver.targetPosition(inputState.attackWith, target);
		}
	}

	public void targetItemMouse() {
		synchronized(inputState) {
			Position target =	
				graphics.getTargetByMouse(inputManager.getCursorPosition());
			driver.targetItemPosition(inputState.useItem, target);
		}
	}

}

