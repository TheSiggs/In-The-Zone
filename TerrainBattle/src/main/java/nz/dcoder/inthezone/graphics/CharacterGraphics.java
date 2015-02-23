/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 *
 * @author informatics-palmerson
 */
public class CharacterGraphics implements AnimEventListener {
	private final Spatial spatial;
	private final AnimChannel channel;
	private final AnimControl control;

	private Position p;

	public CharacterGraphics(Spatial spatial, Position p) {
		this.spatial = spatial;
		spatial.setUserData("p", new SaveablePosition(p));

		this.setPosition(p);

		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		setAnimation("idleA");
	}

	public Position getPosition() {
		return p;
	}

	public void setPosition(Position p) {
		this.p = p;
		((SaveablePosition) this.spatial.getUserData("p")).setPosition(p);

		float bx = ((float) p.x) * Graphics.scale;
		float by = ((float) -p.y) * Graphics.scale;
		float bz = 0.2f * Graphics.scale;
		Vector3f translation = new Vector3f(bx, by, bz);
		spatial.setLocalTranslation(translation);
		spatial.getParent().attachChild(spatial);
	}

	/**
	 * @return the spatial
	 */
	public Spatial getSpatial() {
		return spatial;
	}

	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		/*
		if (animName.equals("walk")) {
			channel.setAnim("idleA", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
		*/
	}

	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

	}

	public void setAnimation(String name) {
		channel.setAnim(name);
	}

	public void die() {
		/*
		Node parent = this.getSpatial().getParent();
		if (parent != null) {
			parent.detachChild(this.getSpatial());
		}
				*/
	}
}

