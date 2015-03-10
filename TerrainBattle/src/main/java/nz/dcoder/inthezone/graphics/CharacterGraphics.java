package nz.dcoder.inthezone.graphics;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 *
 * @author informatics-palmerson
 */
public class CharacterGraphics extends ModelGraphics {
	public static final Quaternion upright = new Quaternion();

	// compute the upright quaternion
	static {
		Quaternion front = new Quaternion();
		front.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
		upright.fromAngles(FastMath.HALF_PI, 0f, 0f);
		upright.multLocal(front);
	}

	private final Geometry selectedIndicator;

	public CharacterGraphics(Graphics graphics, Spatial spatial, Position p) {
		super(graphics, spatial, p, "character");
		this.selectedIndicator = graphics.selectedIndicator;
		setAnimation("idleA");
	}

	public void indicateSelected() {
		Node p = selectedIndicator.getParent();
		if (p != null) p.detachChild(selectedIndicator);

		((Node) this.getSpatial()).attachChild(selectedIndicator);
		selectedIndicator.getControl(SpinController.class).setEnabled(true);

		selectedIndicator.setLocalScale(0.2f, 0.4f, 0.2f);
		selectedIndicator.setLocalTranslation(new Vector3f(0.0f, 2.0f, 0.0f));
	}

  public Quaternion getUprightRotation() {
		return upright;
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
}

