package nz.dcoder.inthezone.graphics;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import nz.dcoder.inthezone.data_model.pure.Points;

public class HealthBarGraphics {
	private final Node healthBar;
	private final Geometry hp;
	private final Geometry nohp;

	public static final float height = 0.12f;
	public static final float width = Graphics.scale * 0.8f;

	private static final float animDelay = 1f;
	private static final float hideDelay = 1f;

	public HealthBarGraphics(Graphics graphics) {
		healthBar = new Node("healthBar");

		Vector3f[] vertices = new Vector3f[5];
		vertices[0] = new Vector3f( 0.0f,  0.0f,   0.0f);
		vertices[1] = new Vector3f( width, 0.0f,   0.0f);
		vertices[2] = new Vector3f( width, height, 0.0f);
		vertices[3] = new Vector3f( 0.0f,  height, 0.0f);

		int[] indicesHP = {0,1,2, 2,3,0, 0,3,2, 2,1,0};
		int[] indicesNoHP = {1,2,3, 3,0,1, 1,0,3, 3,2,1};
		
		Mesh qhp = new Mesh();
		qhp.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		qhp.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indicesHP));
		qhp.updateBound();

		Mesh qnohp = new Mesh();
		qnohp.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		qnohp.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indicesNoHP));
		qnohp.updateBound();

		hp = new Geometry("hp", qhp);
		nohp = new Geometry("nohp", qnohp);
		healthBar.attachChild(hp);
		healthBar.attachChild(nohp);
		healthBar.addControl(new Controller());

		hp.setMaterial(graphics.solidGreen);
		nohp.setMaterial(graphics.solidRed);

		hp.setLocalTranslation(-(width / 2), 0, 0);
		nohp.setLocalTranslation(-(width / 2), 0, 0);

		interpolateHealth(1.0f);

		BillboardControl billboard = new BillboardControl();
		healthBar.addControl(billboard);
	}

	public Spatial getSpatial() {
		return healthBar;
	}

	private float health = 1f;         // the target value
	private float oldHealth = 1f;      // previous target value
	private float healthInter = 1f;    // 0 for oldHealth, 1 for health
	private float untilFade = 0f;

	public void setHP(Points hp) {
		oldHealth = health;
		health = ((float) hp.total) / ((float) hp.max);
		healthInter = 0f;
	}

	public void hideHealth() {
		hideHealthGeom();
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
					if (healthInter >= 1.0f) {
						healthInter = 1.0f;
						untilFade = hideDelay;
					}
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

