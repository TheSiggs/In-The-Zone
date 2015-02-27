package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents the visual part of a battle object (such as a corpse or a
 * boulder)
 * */
public class ObjectGraphics implements AnimEventListener {
	private final Spatial spatial;
	private final AnimChannel channel;
	private final AnimControl control;

	private Position p;

	public ObjectGraphics(Spatial spatial, Position p) {
		this.spatial = spatial;

		spatial.setUserData("p", new SaveablePosition(p));
		spatial.setUserData("kind", "object");

		this.setPosition(p);

		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
	}

	public Position getPosition() {
		return p;
	}

	void setPositionInternal(Position p) {
		this.p = p;
		((SaveablePosition) this.spatial.getUserData("p")).setPosition(p);
	}

	public void setPosition(Position p) {
		setPositionInternal(p);

		Vector3f translation = Graphics.positionToVector(p);
		spatial.setLocalTranslation(translation);
	}

	/**
	 * @return the spatial
	 */
	public Spatial getSpatial() {
		return spatial;
	}

	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
	}

	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

	}

	public void setAnimation(String name) {
		channel.setAnim(name);
	}

}

