package nz.dcoder.inthezone.graphics;

import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Terrain;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Collection;

public class Graphics {
	public static final float scale = 0.8f;
	public static final float rotationSpeed = 1.5f;
	public static final float travelSpeed = 4f;

	private final AssetManager assetManager;
	private final Camera cam;
	private final Node boardNode;
	private final Node rootNode;
	private final Node sceneNode = new Node();

	private final Collection<CharacterGraphics> characters =
		new ArrayList<CharacterGraphics>();

	public Graphics(SimpleApplication app, Terrain terrain) {
		this.rootNode = app.getRootNode();
		this.assetManager = app.getAssetManager();
		this.cam = app.getCamera();

		app.getFlyByCamera().setEnabled(false);

		assetManager.registerLocator("assets", FileLocator.class);

		Spatial scene = assetManager.loadModel("Scenes/sample.j3o");
		scene.rotate(FastMath.HALF_PI, 0f, 0f);
		sceneNode.attachChild(scene);

		Vector3f trans = Vector3f.UNIT_Y.clone();
		trans.multLocal(1.5f);
		sceneNode.setLocalTranslation(trans);

		rootNode.attachChild(sceneNode);

		initLight();

		boardNode = new BoardGraphics(terrain, assetManager).getBoardNode();
		int width = terrain.getWidth();
		int height = terrain.getHeight();
		boardNode.setLocalTranslation(-scale * width / 2 + scale / 2,
				scale * height / 2 - scale / 2, 0);
		sceneNode.attachChild(boardNode);

		Quaternion quaternion = new Quaternion();
		quaternion.fromAngles(0f, 0f, FastMath.QUARTER_PI);
		sceneNode.rotate(quaternion);

		Vector3f myAxis = new Vector3f(1f, -1f, 0).normalizeLocal();

		quaternion.fromAngleAxis(-FastMath.QUARTER_PI, myAxis);
		sceneNode.rotate(quaternion);
		cam.setFrustumFar(80f);
	}

	private void initLight() {
		DirectionalLight southLight = new DirectionalLight();
		southLight.setDirection(new Vector3f(0f, -1f, -1f).normalizeLocal());
		rootNode.addLight(southLight);
	}

	Quaternion quat = new Quaternion();

	/**
	 * Rotate the view about the z-axis
	 * */
	public void rotateView(float angle) {
		quat.fromAngleAxis(angle, Vector3f.UNIT_Z);
		sceneNode.rotate(quat);
	}

	/**
	 * Later, we will make a new method "addCharacter" that can add any
	 * character.  For now we just have this method that adds goblins.
	 * */
	public CharacterGraphics addGoblin(Position p, String texture) {
		Spatial spatial = assetManager.loadModel(
			"3d_objects/creatures/goblin/goblin.mesh.xml");

		// 3d_objects/creatures/goblin/textures/green/ogre.material
		Material mat = new Material(
			assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setTexture("ColorMap", assetManager.loadTexture(
			"3d_objects/creatures/goblin/textures/" + texture));
		spatial.setMaterial(mat);
		spatial.scale(0.5f);

		// need to do this before constructing the CharacterGraphics object
		boardNode.attachChild(spatial);

		CharacterGraphics cg = new CharacterGraphics(spatial, p);
		characters.add(cg);

		return cg;
	}

	/**
	 * Get the character at a given position
	 * */
	public CharacterGraphics getCharacterByPosition(Position p) {
		return characters.stream()
			.filter(c -> c.getPosition().equals(p))
			.findFirst().orElse(null);
	}
}

