package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

	float scale = 0.8f;
	int width = 10;
	int height = 10;
	Node boardNode = new Node("board");
	ColorRGBA colors[] = {
		new ColorRGBA(0.3f, 0.3f, 0.3f, 0f),
		new ColorRGBA(0.9f, 0.9f, 0.9f, 0f)
	};
	Geometry player1, player2;

	public Main() {
		super((AppState) null);
	}

	public static void main(String[] args) {
		Main app = new Main();
		app.start();
	}

	private void makeGrid() {
		float x, y, z;
		z = 0;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				x = i * scale;
				y = -j * scale;
				String name = i +","+ j;
				addBox(name, new Vector3f(x, y, z), colors[j % 2 == 0 ? i % 2 : (i + 1) % 2]);
			}
		}
	}

	private void addBox(String name, Vector3f translation, ColorRGBA color) {
		Box b = new Box(scale / 2, scale / 2, 0.1f * scale);
		Geometry geom = new Geometry(name, b);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", color);
		Texture cube1Tex = assetManager.loadTexture(
				"Textures/random_grey_variations.png");
		mat.setTexture("ColorMap", cube1Tex);
		geom.setMaterial(mat);
		geom.setLocalTranslation(translation);
		boardNode.attachChild(geom);
	}

	@Override
	public void simpleInitApp() {
		makeGrid();
		initPlayers();
		initInput();
		boardNode.setLocalTranslation(-scale * width / 2 + scale / 2,
				scale * height / 2 - scale / 2, 0);
		rootNode.attachChild(boardNode);
		placePlayer(player1, 3, 9);
		placePlayer(player2, 3, 0);
		Quaternion quaternion = new Quaternion();
		quaternion.fromAngles(0f, 0f, FastMath.QUARTER_PI);
		rootNode.rotate(quaternion);
		//rootNode.rotate(FastMath.QUARTER_PI, 0f, 0f);
		System.out.println("quat = " + quaternion);
		Vector3f axis = new Vector3f(1f, -1f, 0).normalizeLocal();
		
		quaternion.fromAngleAxis(-FastMath.QUARTER_PI, axis);
		rootNode.rotate(quaternion);
	}

	@Override
	public void simpleUpdate(float tpf) {
		//TODO: add update code
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}

	void initPlayers() {
		Box b = new Box(scale / 2, scale / 2, 0.1f * scale);
		player1 = new Geometry("Box", b);

		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture cube1Tex = assetManager.loadTexture(
				"Textures/player1.jpg");
		mat.setTexture("ColorMap", cube1Tex);
		player1.setMaterial(mat);
		Box b2 = new Box(scale / 2, scale / 2, 0.1f * scale);
		player2 = new Geometry("Box", b2);

		Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture cube2Tex = assetManager.loadTexture(
				"Textures/player2.png");
		mat2.setTexture("ColorMap", cube2Tex);
		player2.setMaterial(mat2);
	}

	private void placePlayer(Geometry geom, int xDir, int yDir) {
		float x = xDir * scale;
		float y = -yDir * scale;
		float z = 0.2f * scale;
		Vector3f translation = new Vector3f(x, y, z);
		geom.setLocalTranslation(translation);
		boardNode.attachChild(geom);
	}

	private void initInput() {
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("LeftMouse", new MouseButtonTrigger(
				MouseInput.BUTTON_LEFT));
		// Add the names to the action listener.
		inputManager.addListener(actionListener, "Up", "Right", "Down", "Left",
				"LeftMouse");
		//inputManager.addListener(analogListener, "Left", "Right", "Rotate");

	}
	ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean isPressed, float tpf) {
			Vector3f pos = player1.getLocalTranslation();
			if (isPressed) {
				if (name.equals("Up")) {
					pos.addLocal(Vector3f.UNIT_Y.mult(scale));
					player1.setLocalTranslation(pos);
				}
				if (name.equals("Right")) {
					pos.addLocal(Vector3f.UNIT_X.mult(scale));
					player1.setLocalTranslation(pos);
				}
				if (name.equals("Down")) {
					pos.addLocal(Vector3f.UNIT_Y.mult(-scale));
					player1.setLocalTranslation(pos);
				}
				if (name.equals("Left")) {
					pos.addLocal(Vector3f.UNIT_X.mult(-scale));
					player1.setLocalTranslation(pos);
				}
				if (name.equals("LeftMouse")) {
					System.out.println("Left Mouse");
					Vector2f mousePos = inputManager.getCursorPosition();
					System.out.println("Mouse Pos: "+ mousePos);

					CollisionResults results = new CollisionResults();
					Vector2f click2d = inputManager.getCursorPosition();
					Vector3f click3d = cam.getWorldCoordinates(
							new Vector2f(click2d.x, click2d.y), 0f).clone();
					Vector3f dir = cam.getWorldCoordinates(
							new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
					Ray ray = new Ray(click3d, dir);
					boardNode.collideWith(ray, results);
					for (int i = 0; i < results.size(); i++) {
						// For each hit, we know distance, impact point, name of geometry.
						float dist = results.getCollision(i).getDistance();
						Vector3f pt = results.getCollision(i).getContactPoint();
						String hit = results.getCollision(i).getGeometry().getName();
						System.out.println("* Collision #" + i);
						System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
						String coords[] = hit.split(",");
						int iCoord, jCoord;
						iCoord = Integer.parseInt(coords[0]);
						jCoord = Integer.parseInt(coords[1]);
						placePlayer(player1, iCoord, jCoord);
						
					}
				}
			}
		}
	};
}
