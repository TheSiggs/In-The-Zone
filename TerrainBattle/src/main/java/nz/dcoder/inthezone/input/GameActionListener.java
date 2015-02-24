/*
 * Custom ActionListener for game.
 * TODO: Implement properly.
 */
package nz.dcoder.inthezone.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import java.util.List;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Turn;
import nz.dcoder.inthezone.data_model.TurnCharacter;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.Graphics;

/**
 *
 * @author denz
 */
public class GameActionListener implements ActionListener {
	private final InputManager inputManager;
	private final Graphics graphics;

	public GameActionListener(InputManager inputManager, Graphics graphics) {
		this.inputManager = inputManager;
		this.graphics = graphics;

		inputManager.addMapping("ForwardsMove", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("RightMove", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("BackwardsMove", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("LeftMove", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("LeftView", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("RightView", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("CharacterSelect",
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("Move",
				new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

		inputManager.addListener(this,
				"ForwardsMove",
				"RightMove",
				"BackwardsMove",
				"LeftMove",
				"LeftView",
				"RightView",
				"CharacterSelect",
				"Move");
	}

	private Rotating viewRotating = Rotating.NONE;
	private Rotating oldViewRotating = Rotating.NONE;
	private CharacterGraphics walking = null;
	private boolean isCharacterWalking = false;

	public void startCharacterWalking(CharacterGraphics cg) {
		walking = cg;
		isCharacterWalking = true;
	}

	public void stopCharacterWalking(CharacterGraphics cg) {
		walking = null;
		isCharacterWalking = false;
	}

	public boolean getIsCharacterWalking() {
		return isCharacterWalking;
	}

	/**
	 * Determine if the view is currently rotating, and in which direction.
	 * */
	public Rotating getViewRotating() {
		return viewRotating;
	}

	private Turn turn = null;
	private TurnCharacter selectedTurnCharacter;
	private CharacterGraphics selectedCharacter;

	public CharacterGraphics getSelectedCharacter() {
		return selectedCharacter;
	}

	public void selectCharacter(CharacterGraphics cg) {
		// TODO: notify GUI
		selectedCharacter = cg;
		if (cg == null) {
			selectedTurnCharacter = null;
		} else {
			selectedTurnCharacter = turn.turnCharacterAt(cg.getPosition());
			if (selectedTurnCharacter == null) {
				// TODO: notify GUI?
				System.out.println("cannot select enemy characters");
				selectedCharacter = null;
			}
		}
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
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
	public void onAction(String name, boolean isPressed, float tpf) {
		if (isPressed) {
			if (name.equals("LeftView")) {
				if (viewRotating != Rotating.LEFT) oldViewRotating = viewRotating;
				viewRotating = Rotating.LEFT;
			}
			if (name.equals("RightView")) {
				if (viewRotating != Rotating.RIGHT) oldViewRotating = viewRotating;
				viewRotating = Rotating.RIGHT;
			}
			if (name.equals("ForwardsMove")) {
			}
			if (name.equals("RightMove")) {
			}
			if (name.equals("BackwardsMove")) {
			}
			if (name.equals("LeftMove")) {
			}
			if (name.equals("CharacterSelect") && !isCharacterWalking) {
				// left mouse

				selectCharacter(graphics.getCharacterByMouse(
					inputManager.getCursorPosition()));

				if (selectedCharacter == null) {
					System.out.println("Deselected character");
				} else {
					System.out.println("Selected character at " +
						selectedCharacter.getPosition().toString());
				}
			}
			if (name.equals("Move") && !isCharacterWalking) {
				// right mouse, for now

				Position destination =
					graphics.getBoardByMouse(inputManager.getCursorPosition());

				if (selectedTurnCharacter != null && destination != null) {
					List<Position> path = selectedTurnCharacter.getMove(
						null, destination);
					if (path == null) {
						System.out.println("Bad path or not enough MP");
					} else {
						selectedTurnCharacter.doMotion(path);
					}
				}

			}
		} else {
			if (name.equals("LeftView")) {
				viewRotating = oldViewRotating;
				oldViewRotating = Rotating.NONE;
			}
			if (name.equals("RightView")) {
				viewRotating = oldViewRotating;
				oldViewRotating = Rotating.NONE;
			}
		}
	}

}

