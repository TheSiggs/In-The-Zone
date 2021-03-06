package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Tracks all the parameters of a battle in progress
 * */
class Battle {
	public final BattleController controller;

	Collection<Character> players;
	Collection<Character> aiPlayers;
	Collection<BattleObject> objects;

	private final ItemBag playerItems;
	private final ItemBag aiItems;

	Terrain terrain;
	Turn turn;

	private int turnNumber;

	public Battle(
		BattleController controller,
		Collection<Character> players,
		Collection<Character> aiPlayers,
		ItemBag playerItems,
		ItemBag aiItems,
		Collection<BattleObject> objects,
		Terrain terrain,
		boolean playerGoesFirst  // true if the human player goes first
	) {
		this.controller = controller;
		this.players = players;
		this.aiPlayers = aiPlayers;
		this.playerItems = playerItems;
		this.aiItems = aiItems;
		this.objects = objects;
		this.terrain = terrain;

		// import obstacles from the terrain
		terrain.getObstacles().stream().forEach(p ->
			objects.add(BattleObject.newTerrainObstacle(p)));

		// create a dummy turn 0 to 'prime the pump'
		turnNumber = 0;
		turn = new Turn(
			!playerGoesFirst,
			new ArrayList<TurnCharacter>(),
			turnItems(!playerGoesFirst),
			this,
			turnNumber);

		// change to turn 1, the actual first turn
		changeTurn();
	}

	public void checkTurn(int oturn) {
		if (oturn != this.turnNumber) throw new RuntimeException(
				"Object from turn " + oturn +
				" used on turn " + this.turnNumber);
	}

	/**
	 * may return null
	 * */
	public Character getCharacterAt(Position pos) {
		return players.stream()
			.filter(c -> c.position.equals(pos))
			.findFirst().orElse(
				aiPlayers.stream()
					.filter(c -> c.position.equals(pos))
					.findFirst().orElse(null));
	}

	/**
	 * may return null
	 * */
	public BattleObject getObjectAt(Position pos) {
		return objects.stream()
			.filter(o -> o.position.equals(pos)).findFirst().orElse(null);
	}
	
	/**
	 * Get the objects that would be triggered if a character moved to a
	 * particular square.
	 * */
	public Collection<BattleObject> getTriggeredObjects(Position pos) {
		return objects.stream()
			.filter(o -> o.mayTriggerAbilityAt(pos, this))
			.collect(Collectors.toList());
	}

	/**
	 * Get the objects that are obstacles to path-finding.
	 * i.e. blockPath objects. blockSpace objects are located by getOccupiedPositions
	 *
	 * must not return null
	 *
	 * @param isPlayer true if we are getting obstacles to the player's team.
	 * false if we are getting obstacles to the ai Player's team.
	 * */
	public Collection<Position> getObstacles(boolean isPlayer) {
		Collection<Character> characters;
		if (isPlayer) characters = aiPlayers;
		else characters = players;

		return Stream.concat(
			objects.stream().filter(o -> o.blocksPath).map(o -> o.position),
			characters.stream().map(c -> c.position)
		).collect(Collectors.toList());
	}

	/**
	 * Get the spaces that are occupied e.g. by characters.  Some of these
	 * objects may not be path-blocking, for path-blocking obstacles, use
	 * getObstacles
	 * */
	public Collection<Position> getOccupiedPositions() {
		return Stream.concat(
			objects.stream().filter(o -> o.blocksSpace).map(o -> o.position),
			Stream.concat(players.stream(), aiPlayers.stream()).map(c -> c.position)
		).collect(Collectors.toList());
	}

	/**
	 * Remove destroyed objects.
	 * */
	public void grimReaper() {
		this.objects = objects.stream()
			.filter(o -> o.hitsRemaining > 0)
			.collect(Collectors.toList());
	}

	/**
	 * Kill a character and remove from the board.
	 * */
	public void kill(List<Character> cs) {
		List<BattleObject> bodies = cs.stream().map(c -> {
			BattleObject body = c.die();
			objects.add(body);
			players.remove(c);
			aiPlayers.remove(c);
			return body;
		}).collect(Collectors.toList());
		controller.callOnDeath(bodies);
	}

	public void changeTurn() {
		if (isBattleOver()) {
			if (aiPlayers.size() == 0) {
				controller.callOnBattleEnd(true);
			} else {
				controller.callOnBattleEnd(false);
			}

		} else {
			turnNumber += 1;
			boolean isPlayerTurn = !turn.isPlayerTurn;
			turn = new Turn(
				isPlayerTurn,
				turnCharacters(isPlayerTurn, turnNumber),
				turnItems(isPlayerTurn),
				this, turnNumber);

			// notify the presentation layer that a new turn has started
			if (isPlayerTurn) {
				controller.callOnPlayerTurnStart(turn);
			} else {
				controller.callOnAIPlayerTurnStart(turn);
			}
		}
	}

	public boolean isBattleOver() {
		grimReaper();
		return players.size() == 0 || aiPlayers.size() == 0;
	}

	private Collection<TurnCharacter> turnCharacters(
		boolean isPlayerTurn, int turnNumber
	) {
		Stream<Character> s;
		if (isPlayerTurn) s = players.stream(); else s = aiPlayers.stream();
		return s
			.map(c -> new TurnCharacter(c, turnNumber, this))
			.collect(Collectors.toList());
	}

	private ItemBag turnItems(boolean isPlayerTurn) {
		if (isPlayerTurn) {
			return playerItems;
		} else {
			return aiItems;
		}
	}
}

