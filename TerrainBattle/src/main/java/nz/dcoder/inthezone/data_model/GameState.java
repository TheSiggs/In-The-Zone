package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;

import nz.dcoder.inthezone.ai.AIPlayer;

/**
 * This object represents the entire state of the game.  It persists in the
 * overworld sections as well.
 * */
public class GameState {
	public Party party;
	public Terrain terrain;
	public boolean isInBattle = false;
	public Battle currentBattle = null;

	public AIPlayer aiPlayer = null;

	public GameState(Party party, Terrain terrain) {
		this.party = party;
		this.terrain = terrain;
	}

	public void makeBattle(
		Collection<Character> pcs,
		Collection<Character> nps,
		BattleController controller,
		AIPlayer aiPlayer
	) {
		if (isInBattle) throw new RuntimeException(
			"Attempted to start a new battle, but we're already in a battle");

		this.aiPlayer = aiPlayer;

		currentBattle = new Battle(
			controller,
			pcs,
			nps,
			new ArrayList<BattleObject>(),   // no battle objects to start with
			terrain,
			true                             // human player goes first
		);
		isInBattle = true;
	}
}

