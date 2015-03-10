package nz.dcoder.inthezone.graphics;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Points;

public class HealthBarGraphics {
	private final Node healthBar;
	private final Geometry hp;
	private final Geometry nohp;

	private static final float height = 0.1f;
	private static final float width = Graphics.scale * 0.6f;

	private static final float animDelay = 1f;
	private static final float hideDelay = 1f;

	public HealthBarGraphics() {
		healthBar = new Node("healthBar");
		Quad qhp = new Quad(width, height);
		Quad qnohp = new Quad(-width, height);
		hp = new Geometry("hp", qhp);
		nohp = new Geometry("nohp", qnohp);
		healthBar.attachChild(hp);
		healthBar.attachChild(nohp);
		healthBar.addControl(new Controller());
	}

	public Spatial getSpatial() {
		return healthBar;
	}

	private float health = 1f;         // the target value
	private float oldHealth = 1f;      // previous target value
	private float healthInter = 0f;    // 0 for oldHealth, 1 for health

	public void setHP(Points hp) {
		oldHealth = health;
		health = ((float) hp.total) / ((float) hp.max);
		healthInter = 0f;
	}

	public void hideHealth() {
		hideHealthGeom();
	}

	private float untilFade = 0f;

	public void hideHealthDelay() {
		untilFade = hideDelay;
	}

	private void interpolateHealth(float i) {
		float p = (health * i) + (oldHealth * (i - 1));
		hp.setLocalScale(p, 1.0f, 1.0f);
		nohp.setLocalScale(1.0f - p, 1.0f, 1.0f);
	}

	private void hideHealthGeom() {
		Node p = healthBar.getParent();
		if (p != null) p.detachChild(healthBar);
	}

	/**
	 * A controller to animate the health bar
	 * */
	private class Controller extends AbstractControl {
		@Override protected void controlUpdate(float tpf) {
			if (spatial != null) {
				if (healthInter < 1.0f) {
					healthInter += tpf / animDelay;
					if (healthInter > 1.0f) healthInter = 1.0f;
					interpolateHealth(healthInter);
				}
				if (untilFade > 0.0f) {
					untilFade -= tpf / hideDelay;
					if (untilFade <= 0.0f) {
						untilFade = 0.0f;
						hideHealthGeom();
					}
				}
			}
		}

		@Override public Control cloneForSpatial(Spatial spatial) {
			final Controller control = new Controller();
			control.setSpatial(spatial);
			return control;
		}

		@Override protected void controlRender(RenderManager rm, ViewPort vp) {
		}
	}
}

