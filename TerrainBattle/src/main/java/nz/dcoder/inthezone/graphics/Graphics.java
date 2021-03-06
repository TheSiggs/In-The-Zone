package nz.dcoder.inthezone.graphics;

import nz.dcoder.inthezone.control.Rotating;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.BattleObjectInfo;
import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Terrain;

import com.jme3.animation.Animation;
import com.jme3.animation.SpatialTrack;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages all scene graph transformations.  Other packages should not
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

	public static final float RUN_SPEED = 4f;
	public static final float WALK_SPEED = 1f;

	private final AssetManager assetManager;
	private final Camera cam;
	private final Node boardNode;
	private final Node rootNode;
	private final Node sceneNode = new Node();

	public final SpinController boardSpinner = new SpinController(1.5f, Vector3f.UNIT_Z);

	private final Collection<CharacterGraphics> characters =
		new ArrayList<CharacterGraphics>();
	
	private final Collection<ObjectGraphics> objects =
		new ArrayList<ObjectGraphics>();
	
	private final BoardGraphics boardGraphics;
	
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
		sceneNode.addControl(boardSpinner);

		initMaterials();

		initLight();

		initSelectedIndicator(solidGreen);

		boardGraphics = new BoardGraphics(terrain, assetManager);
		boardNode = boardGraphics.getBoardNode();
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

	Geometry selectedIndicator = null;
	private void initSelectedIndicator(Material mat) {
		Mesh m = new Mesh();
		Vector3f[] vertices = new Vector3f[5];
		vertices[0] = new Vector3f( 0.0f, 0.0f,  0.0f);
		vertices[1] = new Vector3f(-0.5f, 1.0f,  0.5f);
		vertices[2] = new Vector3f( 0.5f, 1.0f,  0.5f);
		vertices[3] = new Vector3f( 0.5f, 1.0f, -0.5f);
		vertices[4] = new Vector3f(-0.5f, 1.0f, -0.5f);
		int[] indices = {0,1,2, 0,2,3, 0,3,4, 0,4,1, 1,4,3, 3,2,1};
		m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		m.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
		m.updateBound();

		selectedIndicator = new Geometry("selected", m);
		selectedIndicator.setMaterial(mat);

		SpinController spinner = new SpinController(3.0f, Vector3f.UNIT_Y, Rotating.LEFT);
		spinner.setEnabled(false);
		selectedIndicator.addControl(spinner);
	}

	public Material solidGreen = null;
	public Material solidRed = null;

	private void initMaterials() {
		solidGreen = new Material(assetManager, 
			"Common/MatDefs/Misc/Unshaded.j3md");
		solidGreen.setColor("Color", ColorRGBA.Green);

		solidRed = new Material(assetManager, 
			"Common/MatDefs/Misc/Unshaded.j3md");
		solidRed.setColor("Color", ColorRGBA.Red);
	}


	public void deselectAllCharacters() {
		Node p = selectedIndicator.getParent();
		if (p != null) p.detachChild(selectedIndicator);
		selectedIndicator.getControl(SpinController.class).setEnabled(false);
	}

	/**
	 * Get the BoardGraphics, which manages highlighting etc.
	 * */
	public BoardGraphics getBoardGraphics() {
		return boardGraphics;
	}

	private ControllerChain controllerChain;

	/**
	 * Not to be invoked outside of the graphics package.
	 * */
	ControllerChain getControllerChain() {
		return controllerChain;
	}

	public void setControllerChain(ControllerChain chain) {
		this.controllerChain = chain;
	}

	public static Vector3f positionToVector(Position p) {
		float bx = ((float) p.x) * Graphics.scale;
		float by = ((float) -p.y) * Graphics.scale;
		float bz = 0.2f * Graphics.scale;
		return new Vector3f(bx, by, bz);
	}

	private Spatial addGoblinSpatial(int i) {
		String texture;
		if (i <= 5) {
			texture = "belt/D.png";
		} else {
			texture = "green/D.png";
		}

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

		return spatial;
	}

	/**
	 * Add a goblin character.  For testing purposes only.
	 * @param i The goblin to create (1 - 10 where 1 - 5 are the player goblins
	 * and 6 - 10 are the enemy goblins)
	 * */
	public CharacterGraphics addGoblin(Position p, int i) {
		Spatial spatial = addGoblinSpatial(i);

		CharacterGraphics cg = new CharacterGraphics(this, spatial, p);
		characters.add(cg);

		return cg;
	}

	/**
	 * Add a dead goblin object.  For testing purposes only.
	 * */
	public ObjectGraphics addDeadGoblin(Position p, int i) {
		Spatial spatial = addGoblinSpatial(i);

		ObjectGraphics og = new ObjectGraphics(this, spatial, p);
		og.addAnim(goblinDieAnimation(p, true));
		objects.add(og);

		return og;
	}

	/**
	 * HACK.  Construct a "die" animation.  This is a really bad way of doing
	 * things.  I use it here only to get around the lack of a suitable animation
	 * in the goblin object.  This animation must be reconstructed every time the
	 * dead goblin moves.
	 *
	 * @param p The position of the goblin
	 * @param animate true to generate an animation, false to generate a static
	 * transformation
	 * */
	private Animation goblinDieAnimation(Position p, boolean animate) {
		Animation die = new Animation("die" + p.toString(), animate? 1f : 0f);

		int frames = animate? 2 : 1;
		float[] times = new float[frames];
		Vector3f[] translations = new Vector3f[frames];
		Quaternion[] rotations = new Quaternion[frames];
		Vector3f[] scales = new Vector3f[frames];

		int i = 0;

		if (animate) {
			times[i] = i;
			translations[i] = positionToVector(p).addLocal(0, -((scale / 2) + 0.2f), 0f);
			rotations[i] = new Quaternion().fromAngles(FastMath.HALF_PI, 0f, 0f);
			scales[i] = new Vector3f(0.5f, 0.5f, 0.5f);
			i++;
		}

		times[i] = i;
		translations[i] = positionToVector(p).addLocal(0, -((scale / 2) + 0.2f), -0.2f);
		rotations[i] = new Quaternion().fromAngles(FastMath.HALF_PI * 0.2f, 0f, 0f);
		scales[i] = new Vector3f(0.5f, 0.5f, 0.5f);

		die.addTrack(new SpatialTrack(times, translations, rotations, scales));

		return die;
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

	private static final BattleObjectName deadGoblin1 =
		new BattleObjectName("goblinBody1");

	private static final BattleObjectName deadGoblin2 =
		new BattleObjectName("goblinBody2");

	/**
	 * Add an object to the board, such as a body, a boulder, or a trip mine.
	 * */
	public ObjectGraphics addObject(Position p, BattleObjectName name) {
		// TODO: implement this method

		// this is a special case for testing purposes, not a general mechanism.
		if (name.equals(deadGoblin1)) {
			return addDeadGoblin(p, 1);
		} else if (name.equals(deadGoblin2)) {
			return addDeadGoblin(p, 6);
		}
		return null;
	}

	/**
	 * Animate a character walking along a path
	 * @param continuation May be null for simple walk actions
	 * */
	public void doRun(
		CharacterGraphics cg,
		List<Position> path,
		Runnable continuation
	) {
		controllerChain.queueAnimation(t -> cg.getPathController().doRun(path, true, t));
		if (continuation != null) controllerChain.queueContinuation(continuation);
	}

	/**
	 * Kill a character.  Removes the character from the field and replaces it
	 * with a body.  Also invokes the death animation (so characters go out with
	 * a bang).
	 * */
	public List<ObjectGraphics> killCharacters(List<BattleObjectInfo> bodies) {
		List<ObjectGraphics> rs = new ArrayList<>();

		for (BattleObjectInfo i: bodies) {
			rs.add(addObject(i.p, i.name));

			CharacterGraphics cg = getCharacterByPosition(i.p);
			boardNode.detachChild(cg.getSpatial());
			characters.remove(cg);
		}

		controllerChain.queueAnimation(t -> {
			rs.stream().forEach(r -> {
				if (r.getSpatial().getName().equals("goblin-ogremesh")) {
					r.setAnimation("die" + r.getPosition().toString(), 1, t);
				} else {
					r.setAnimation("die", 1, t);
				}
			});
		});

		return rs;
	}

	/**
	 * Remove an object from the battle (in dramatic fashion).
	 * */
	public void destroyObjects(List<Position> positions) {
		List<ObjectGraphics> ogs = new ArrayList<>();

		for (Position p : positions) {
			ObjectGraphics og = getObjectByPosition(p);
			ogs.add(og);
			objects.remove(og);
		}

		controllerChain.queueAnimation(t ->
			ogs.stream().forEach(og -> og.setAnimation("destroy", 1, t)));
	}

	/**
	 * Animate a character doing an ability.
	 * */
	public void doAbility(
		CharacterGraphics cg,
		AbilityName name,
		Runnable continuation
	) {
		// TODO: implement this method.  Keep in mind that any animations or custom
		// controllers must be queued up in the controllerChain.
		//
		// Like this for example:
		// controllerChain.queueAnimation(() -> cg.setAnimation("attack"));
		// if (continuation != null) controllerChain.queueContinuation(continuation);
	}

	/**
	 * Animate a push action
	 * */
	public void doPush(
		CharacterGraphics cg,
		ObjectGraphics og,
		Position target,
		Runnable continuation
	) {
		Position dp = target.sub(cg.getPosition());
		Position oldObjectPos = og.getPosition();
		Position objectTarget = oldObjectPos.add(dp);

		List<Position> characterPath = new ArrayList<>();
		List<Position> objectPath = new ArrayList<>();

		characterPath.add(cg.getPosition());
		characterPath.add(target);

		objectPath.add(og.getPosition());
		objectPath.add(objectTarget);

		controllerChain.queueAnimation(t -> {
			cg.getPathController().doWalk(characterPath, true, t);
			og.getPathController().doSlide(objectPath, WALK_SPEED, false, t);
		});

		// HACK: part of the workaround for no "die" animation in the goblin model
		// This should be removed as soon as possible.
		controllerChain.queueAnimation(t -> {
			og.replaceAnim("die" + oldObjectPos.toString(),
				goblinDieAnimation(objectTarget, false));
			og.setAnimation("die" + objectTarget.toString(), 1, t);
		});
		if (continuation != null) controllerChain.queueContinuation(continuation);
	}

	/**
	 * Animate a teleport
	 * */
	public void doTeleport(
		CharacterGraphics cg, Position target, Runnable continuation
	) {
		controllerChain.queueAnimation(t -> cg.setPosition(target));
		if (continuation != null) controllerChain.queueContinuation(continuation);
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
	 * @param precise Set to true if the mouse must be directly over the
	 * character, not just over the character's square.
	 * */
	public CharacterGraphics getCharacterByMouse(Vector2f cursor, boolean precise) {
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

				String kind = spatial.getUserData("kind");
				
				if (op != null && (!precise || kind.equals("character"))) {
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

	/**
	 * Get the target of an ability at the mouse cursor.
	 * */
	public Position getTargetByMouse(Vector2f cursor) {
		// TODO: rewrite this to be more efficient, and to handle object targets
		CharacterGraphics character = getCharacterByMouse(cursor, false);
		if (character != null) {
			return character.getPosition();
		} else {
			return getBoardByMouse(cursor);
		}
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

