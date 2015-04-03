package nz.dcoder.inthezone.graphics;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.BillboardControl;
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

	public static final float height = 0.12f;
	public static final float width = Graphics.scale * 0.8f;

	private static final float animDelay = 1f;
	private static final float hideDelay = 1f;
	private static final float fadeDelay = 0.5f;

	private ColorRGBA hpColor = ColorRGBA.Green.clone();
	private ColorRGBA nohpColor = ColorRGBA.Red.clone();

	public HealthBarGraphics(Graphics graphics) {
		healthBar = new Node("healthBar");
		healthBar.setQueueBucket(Bucket.Translucent);

		Material hpMat = graphics.solidGreen.clone();
		Material nohpMat = graphics.solidRed.clone();
		hpMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		nohpMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		hp = new Geometry("hp", new Quad(width, height));
		nohp = new Geometry("nohp", new Quad(width, height));
		hp.setMaterial(hpMat);
		nohp.setMaterial(nohpMat);

		healthBar.attachChild(nohp);
		healthBar.attachChild(hp);
		healthBar.addControl(new Controller());

		BillboardControl billboard = new BillboardControl();
		healthBar.addControl(billboard);

		hp.setLocalTranslation(-(width / 2), 0, 0);
		nohp.setLocalTranslation(-(width / 2), 0, 0);

		interpolateHealth(1.0f);
	}

	public Spatial getSpatial() {
		return healthBar;
	}

	private float health = 1f;         // the target value
	private float oldHealth = 1f;      // previous target value
	private float healthInter = 1f;    // 0 for oldHealth, 1 for health
	private float untilFade = 0f;
	private float untilVanish = 0f;

	public void setHP(Points hp) {
		oldHealth = health;
		health = ((float) hp.total) / ((float) hp.max);
		healthInter = 0f;
		untilFade = 0f;
		untilVanish = 0f;
	}

	/**
	 * Call before attaching the health bar spatial to the scene graph.
	 * */
	public void showHealth() {
		untilFade = 0;
		untilVanish = 0;
		interpolateFade(0.0f);
	}

	public void hideHealth() {
		if (!(healthInter < 1f || untilVanish > 0f || untilFade > 0f)) {
			untilVanish = fadeDelay;
		}
	}

	private void interpolateHealth(float i) {
		float p = (health * i) + (oldHealth * (1 - i));
		hp.setLocalScale(p, 1.0f, 1.0f);
		nohp.setLocalScale(1 - p, 1.0f, 1.0f);
		nohp.setLocalTranslation((p * width) - (width/2), 0.0f, 0.0f);
	}

	private void interpolateFade(float fade) {
		hpColor.a = 1 - fade;
		nohpColor.a = 1 - fade;
		hp.getMaterial().setColor("Color", hpColor);
		nohp.getMaterial().setColor("Color", nohpColor);
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
					if (healthInter >= 1.0f) {
						healthInter = 1.0f;
						untilFade = hideDelay;
					}
					interpolateHealth(healthInter);
				}
				if (untilFade > 0.0f) {
					untilFade -= tpf;
					if (untilFade <= 0.0f) {
						untilFade = 0.0f;
						untilVanish = fadeDelay;
						hideHealth();
					}
				}
				if (untilVanish > 0.0f) {
					untilVanish -= tpf;
					if (untilVanish <= 0.0f) {
						untilVanish = 0.0f;
						hideHealthGeom();
					}
					interpolateFade((fadeDelay - untilVanish) / fadeDelay);
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

