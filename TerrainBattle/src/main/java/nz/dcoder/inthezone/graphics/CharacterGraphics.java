package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
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
	private final CharacterWalkControl walkControl;

	public static final Quaternion upright = new Quaternion();

	// compute the upright quaternion
	static {
		Quaternion front = new Quaternion();
		front.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
		upright.fromAngles(FastMath.HALF_PI, 0f, 0f);
		upright.multLocal(front);
	}

	private Position p;

	public CharacterGraphics(Graphics graphics, Spatial spatial, Position p) {
		this.spatial = spatial;

		spatial.setUserData("p", new SaveablePosition(p));
		spatial.setUserData("kind", "character");

		this.setPosition(p);
		this.spatial.setLocalRotation(upright);

		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		setAnimation("idleA");

		walkControl = new CharacterWalkControl(graphics, this);
		walkControl.setSpatial(spatial);
		spatial.addControl(walkControl);
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
	 * Set the rotation of this character
	 * @param dp The position delta, i.e. target position - start position
	 * */
	public void setHeading(Position dp) {
		spatial.setLocalRotation(computeHeading(dp));
	}

	private Quaternion computeHeading(Position dp) {
		float mult;
		if (dp.x > 0) mult = -1f;
		else if (dp.x < 0) mult = 1f;
		else if (dp.y > 0) mult = 2f;
		else mult = 0f;

		Quaternion facing = new Quaternion();
		Vector3f myAxis = Vector3f.UNIT_Y;
		facing.fromAngleAxis(mult * FastMath.HALF_PI, myAxis);

		Quaternion r = upright.clone();
		r.multLocal(facing);
		return r;
	}

	public CharacterWalkControl getWalkControl() {
		return walkControl;
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

