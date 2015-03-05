package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.Control;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.List;

import nz.dcoder.inthezone.data_model.pure.Position;

public class PathController extends ChainableController {
	private final Graphics graphics;
	private final ModelGraphics mg;

	// the path
	private Spline spline = null;
	private int nControlPoints = 0;
	private Collection<CharacterGraphics> charactersToRestore = null;
  private Position finalPosition = null;
	// progress along the path
	private int controlPoint = 0;
	private float t = 0;

	public PathController (Graphics graphics, ModelGraphics mg) {
		super(graphics.getControllerChain());
		this.graphics = graphics;
		this.mg = mg;
		this.setEnabled(false);
	}

  private String animation = null;
  private String restoreAnimation = null;
	private boolean rotate;
	private float speed = 1f;

	public void doWalk(List<Position> path, boolean rotate) {
		animation = "walk";
		restoreAnimation = mg.getAnimation();
		this.rotate = rotate;
		this.speed = Graphics.WALK_SPEED;
		activate(path, true);
	}

	public void doRun(List<Position> path, boolean rotate) {
		animation = "run";
		restoreAnimation = mg.getAnimation();
		this.rotate = rotate;
		this.speed = Graphics.RUN_SPEED;
		activate(path, true);
	}

	public void doSlide(List<Position> path, float speed, boolean rotate) {
		animation = null;
		restoreAnimation = null;
		this.rotate = rotate;
		this.speed = speed;
		activate(path, false);
	}

	private void activate(List<Position> path, boolean walkAroundCharacters) {
		Spline spline0 = new Spline();
		nControlPoints = path.size();
		for (Position p : path) {
			spline0.addControlPoint(Graphics.positionToVector(p));
		}
		spline0.setType(Spline.SplineType.CatmullRom);
		spline0.setCurveTension(0.2f);

		if (walkAroundCharacters) {
			spline = adjustSplineForCharacters(path, spline0);
		} else {
			spline = spline0;
		}

		controlPoint = 0;
		t = 0;

		finalPosition = path.get(path.size() - 1);
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
				norm.normalizeLocal().multLocal(Graphics.scale * 0.25f);

				// adjust path to go around the other character
				r.addControlPoint(controlPoints.get(i).add(norm));
				norm.negateLocal();
				// and make the other character jump out of the way
				c.getSpatial().move(norm);
			}
		}

		r.setType(s.getType());
		r.setCurveTension(s.getCurveTension());

		return r;
	}

	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			if (animation != null) mg.setAnimation(animation);
		} else {
			if (restoreAnimation != null) mg.setAnimation(restoreAnimation);
		}
	}

	@Override protected void controlUpdate(float tpf) {
		if (!enabled) return;

		t += tpf * speed;
		while (t > 1) {
			controlPoint += 1;
			t -= 1;
		}

		if (controlPoint + 1 >= nControlPoints) {
			// we're at the end of the path
			// centre the characters on their squares
			mg.setPosition(finalPosition);

			if (charactersToRestore != null) {
				for (CharacterGraphics c : charactersToRestore) {
					c.setPosition(c.getPosition());
				}
			}

			// clean up and notify presentation layer that we're done
			charactersToRestore = null;
			endControl();
		} else {
			Vector3f p0 = spline.interpolate(t, controlPoint, null);
			spatial.setLocalTranslation(p0);

			// compute the heading
			int c1 = controlPoint;
			float t1 = t + (tpf * speed);
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
			if (rotate) {
				Quaternion r = mg.getUprightRotation().clone();
				Quaternion facing = new Quaternion();
				if (tangent.getX() > 0) {
					facing.fromAngleAxis(
						0 - Vector3f.UNIT_Y.angleBetween(tangent), Vector3f.UNIT_Y);
				} else {
					facing.fromAngleAxis(
						Vector3f.UNIT_Y.angleBetween(tangent), Vector3f.UNIT_Y);
				}
				r.multLocal(facing);
				spatial.setLocalRotation(r);
			}
		}
	}

	@Override protected void controlRender(RenderManager rm, ViewPort vp) {
		// do nothing
	}

	@Override public Control cloneForSpatial(Spatial spatial) {
		CharacterGraphics c = graphics.getCharacterByPosition(
			((SaveablePosition) spatial.getUserData("p")).getPosition());

		ObjectGraphics o = graphics.getObjectByPosition(
			((SaveablePosition) spatial.getUserData("p")).getPosition());

		ModelGraphics g = (c != null)? c : o;

		final PathController control = new PathController(graphics, g);
		control.setSpatial(spatial);
		return control;
	}
}

