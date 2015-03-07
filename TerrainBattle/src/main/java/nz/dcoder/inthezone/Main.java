/*
 * Main class that holds everthing together. The overarching game logic is here.
 * No unncessary bits should be here.
 */
package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import java.util.ArrayList;
import java.util.List;

import nz.dcoder.inthezone.ai.AIPlayer;
import nz.dcoder.inthezone.control.Control;
import nz.dcoder.inthezone.control.GameActionListener;
import nz.dcoder.inthezone.control.GameDriver;
import nz.dcoder.inthezone.control.UserInterface;
import nz.dcoder.inthezone.data_model.Character;
import nz.dcoder.inthezone.data_model.Equipment;
import nz.dcoder.inthezone.data_model.factories.AbilityFactory;
import nz.dcoder.inthezone.data_model.factories.BattleObjectFactory;
import nz.dcoder.inthezone.data_model.factories.CharacterFactory;
import nz.dcoder.inthezone.data_model.factories.DatabaseException;
import nz.dcoder.inthezone.data_model.factories.EquipmentFactory;
import nz.dcoder.inthezone.data_model.factories.ItemFactory;
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.ItemBag;
import nz.dcoder.inthezone.data_model.Party;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.EquipmentName;
import nz.dcoder.inthezone.data_model.pure.ItemName;
import nz.dcoder.inthezone.data_model.pure.Position;
import nz.dcoder.inthezone.data_model.Terrain;
import nz.dcoder.inthezone.graphics.CharacterGraphics;
import nz.dcoder.inthezone.graphics.ControllerChain;
import nz.dcoder.inthezone.graphics.Graphics;

/**
 * Please change sparingly and only to attach new objects dependencies which are
 * necessary for the game to work. Nothing should be done in this class except
 * glue the game together and create objects and their factories.
 * 
 * @author tim@heuer.nz
 */
public class Main extends SimpleApplication {
	private GameState gameState = null;
	private Control control = null;
	private UserInterface userInterface = null;
	private Graphics graphics = null;
	private GameDriver driver = null;

	private AbilityFactory abilityFactory = null;
	private BattleObjectFactory battleObjectFactory = null;
	private CharacterFactory characterFactory = null;
	private EquipmentFactory equipmentFactory = null;
	private ItemFactory itemFactory = null;

	public Main() {
		super((AppState) null);
	}

	/**
	 * Standard main method to get things started
	 * @param args 
	 */
	public static void main(String args[]) {
		if (args.length > 1) {
			BlenderRender.main(args);
		} else {
			Main app = new Main();
			app.start();
		}
	}

	/**
	 * Initialise the game states.
	 */
	@Override
	public void simpleInitApp() {
		Party party = new Party();
		Terrain terrain = new Terrain();
		gameState = new GameState(party, terrain);

		try {
			abilityFactory = new AbilityFactory();
			battleObjectFactory = new BattleObjectFactory(abilityFactory);
			characterFactory = new CharacterFactory(
				abilityFactory, battleObjectFactory);
			equipmentFactory = new EquipmentFactory(abilityFactory);
			itemFactory = new ItemFactory(abilityFactory);

			graphics = new Graphics(this, gameState.terrain);
			userInterface = new UserInterface(this, gameState, graphics);
			driver = userInterface.getGameDriver();

			GameActionListener input = userInterface.getGameActionListener();
			graphics.setControllerChain(new ControllerChain(
				input::notifyAnimationStart,
				input::notifyAnimationEnd));

			control = new Control(gameState, graphics, userInterface);

			startSimpleBattle();
		} catch (DatabaseException e) {
			System.err.println("Error reading data files: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Pass responsibility down to presentation.
	 * @param tpf time per frame
	 */
	@Override
	public void simpleUpdate(float tpf) {
		control.simpleUpdate(tpf);
	}

	void startSimpleBattle() {
		// create the characters for this battle (ignore the Party class for now)

		List<Character> pcs = new ArrayList<Character>();
		List<Character> npcs = new ArrayList<Character>();

		Position headingN = new Position(0, -1);
		Position headingS = new Position(0, 1);

		for (int x = 0; x < 5; ++x) {
			pcs.add(initGoblin(new Position(x * 2, 9), x + 1, headingN));
		}

		for (int x = 0; x < 5; ++x) {
			npcs.add(initGoblin(new Position(x * 2, 0), x + 6, headingS));
		}

		// create some items and add them to the party
		ItemName potion = new ItemName("potion");
		ItemName grenade = new ItemName("grenade");

		ItemBag playerItems = gameState.party.getItemBag();
		ItemBag aiItems = new ItemBag();

		for (int i = 0; i < 3; i++) {
			playerItems.gainItem(itemFactory.newItem(potion));
		}

		for (int i = 0; i < 2; i++) {
			playerItems.gainItem(itemFactory.newItem(grenade));
		}

		gameState.makeBattle(pcs, npcs,
			playerItems, aiItems,
			control.getBattleController(), new AIPlayer());
	}

	Character initGoblin(Position p, int i, Position dp) {
		CharacterGraphics cg = graphics.addGoblin(p, i);
		cg.setHeading(dp);

		CharacterName name = new CharacterName("goblin " + i);
		Character r = characterFactory.newCharacter(name, 1);
		Equipment weapon = equipmentFactory.newEquipment(
			new EquipmentName("simple weapon"));
		r.equipWeapon(weapon);

		if (r == null) {
			throw new RuntimeException(
				"Could not create character " + name.toString());
		}

		r.position = p;

		return r;
	}
}

