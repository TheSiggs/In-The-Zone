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

		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("A", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("D", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("C", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addMapping("LeftMouse",
			new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("RightMouse",
			new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

		// TODO: fix these mappings so they match onAction
		inputManager.addListener(this,
			"Up",
			"Right",
			"Down",
			"Left",
			"LeftMouse",
			"RightMouse",
			"A",
			"C",
			"D");
	}
	
	/**
	 * TODO: Think about action names and possible key mappings.
	 *       Should be customisable later but can be static for a start.
	 *       Maybe better names than just the key names, because these are
	 *       actions.
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
			if (name.equals("CharacterSelect")) { // can be mouse?!
			}
			if (name.equals("Attack")) {
			}
		} else {
			if (name.equals("LeftViewUp")) {
			}
			if (name.equals("RightViewUp")) {
			}
		}
	}
}
