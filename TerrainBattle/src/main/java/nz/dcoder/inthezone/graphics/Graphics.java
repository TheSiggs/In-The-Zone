package nz.dcoder.inthezone.graphics;

import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Terrain;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages all scene graph transformations.  Other classes should not
 * manipulate the scene graph directly, but should instead call methods in this
 * class.
 *
 * Note:
 *   Two pieces of user data will be added to every Spatial in the scene graph:
 *     kind = "character" for character nodes, "board" for board tiles
 *     p = A SaveablePosition object that contains the position of the spatial
 *     in grid co-ordinates
 * */
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
	
	private final Collection<ObjectGraphics> objects =
		new ArrayList<ObjectGraphics>();

	public Graphics(SimpleApplication app, Terrain terrain) {
		this.rootNode = app.getRootNode();
		this.assetManager = app.getAssetManager();
		this.cam = app.getCamera();

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

	public static Vector3f positionToVector(Position p) {
		float bx = ((float) p.x) * Graphics.scale;
		float by = ((float) -p.y) * Graphics.scale;
		float bz = 0.2f * Graphics.scale;
		return new Vector3f(bx, by, bz);
	}

	/**
	 * Add a goblin character.  For testing purposes only.
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

		CharacterGraphics cg = new CharacterGraphics(this, spatial, p);
		characters.add(cg);

		return cg;
	}

	/**
	 * Add a character to the board.  This method loads the appropriate assets,
	 * creates a CharacterGraphics object, and registers everything
	 * appropriately.  See addGoblin for an example.
	 * */
	public CharacterGraphics addCharacter(Position p, CharacterName name) {
		// TODO: implement this method
		return null;
	}

	/**
	 * Add an object to the board, such as a body, a boulder, or a trip mine.
	 * */
	public ObjectGraphics addObject(Position p, BattleObjectName name) {
		// TODO: implement this method
		return null;
	}

	/**
	 * Kill a character.  Removes the character from the field and replaces it
	 * with a body.  Also invokes the death animation (so characters go out with
	 * a bang).
	 * */
	public ObjectGraphics killCharacter(
		CharacterGraphics cg, BattleObjectName body
	) {
		Position p = cg.getPosition();
		boardNode.detachChild(cg.getSpatial());
		characters.remove(cg);
		ObjectGraphics r = addObject(p, body);
		r.setAnimation("die");
		return r;
	}

	/**
	 * Remove an object from the battle (in dramatic fashion).
	 * */
	public void destroyObject(ObjectGraphics og) {
		objects.remove(og);
		og.setAnimation("destroy");
	}

	/**
	 * Get the character at a given position.
	 * @return null if there is no character at this position
	 * */
	public CharacterGraphics getCharacterByPosition(Position p) {
		return characters.stream()
			.filter(c -> c.getPosition().equals(p))
			.findFirst().orElse(null);
	}

	/**
	 * Get the object at a given position.
	 * @return null if there is no object at this position
	 * */
	public ObjectGraphics getObjectByPosition(Position p) {
		return objects.stream()
			.filter(o -> o.getPosition().equals(p))
			.findFirst().orElse(null);
	}

	/**
	 * Get the character under the mouse cursor
	 * */
	public CharacterGraphics getCharacterByMouse(Vector2f cursor) {
		CollisionResults rs = getMouseCollision(cursor);

		CharacterGraphics cg = null;
		float closestDistance = Float.MAX_VALUE;

		for (CollisionResult r : rs) {
			float thisDistance = r.getDistance();

			if (thisDistance < closestDistance) {
				Spatial spatial = r.getGeometry();

				// The collision detection only gives us leaf nodes, but for character
				// models we need the internal Node that represents the entire
				// character.  This while loop finds the required internal node by
				// walking up the tree until it finds something with a Position object.
				Node parent = null;
				Object op = spatial.getUserData("p");
				while (op == null && (parent = spatial.getParent()) != null) {
					spatial = parent;
					op = spatial.getUserData("p");
				}
				
				if (op != null) {
					Position p = ((SaveablePosition) op).getPosition();
					CharacterGraphics cg1 = getCharacterByPosition(p);
					if (cg1 != null) {
						cg = cg1;
						closestDistance = thisDistance;
					}
				}
			}
		}

		return cg;
	}

	/**
	 * Get the grid position at the mouse cursor
	 * */
	public Position getBoardByMouse(Vector2f cursor) {
		CollisionResults rs = getMouseCollision(cursor);

		for (CollisionResult r : rs) {
				Spatial spatial = r.getGeometry();

				Node parent = null;
				Object okind = spatial.getUserData("kind");
				while (
					(okind == null || !okind.equals("board")) &&
					(parent = spatial.getParent()) != null
				) {
					spatial = parent;
					okind = spatial.getUserData("kind");
				}

				Object op = spatial.getUserData("p");
				if (op != null) {
					return ((SaveablePosition) op).getPosition();
				}
		}

		return null;
	}

	private CollisionResults getMouseCollision(Vector2f cursor) {
		Vector3f click3d = cam.getWorldCoordinates(
				new Vector2f(cursor.x, cursor.y), 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(
				new Vector2f(cursor.x, cursor.y), 1f).subtractLocal(click3d).normalizeLocal();
		Ray ray = new Ray(click3d, dir);

		CollisionResults results = new CollisionResults();
		boardNode.collideWith(ray, results);
		return results;
	}
}

