/*
 * Main class that holds everthing together. The overarching game logic is here.
 * No unncessary bits should be here.
 */
package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import nz.dcoder.inthezone.data_model.BattleController;
import nz.dcoder.inthezone.data_model.factories.DatabaseException;
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.Party;
import nz.dcoder.inthezone.data_model.Terrain;
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
	private Presentation presentation = null;
	private UserInterface userInterface = null;
	private Graphics graphics = null;

	public Main() {
		super((AppState) null);
	}

	/**
	 * Standard main method to get things started
	 * @param args 
	 */
	public static void main(String args[]) {
		Main app = new Main();
		app.start();
	}

	/**
	 * Initialise the game states.
	 */
	@Override
	public void simpleInitApp() {
		Party party = new Party();
		Terrain terrain = new Terrain();
		gameState = new GameState(party, terrain);

		graphics = new Graphics(this, gameState.terrain);
		userInterface = new UserInterface(this, graphics);

		try {
			presentation = new Presentation(gameState, graphics, userInterface);
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
		presentation.simpleUpdate(tpf);
	}

	public void someMethod() {
		System.out.println("hello from javafx");
	}
}
