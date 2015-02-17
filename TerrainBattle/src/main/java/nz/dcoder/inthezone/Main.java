/*
 * Main class that holds everthing together. The overarching game logic is here.
 * No unncessary bits should be here.
 */
package nz.dcoder.inthezone;

import com.jme3.app.SimpleApplication;
import nz.dcoder.inthezone.data_model.BattleController;
import nz.dcoder.inthezone.data_model.GameState;
import nz.dcoder.inthezone.data_model.Party;
import nz.dcoder.inthezone.data_model.Terrain;

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
	private BattleController battleController = null;

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
		battleController = new BattleController();

		presentation = new Presentation(this);
		userInterface = new UserInterface(this);
	}

	/**
	 * Pass responsibility down to presentation.
	 * @param tpf time per frame
	 */
	@Override
	public void simpleUpdate(float tpf) {
		getPresentation().simpleUpdate(tpf);
	}

	/**
	 * @return the gameState
	 */
	public GameState getGameState() {
		return gameState;
	}

	/**
	 * @return the presentation
	 */
	public Presentation getPresentation() {
		return presentation;
	}

	/**
	 * @return the userInterface
	 */
	public UserInterface getUserInterface() {
		return userInterface;
	}

	/**
	 * @return the battleController
	 */
	public BattleController getBattleController() {
		return battleController;
	}
}
