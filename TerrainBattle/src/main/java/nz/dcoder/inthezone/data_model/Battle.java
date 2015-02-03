package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Tracks all the parameters of a battle in progress
 * */
class Battle {
	public final BattleController controller;

	public Collection<Character> players;
	public Collection<Character> aiPlayers;
	public Collection<BattleObject> objects;

	Terrain terrain;
	Turn turn;

	private int turnNumber;

	public Battle(
		BattleController controller,
		Collection<Character> players,
		Collection<Character> aiPlayers,
		Collection<BattleObject> objects,
		Terrain terrain
	) {
		this.controller = controller;
		this.players = players;
		this.aiPlayers = aiPlayers;
		this.objects = objects;
		this.terrain = terrain;

		turnNumber = 0;
		changeTurn();
	}

	/**
	 * may return null
	 * */
	public Character getCharacterAt(Position pos) {
		// TODO: implement this method
		return null;
	}

	/**
	 * may return null
	 * */
	public BattleObject getObjectAt(Position pos) {
		// TODO: implement this method
		return null;
	}

	/**
	 * must not return null
	 * */
	public Collection<Position> getObstacles() {
		// TODO: implement this method
		return null;
	}

	public void grimReaper() {
		// TODO: implement this method
	}

	public void changeTurn() {
		turnNumber += 1;
		// TODO: construct a new Turn object
	}

	public void changeTurnIfRequired() {
		// TODO: implement this method
	}
}

