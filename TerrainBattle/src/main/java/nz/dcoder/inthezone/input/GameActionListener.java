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

import nz.dcoder.inthezone.control.GameDriver;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.BoardGraphics;
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
		 * Notify input handler that an ability is repeating.
		 * */
		public synchronized void notifyRepeat() {
			if (repeats > 0) {
				System.out.println("Ability repeats");
				leftButtonMode = InputMode.TARGET;
				repeats -= 1;
			}
		}

		/**
		 * End the current turn.  To be called by the GUI.
		 * */
		public synchronized void notifyEndTurn() {
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
				driver.setViewRotating(Rotating.LEFT);
				viewLDown = true;
			}
			if (name.equals("RightView")) {
				driver.setViewRotating(Rotating.RIGHT);
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

							graphics.getBoardGraphics().clearHighlighting();
							inputState.leftButtonMode = InputMode.SELECT;
							break;
						case TARGET:
							targetMouse();

							graphics.getBoardGraphics().clearHighlighting();
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
					driver.setViewRotating(Rotating.RIGHT);
				} else {
					driver.setViewRotating(Rotating.NONE);
				}
			}
			if (name.equals("RightView")) {
				viewRDown = false;
				if (viewLDown) {
					driver.setViewRotating(Rotating.LEFT);
				} else {
					driver.setViewRotating(Rotating.NONE);
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
			if (inputState.leftButtonMode == InputMode.MOVE) {
				Position p = graphics.getBoardByMouse(mouse);

				if (p == null) {
					graphics.getBoardGraphics().clearHighlighting();

				} else if(!p.equals(lastMouse)) {
					lastMouse = p;

					List<Position> path = selected.getMove(null, p);
					graphics.getBoardGraphics().highlightTiles(
						path, BoardGraphics.PATH_COLOR);
				}

			} else if (inputState.leftButtonMode == InputMode.TARGET) {
				Position p = graphics.getTargetByMouse(mouse);
				
				if (p == null) {
					graphics.getBoardGraphics().clearHighlighting();

				} else if (!p.equals(lastMouse)) {
					lastMouse = p;

					if (!selected.canDoAbility(inputState.attackWith, p)) {
						graphics.getBoardGraphics().clearHighlighting();
						
					} else {
						Collection<Position> aoe =
							selected.getAffectedArea(inputState.attackWith, p);
						graphics.getBoardGraphics().highlightTiles(
							aoe, BoardGraphics.TARGET_COLOR);
					}
				}
			}
		}
	}

	public void selectCharacterAtMouse() {
		driver.selectCharacter(graphics.getCharacterByMouse(
			inputManager.getCursorPosition()));
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

}

