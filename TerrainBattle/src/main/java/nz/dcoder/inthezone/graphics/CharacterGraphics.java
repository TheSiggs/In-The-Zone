package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 *
 * @author informatics-palmerson
 */
public class CharacterGraphics implements AnimEventListener {
	private final Spatial spatial;
	private final AnimChannel channel;
	private final AnimControl control;
	private static final Quaternion upright = new Quaternion();

	// compute the upright quaternion
	static {
		Quaternion front = new Quaternion();
		front.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
		upright.fromAngles(FastMath.HALF_PI, 0f, 0f);
		upright.multLocal(front);
	}

	private Position p;

	public CharacterGraphics(Spatial spatial, Position p) {
		this.spatial = spatial;

		spatial.setUserData("p", new SaveablePosition(p));
		spatial.setUserData("kind", "character");

		this.setPosition(p);
		this.spatial.setLocalRotation(upright);

		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		setAnimation("idleA");
	}

	public Position getPosition() {
		return p;
	}

	private void setPositionInternal(Position p) {
		this.p = p;
		((SaveablePosition) this.spatial.getUserData("p")).setPosition(p);
	}

	public void setPosition(Position p) {
		setPositionInternal(p);

		Vector3f translation = positionToVector(p);
		spatial.setLocalTranslation(translation);
	}

	public static Vector3f positionToVector(Position p) {
		float bx = ((float) p.x) * Graphics.scale;
		float by = ((float) -p.y) * Graphics.scale;
		float bz = 0.2f * Graphics.scale;
		return new Vector3f(bx, by, bz);
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

	/**
	 * Walk along a set path
	 * @param path A list of all the positions the character will walk across,
	 * including the current position and the target position.
	 * @param endNotify A method reference that will be invoked when the walk
	 * action ends
	 * */
	public void walk(
		List<Position> path, Consumer<CharacterGraphics> endNotify
	) {
		MotionPath mpath = new MotionPath();
		List<Position> headings = new ArrayList<Position>();

		// compute the first heading
		Position p0 = path.remove(0);

		for (Position p : path) {
			mpath.addWayPoint(positionToVector(p));
			headings.add(p.sub(p0));
			p0 = p;
		}

		setPositionInternal(p0);

		MotionEvent motionControl = new MotionEvent(spatial, mpath);
		motionControl.setDirectionType(MotionEvent.Direction.Rotation);
		motionControl.setRotation(computeHeading(headings.get(0)));
		motionControl.setInitialDuration(
			((float) (path.size() - 1)) / Graphics.travelSpeed);

		setAnimation("walk");

		CharacterGraphics instance = this;
		mpath.addListener(new MotionPathListener() {
			public void onWayPointReach(MotionEvent control, int wayPointIndex) {
				if (mpath.getNbWayPoints() == wayPointIndex + 1) {
					// HACK.  This shouldn't be necessary.  It might be a bug in JME to
					// do with very low framerates.  We shall have to investigate
					spatial.setLocalTranslation(
						mpath.getWayPoint(mpath.getNbWayPoints() - 1));

					setAnimation("idleA");
					endNotify.accept(instance);
				} else {
					motionControl.setRotation(
						computeHeading(headings.get(wayPointIndex + 1)));
				}
			}
		});

		motionControl.play();
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

