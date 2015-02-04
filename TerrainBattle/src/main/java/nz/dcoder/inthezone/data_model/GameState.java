package nz.dcoder.inthezone.data_model;

import java.util.Collection;

/**
 * This object represents the entire state of the game.  It persists in the
 * overworld sections as well.
 * */
public class GameState {
	public Party party;
	public Terrain terrain;
	public boolean isInBattle = false;
	public Battle currentBattle = null;

	public GameState(Party party, Terrain terrain) {
		this.party = party;
		this.terrain = terrain;
	}

	public void makeBattle(
		Collection<Character> pcs,
		Collection<Character> nps,
		BattleController controller
	) {
		// TODO: implement this method
		return;
	}
}

