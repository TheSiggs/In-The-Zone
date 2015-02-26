package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.List;

import nz.dcoder.inthezone.data_model.pure.Position;

public class CharacterWalkControl extends AbstractControl {
	private final Graphics graphics;
	private final CharacterGraphics cg;

	// the path
	private Spline spline = null;
	private int nControlPoints = 0;
	private Consumer<CharacterGraphics> endNotify = null;
	private Collection<CharacterGraphics> charactersToRestore = null;
	// progress along the path
	private int controlPoint = 0;
	private float t = 0;

	public CharacterWalkControl (Graphics graphics, CharacterGraphics cg) {
		this.graphics = graphics;
		this.cg = cg;
		this.setEnabled(false);
	}

	/**
	 * Walk along a set path
	 * @param path A list of all the positions the character will walk across,
	 * including the current position and the target position.
	 * @param endNotify A method reference that will be invoked when the walk
	 * action ends
	 * */
	public void doWalk(
		List<Position> path, Consumer<CharacterGraphics> endNotify
	) {
		this.endNotify = endNotify;

		Spline spline0 = new Spline();
		nControlPoints = path.size();
		for (Position p : path) {
			spline0.addControlPoint(Graphics.positionToVector(p));
		}
		spline0.setType(Spline.SplineType.CatmullRom);
		spline0.setCurveTension(0.6f);

		spline = adjustSplineForCharacters(path, spline0);

		controlPoint = 0;
		t = 0;

		// do this after adjusting the spline
		this.cg.setPositionInternal(path.get(path.size() - 1));
		this.setEnabled(true);
	}

	private Spline adjustSplineForCharacters(List<Position> path, Spline s) {
		Spline r = new Spline();
		List<Vector3f> controlPoints = s.getControlPoints();

		r.addControlPoint(controlPoints.get(0));

		charactersToRestore = new ArrayList<CharacterGraphics>();

		for (int i = 1; i < nControlPoints; i++) {
			CharacterGraphics c = graphics.getCharacterByPosition(path.get(i));
			if (c == null) {
				r.addControlPoint(controlPoints.get(i));
			} else {
				charactersToRestore.add(c);

				Vector3f p0 = s.interpolate(1f - (1f/16f), i - 1, null);
				Vector3f p1 = s.interpolate(1f/16f, i , null);
				Vector3f tangent = p1.subtract(p0);
				Vector3f norm = new Vector3f(
					-tangent.getY(), tangent.getX(), tangent.getZ());
				norm.normalizeLocal().multLocal(Graphics.scale);

				// adjust path to go around the other character
				r.addControlPoint(controlPoints.get(i).add(norm));
				norm.negateLocal();
				// and make the other character jump out of the way
				c.getSpatial().setLocalTranslation(norm);
			}
		}

		r.setType(s.getType());
		r.setCurveTension(s.getCurveTension());

		return r;
	}

	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			cg.setAnimation("run");
		} else {
			cg.setAnimation("idleA");
		}
	}

	@Override protected void controlUpdate(float tpf) {
		if (!this.isEnabled()) return;

		t += tpf * Graphics.travelSpeed;
		while (t > 1) {
			controlPoint += 1;
			t -= 1;
		}

		if (controlPoint + 1 >= nControlPoints) {
			// we're at the end of the path
			// centre the characters on their squares
			cg.setPosition(cg.getPosition());
			for (CharacterGraphics c : charactersToRestore) {
				c.setPosition(c.getPosition());
			}

			// clean up and notify presentation layer that we're done
			charactersToRestore = null;
			this.setEnabled(false);
			if (endNotify != null) endNotify.accept(cg);
		} else {
			Vector3f p0 = spline.interpolate(t, controlPoint, null);
			spatial.setLocalTranslation(p0);

			// compute the heading
			int c1 = controlPoint;
			float t1 = t + (tpf * Graphics.travelSpeed);
			while (t1 > 1) {
				t1 -= 1;
				c1 += 1;
			}
			if (c1 + 1 >= nControlPoints) {
				c1 = nControlPoints - 2;
				t1 = 1;
			}

			Vector3f p1 = spline.interpolate(t1, c1, null);
			Vector3f tangent = p1.subtract(p0);
			tangent.normalizeLocal();

			// rotate the character
			Quaternion r = CharacterGraphics.upright.clone();
			Quaternion facing = new Quaternion();
			facing.fromAngleAxis(
				tangent.angleBetween(Vector3f.UNIT_Z), Vector3f.UNIT_Y);
			r.multLocal(facing);
			spatial.setLocalRotation(r);
		}
	}

	@Override protected void controlRender(RenderManager rm, ViewPort vp) {
		// do nothing
	}

	@Override public Control cloneForSpatial(Spatial spatial) {
		CharacterGraphics c = graphics.getCharacterByPosition(
			((SaveablePosition) spatial.getUserData("p")).getPosition());

		final CharacterWalkControl control = new CharacterWalkControl(graphics, c);
		control.setSpatial(spatial);
		return control;
	}
}

