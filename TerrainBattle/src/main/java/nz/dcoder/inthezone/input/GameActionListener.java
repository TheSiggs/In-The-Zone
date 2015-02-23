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
import com.jme3.math.Vector3f;

/**
 *
 * @author denz
 */
public class GameActionListener implements ActionListener {
	private final InputManager inputManager;

	public GameActionListener(InputManager inputManager) {
		this.inputManager = inputManager;

		inputManager.addMapping("ForwardsMove", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("RightMove", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("BackwardsMove", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("LeftMove", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("LeftView", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("RightView", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("CharacterSelect",
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("Attack",
				new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

		inputManager.addListener(this,
				"ForwardsMove",
				"RightMove",
				"BackwardsMove",
				"LeftMove",
				"LeftView",
				"RightView",
				"CharacterSelect",
				"Attack");
	}

	private Rotating viewRotating = Rotating.NONE;
	private Rotating oldViewRotating = Rotating.NONE;

	/**
	 * Determine if the view is currently rotating, and in which direction.
	 * */
	public Rotating getViewRotating() {
		return viewRotating;
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
			if (name.equals("CharacterSelect")) { // left mouse
			}
			if (name.equals("Attack")) { // right mouse
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

