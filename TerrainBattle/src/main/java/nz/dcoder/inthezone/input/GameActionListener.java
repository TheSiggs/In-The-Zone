/*
 * Custom ActionListener for game.
 * TODO: Implement properly.
 */
package nz.dcoder.inthezone.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
import nz.dcoder.inthezone.graphics.CharacterGraphics;

/**
 *
 * @author denz
 */
public class GameActionListener implements ActionListener {

	public CharacterGraphics characterGraphics;
	
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
