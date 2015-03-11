package nz.dcoder.inthezone.graphics;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Points;
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
	private final HealthBarGraphics healthBar;

	public CharacterGraphics(Graphics graphics, Spatial spatial, Position p) {
		super(graphics, spatial, p, "character");
		this.selectedIndicator = graphics.selectedIndicator;
		this.healthBar = new HealthBarGraphics(graphics);
		setAnimation("idleA");
	}

	/**
	 * Indicate that this character is selected.
	 * */
	public void indicateSelected() {
		Node p = selectedIndicator.getParent();
		if (p != null) p.detachChild(selectedIndicator);

		((Node) spatial).attachChild(selectedIndicator);
		selectedIndicator.getControl(SpinController.class).setEnabled(true);

		selectedIndicator.setLocalScale(0.2f, 0.4f, 0.2f);
		selectedIndicator.setLocalTranslation(
			new Vector3f(0.0f, 2.0f, 0.0f));
	}

	/**
	 * Update the health bar for this character (if it's visible).  The health
	 * bar appears to show the new HP, then disappears after a delay.
	 * */
	public void setHP(Points hp) {
		showHP();
		healthBar.setHP(hp);
	}

	private boolean healthVisible = false;

	/**
	 * Show the health bar for this character.
	 * */
	public void showHP() {
		if (!healthVisible) {
			healthBar.showHealth();
			Spatial bar = healthBar.getSpatial();
			((Node) spatial).attachChild(bar);
			bar.setLocalTranslation(new Vector3f(0.5f, 1.8f, 0.0f));
			healthVisible = true;
		}
	}

	/**
	 * Hide the health bar for this character.
	 * */
	public void hideHP() {
		if (healthVisible) {
			healthBar.hideHealth();
			healthVisible = false;
		}
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

