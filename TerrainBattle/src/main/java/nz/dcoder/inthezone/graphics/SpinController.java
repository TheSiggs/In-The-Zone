package nz.dcoder.inthezone.graphics;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.control.Rotating;

public class SpinController extends AbstractControl {
	private Rotating direction = Rotating.NONE;
	private Quaternion quat = new Quaternion();
	private float rotatingSpeed = 0.0f;
	private Vector3f axis = null;

	SpinController(float rotatingSpeed, Vector3f axis) {
		this.rotatingSpeed = rotatingSpeed;
		this.axis = axis;
	}

	SpinController(float rotatingSpeed, Vector3f axis, Rotating direction) {
		this.rotatingSpeed = rotatingSpeed;
		this.axis = axis;
		this.direction = direction;
	}

	public void setDirection(Rotating direction) {
		this.direction = direction;
	}

	@Override protected void controlUpdate(float tpf) {
		if (spatial != null && enabled) {
			if (direction == Rotating.LEFT) {
				quat.fromAngleAxis(tpf * rotatingSpeed, axis);
				spatial.rotate(quat);
			} else if (direction == Rotating.RIGHT) {
				quat.fromAngleAxis(-tpf * rotatingSpeed, axis);
				spatial.rotate(quat);
			}
		}
	}

	@Override public Control cloneForSpatial(Spatial spatial) {
		final SpinController control = new SpinController(rotatingSpeed, axis);
		control.setSpatial(spatial);
		control.setDirection(direction);
		return control;
	}

	@Override protected void controlRender(RenderManager rm, ViewPort vp) {
	}
}

