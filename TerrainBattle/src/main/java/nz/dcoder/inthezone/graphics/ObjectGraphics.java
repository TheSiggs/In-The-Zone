package nz.dcoder.inthezone.graphics;

import com.jme3.animation.Animation;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents the visual part of a battle object (such as a corpse or a
 * boulder)
 * */
public class ObjectGraphics extends ModelGraphics {
	public ObjectGraphics(Graphics graphics, Spatial spatial, Position p) {
		super(graphics, spatial, p, "object");
	}

	private static final Quaternion upright =
		new Quaternion().fromAngles(0, 0, 0);

  public Quaternion getUprightRotation() {
		return upright;
	}

	/**
	 * Hack to allow extending a model with animations that weren't supplied by
	 * the artist.  This should be removed at first opportunity.
	 * */
	void addAnim(Animation anim) {
		this.control.addAnim(anim);
	}
}

