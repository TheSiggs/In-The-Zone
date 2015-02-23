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

import nz.dcoder.inthezone.graphics.CharacterGraphics;

/**
 *
 * @author denz
 */
public class GameActionListener implements ActionListener {

	public CharacterGraphics characterGraphics;

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

		// TODO: fix these mappings so they match onAction
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
		Vector3f pos = characterGraphics.getSpatial().getLocalTranslation();
		if (isPressed) {
			if (name.equals("LeftView")) {
			}
			if (name.equals("RightView")) {
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
			}
			if (name.equals("RightView")) {
			}
		}
	}
}
