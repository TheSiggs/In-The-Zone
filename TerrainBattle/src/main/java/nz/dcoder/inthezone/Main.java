package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
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
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3x.jfx.JmeFxContainer;
import com.jme3x.jfx.JmeFxScreenContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javax.vecmath.Point2i;
import nz.dcoder.ai.astar.AStarSearch;
import nz.dcoder.ai.astar.BoardNode;
import nz.dcoder.ai.astar.BoardState;
import nz.dcoder.ai.astar.Tile;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

	float scale = 0.8f;
	int width = 20;
	int height = 20;
	long flags = 0;
	static final long LEFT_ROTATE = 0b1;
	static final long RIGHT_ROTATE = 0b10;
	static final long PLAYER_MOVING = 0b100;
	Node boardNode = new Node("board");
	ColorRGBA colors[] = {
		new ColorRGBA(0.3f, 0.3f, 0.3f, 0f),
		new ColorRGBA(0.9f, 0.9f, 0.9f, 0f)
	};
	Spatial player1;
	BoardState boardState;
	SortedSet<Tile> boardTiles;
	private List<nz.dcoder.ai.astar.Node> path;
	private int pathNode;
	private Node sceneNode;
	//private List<Character> players = new ArrayList<>();
	private List<Character> team1 = new ArrayList<>();
	private List<Character> team2 = new ArrayList<>();

	public Main() {
		super((AppState) null);
		boardState = new BoardState().load("board.map");
		width = boardState.getWidth();
		height = boardState.getHeight();
		boardTiles = new TreeSet<>();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (boardState.get(x, y) == 0) {
					boardTiles.add(new Tile(x, y));
				}
			}
		}
		BoardNode.tiles = boardTiles;
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
				String name = i + "," + j;
				addBox(name, new Vector3f(x, y, z),
					colors[j % 2 == 0 ? i % 2 : (i + 1) % 2],
					boardState.get(i, j));
			}
		}
	}

	private void addBox(String name, Vector3f translation, ColorRGBA color, int boardValue) {
		float offsetZ = boardValue * 0.5f;
		Box b = new Box(scale / 2, scale / 2, 0.1f * scale + offsetZ);
		Geometry geom = new Geometry(name, b);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		ColorRGBA real = color.clone();
		if (boardValue != 0) {
			real.r = 1f;
		}
		mat.setColor("Color", real);
		Texture cube1Tex = assetManager.loadTexture(
			"Textures/random_grey_variations.png");
		mat.setTexture("ColorMap", cube1Tex);
		geom.setMaterial(mat);
		//geom.setLocalTranslation(translation.add(Vector3f.UNIT_Z.multLocal(1f * boardValue)));
		geom.setLocalTranslation(translation);
		boardNode.attachChild(geom);
	}

	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets", FileLocator.class);
		//assetManager.registerLocator("assets/town.zip", ZipLocator.class);
		Spatial scene = assetManager.loadModel("Scenes/sample.j3o");
		scene.rotate(FastMath.HALF_PI, 0f, 0f);
		sceneNode = new Node();
		Vector3f trans = Vector3f.UNIT_Y.clone();
		trans.multLocal(1.5f);
		sceneNode.setLocalTranslation(trans);
		rootNode.attachChild(sceneNode);
		sceneNode.attachChild(scene);
		initLight();
		makeGrid();
		initPlayers();
		initInput();
		initGui();
		boardNode.setLocalTranslation(-scale * width / 2 + scale / 2,
			scale * height / 2 - scale / 2, 0);
		sceneNode.attachChild(boardNode);
		//placePlayer(player1, 3, 9);
		Quaternion quaternion = new Quaternion();
		quaternion.fromAngles(0f, 0f, FastMath.QUARTER_PI);
		sceneNode.rotate(quaternion);
		//rootNode.rotate(FastMath.QUARTER_PI, 0f, 0f);
		Vector3f myAxis = new Vector3f(1f, -1f, 0).normalizeLocal();

		quaternion.fromAngleAxis(-FastMath.QUARTER_PI, myAxis);
		sceneNode.rotate(quaternion);
		cam.setFrustumFar(80f);
	}
	Quaternion quat = new Quaternion();
	Vector3f axis = Vector3f.UNIT_Z;
	float currentX = -1;
	float currentY = -1;
	float rotationSpeed = 1.5f;
	float travelSpeed = 4f;
	float percentAlong = 0f;

	@Override
	public void simpleUpdate(float tpf) {
		if ((flags & LEFT_ROTATE) != 0) {
			quat.fromAngleAxis(tpf * rotationSpeed, axis);
			sceneNode.rotate(quat);
		}
		if ((flags & RIGHT_ROTATE) != 0) {
			quat.fromAngleAxis(-tpf * rotationSpeed, axis);
			sceneNode.rotate(quat);
		}
		if ((flags & PLAYER_MOVING) != 0) {
			setPlayerLocation(player1, beginX, beginY, targetX, targetY, percentAlong);
			percentAlong += tpf * travelSpeed;
			if (percentAlong > 1f) {
				percentAlong = 0f;
			}
		} else {
			percentAlong = 0f;
		}
		fixFacing();
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}

	private Character makeCharacter(int x, int y, String texture) {
		Spatial mySpatial = assetManager.loadModel("3d_objects/creatures/goblin/goblin.mesh.xml");
		// 3d_objects/creatures/goblin/textures/green/ogre.material
		Material mat = new Material(
			assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setTexture("ColorMap",
			assetManager.loadTexture("3d_objects/creatures/goblin/textures/" + texture));
		mySpatial.setMaterial(mat);
		mySpatial.scale(0.5f);
		Character character = new Character(mySpatial);
		character.setX(x);
		character.setY(y);
		placePlayer(character.getSpatial(), x, y);
		return character;
	}

	void initPlayers() {
		//Box b = new Box(scale / 2, scale / 2, 0.1f * scale);
		//player1 = new Geometry("Box", b);
		//player1 = assetManager.loadModel("Models/black-canary/black canary hero185.j3o");
		//Quaternion playerRotation = new Quaternion();
		//playerRotation.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
		//player1.setLocalRotation(playerRotation);
		//player1.scale(0.25f);
		for (int x = 0; x < 5; ++x) {
			team1.add(makeCharacter(x * 2, 9, "belt/D.png"));
		}
		player1 = team1.get(0).getSpatial();
		for (int x = 0; x < 5; ++x) {
			team2.add(makeCharacter(x * 2, 0, "green/D.png"));
		}

		//Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		//Texture cube1Tex = assetManager.loadTexture(
		//		"Textures/player1.jpg");
		//mat.setTexture("ColorMap", cube1Tex);
		//player1.setMaterial(mat);
	}
	int lastX = -1;
	int lastY = -1;

	private void setPlayerLocation(Spatial geom, int startX, int startY, int endX, int endY, float along) {
		if (along == 0f) {
			//along = 1f;
			int size = path.size();
			BoardNode bn;
			if (pathNode < size) {
				bn = (BoardNode) path.get(pathNode);
				startX = beginX = bn.getX();
				startY = beginY = bn.getY();
				pathNode++;
			}
			if (pathNode < size) {
				bn = (BoardNode) path.get(pathNode);
				endX = targetX = bn.getX();
				endY = targetY = bn.getY();
			} else {
				flags &= ~PLAYER_MOVING;
				findCurrentPlayer().setAnimation("idleA");
				percentAlong = 0f;
			}
			//percentAlong = 0f;
		}
		float addX = (endX - startX) * along;
		float addY = (endY - startY) * along;
		float x = ((float) startX + addX) * scale;
		float y = -((float) startY + addY) * scale;
		float z = 0.2f * scale;
		Vector3f translation = new Vector3f(x, y, z);
		geom.setLocalTranslation(translation);
		boardNode.attachChild(geom);
	}

	private void setPlayerLocation(Spatial geom, int xDir, int yDir) {
		float x = xDir * scale;
		float y = -yDir * scale;
		float z = 0.2f * scale;
		Vector3f translation = new Vector3f(x, y, z);
		geom.setLocalTranslation(translation);
		boardNode.attachChild(geom);
	}

	/**
	 * Place player on field with coordinates
	 *
	 * @param geom The player geometry
	 * @param xDir x coordinate
	 * @param yDir y coordinate
	 */
	private void placePlayer(Spatial geom, int xDir, int yDir) {
		if (lastX == -1) {
			setPlayerLocation(geom, xDir, yDir);
		} else {
			findPathAndWalkTo(geom, lastX, lastY, xDir, yDir);
			lastX = xDir;
			lastY = yDir;
		}
	}
	int beginX = -1;
	int beginY = -1;
	int targetX = -1;
	int targetY = -1;

	private void findPathAndWalkTo(Spatial geom, int fromX, int fromY, int toX, int toY) {
		int maxX = boardState.getWidth() - 1;
		int maxY = boardState.getHeight() - 1;
		if (fromX < 0 || fromX > maxX
			|| toX < 0 || toX > maxX
			|| fromY < 0 || fromY > maxY
			|| toY < 0 || toY > maxY
			|| boardState.get(fromX, fromY) != 0
			|| boardState.get(toX, toY) != 0
			|| (percentAlong > 0f && percentAlong < 1f)) {
			return;
		}
		BoardNode start = new BoardNode(fromX, fromY, null);
		BoardNode goal = new BoardNode(toX, toY, null);
		AStarSearch search = new AStarSearch(start, goal);
		path = search.search();
		System.out.println(path);
		pathNode = 0;
		flags |= PLAYER_MOVING;
		percentAlong = 0f;
		updateCurrentCharacterLocation(toX, toY);
		setWalkingAnimation();
	}

	private void walkTo(Spatial geom, int fromX, int fromY, int toX, int toY) {
		beginX = fromX;
		beginY = fromY;
		targetX = toX;
		targetY = toY;
		flags |= PLAYER_MOVING;
	}

	private void initInput() {
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("A", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("D", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("C", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addMapping("LeftMouse", new MouseButtonTrigger(
			MouseInput.BUTTON_LEFT));
		// Add the names to the action listener.
		inputManager.addListener(actionListener, "Up", "Right", "Down", "Left",
			"LeftMouse", "A", "C", "D");
		//inputManager.addListener(analogListener, "Left", "Right", "Rotate");

	}
	ActionListener actionListener = new ActionListener() {
		@Override
		public void onAction(String name, boolean isPressed, float tpf) {
			Vector3f pos = player1.getLocalTranslation();
			if (isPressed) {
				if (name.equals("A")) {
					flags |= LEFT_ROTATE;
				}
				if (name.equals("D")) {
					flags |= RIGHT_ROTATE;
				}
				int x = (int) (pos.x / scale);
				int y = (int) (-pos.y / scale);
				if (name.equals("Up")) {
					findPathAndWalkTo(player1, x, y, x, y - 1);
				}
				if (name.equals("Right")) {
					findPathAndWalkTo(player1, x, y, x + 1, y);
				}
				if (name.equals("Down")) {
					findPathAndWalkTo(player1, x, y, x, y + 1);
				}
				if (name.equals("Left")) {
					findPathAndWalkTo(player1, x, y, x - 1, y);
				}
				CollisionResults results;
				if (name.equals("C")) {
					if ((flags & PLAYER_MOVING) != 0) {
						return;
					}
					results = new CollisionResults();
					boardNode.collideWith(getCollisionRay(), results);
					Point2i point = getCollidingBoardTile(results);
					selectPlayerAt(point);
				}
				if (name.equals("LeftMouse")) {
					results = new CollisionResults();
					boardNode.collideWith(getCollisionRay(), results);
					Point2i point = getCollidingBoardTile(results);
					findPathAndWalkTo(player1, x, y, point.x, point.y);
					highlightRoute(point.x, point.y);
				}
			} else {
				if (name.equals("A")) {
					flags &= ~LEFT_ROTATE;
				}
				if (name.equals("D")) {
					flags &= ~RIGHT_ROTATE;
				}
			}
		}

		private void highlightRoute(int iCoord, int jCoord) {
			int lx = lastX;
			int ly = lastY;

		}

		private void selectPlayerAt(Point2i point) {
			for (Character c : team1) {
				if (c.getX() == point.x && c.getY() == point.y) {
					player1 = c.getSpatial();
					break;
				}
			}
			for (Character c : team2) {
				if (c.getX() == point.x && c.getY() == point.y) {
					player1 = c.getSpatial();
					break;
				}
			}
		}
	};

	private void initLight() {
		/*
		 PointLight pl = new PointLight();
		 pl.setPosition(getCamera().getLocation());
		 pl.setRadius(200f);
		 rootNode.addLight(pl);
		 */
		DirectionalLight southLight = new DirectionalLight();
		southLight.setDirection(new Vector3f(0f, -1f, -1f).normalizeLocal());
		rootNode.addLight(southLight);
	}

	private void fixFacing() {
		Quaternion facing = new Quaternion();
		Vector3f myAxis = Vector3f.UNIT_Y;
		float mult = 0f;
		if (beginX > targetX) {
			mult = 1f;
		}
		if (beginX < targetX) {
			mult = -1f;
		}
		if (beginY < targetY) {
			mult = 2f;
		}
		facing.fromAngleAxis(mult * FastMath.HALF_PI, myAxis);
		Quaternion upright = new Quaternion();
		Quaternion front = new Quaternion();
		front.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
		upright.fromAngles(FastMath.HALF_PI, 0f, 0f);
		upright.multLocal(front);
		for (Character c : team1) {
			c.getSpatial().setLocalRotation(upright);
		}
		Quaternion opposite = upright.clone();
		Quaternion turnedAround = new Quaternion();
		turnedAround.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
		opposite.multLocal(turnedAround);
		for (Character c : team2) {
			c.getSpatial().setLocalRotation(opposite);
		}
		upright.multLocal(facing);
		player1.setLocalRotation(upright);
	}

	private Ray getCollisionRay() {
		System.out.println("Left Mouse");
		Vector2f mousePos = inputManager.getCursorPosition();
		System.out.println("Mouse Pos: " + mousePos);

		Vector2f click2d = inputManager.getCursorPosition();
		Vector3f click3d = cam.getWorldCoordinates(
			new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(
			new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
		Ray ray = new Ray(click3d, dir);
		return ray;
	}

	private Point2i getCollidingBoardTile(CollisionResults results) {
		int iCoord = -1, jCoord = -1;
		for (int i = 0; i < results.size(); i++) {
			// For each hit, we know distance, impact point, name of geometry.
			float dist = results.getCollision(i).getDistance();
			Vector3f pt = results.getCollision(i).getContactPoint();
			String hit = results.getCollision(i).getGeometry().getName();
			System.out.println("* Collision #" + i);
			System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
			String coords[] = hit.split(",");
			if (coords.length > 1) {
				iCoord = Integer.parseInt(coords[0]);
				jCoord = Integer.parseInt(coords[1]);
				if (boardState.get(iCoord, jCoord) == 0) {
					break;
				}
			}
		}
		return new Point2i(iCoord, jCoord);
	}

	private void updateCurrentCharacterLocation(int toX, int toY) {
		Character current = findCurrentPlayer();
		current.setX(toX);
		current.setY(toY);
	}

	private Character findCurrentPlayer() {
		for (Character c : team1) {
			if (c.getSpatial() == player1) {
				return c;
			}
		}
		for (Character c : team2) {
			if (c.getSpatial() == player1) {
				return c;
			}
		}
		return null;
	}

	private void setWalkingAnimation() {
		Character currentPlayer = findCurrentPlayer();
		currentPlayer.setAnimation("run");
	}

	private void initGui() {
		/*
		final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, true, new ProtonCursorProvider(this, this.assetManager, this.inputManager));
		this.inputManager.addRawInputListener(testguiManager.getInputRedirector());
		String path = "nz/dcoder/inthezone/FXMLDocument.fxml";
		final FXMLHud testhud = new FXMLHud(path);
		testhud.precache();
		testguiManager.attachHudAsync(testhud);
		final FXMLWindow testwindow = new FXMLWindow(path);
		testwindow.setExternalisable(true);
		testwindow.setExternalized(true);
		testwindow.precache();
		testwindow.setTitleAsync("TestTitle");
		testguiManager.attachHudAsync(testwindow);
		 */
		JmeFxScreenContainer jmefx = JmeFxContainer.install(this, getGuiNode(), true, null);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				jmefx.setScene(createScene(), root);
			}
		});

		this.guiNode.attachChild(jmefx.getJmeNode());
	}

    private static Group root;
	public static Scene createScene() {

		root = new Group();

		Scene scene = new Scene(root, 600, 600, true);
		
		scene.setFill(new Color(0, 0, 0, 0));

		final TreeItem<String> treeRoot = new TreeItem<String>("Root node");
		treeRoot.getChildren().addAll(Arrays.asList(new TreeItem<String>("Child Node 1"), new TreeItem<String>("Child Node 2"), new TreeItem<String>("Child Node 3")));
		treeRoot.getChildren().get(2).getChildren().addAll(Arrays.asList(new TreeItem<String>("Child Node 4"), new TreeItem<String>("Child Node 5")));

		final TreeView treeView = new TreeView();
		treeView.setShowRoot(true);

		treeView.setRoot(treeRoot);

		treeRoot.setExpanded(true);
		treeView.setLayoutY(100);

		Button test1 = new Button("Test1");
		test1.setLayoutX(500);
		test1.setLayoutY(500);
		test1.setOnAction(event -> {

		});

		CheckBox test2 = new CheckBox("Test2");
		test2.setLayoutX(700);
		test2.setLayoutY(700);

		MenuBar bar = new MenuBar();

		Menu testMenu = new Menu("Test");
		bar.getMenus().add(testMenu);
		MenuItem i1 = new MenuItem("Entry1");
		MenuItem i2 = new MenuItem("Entry2");
		Menu sub = new Menu("Submenu");
		sub.getItems().addAll(new MenuItem("Sub entry 1"), new MenuItem("Sub Entry 2"));
		testMenu.getItems().addAll(i1, sub, i2);

		TextArea ta = new TextArea();
		ta.setOpacity(0.4);
		ta.setLayoutX(400);
		ta.setLayoutY(300);

		ChoiceBox cb = new ChoiceBox();

		cb.setItems(FXCollections.observableArrayList("Alfa", "Beta"));
		cb.setLayoutX(300);
		cb.setLayoutY(200);

		ComboBox<String> comboBox = new ComboBox<String>();
		comboBox.getItems().addAll("11111", "22222", "3333", "44444", "55555", "6666");
		comboBox.setLayoutX(350);
		comboBox.setLayoutY(250);

		root.getChildren().addAll(treeView, test1, bar, test2, ta, cb, comboBox);

		return scene;
	}
}
