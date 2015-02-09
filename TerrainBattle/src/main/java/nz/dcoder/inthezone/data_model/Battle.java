package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
		Terrain terrain,
		boolean playerGoesFirst  // true if the human player goes first
	) {
		this.controller = controller;
		this.players = players;
		this.aiPlayers = aiPlayers;
		this.objects = objects;
		this.terrain = terrain;

		// create a dummy turn 0 to 'prime the pump'
		turnNumber = 0;
		turn = new Turn(
			!playerGoesFirst,
			new ArrayList<TurnCharacter>(),
			this,
			turnNumber);

		// change to turn 1, the actual first turn
		changeTurn();
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
	 * Get the objects that are obstacles to path-finding.
	 * i.e. blockPath objects. blockSpace objects need to be checked for elsewhere
	 *
	 * must not return null
	 * */
	public Collection<Position> getObstacles() {
		return Stream.concat(
			objects.stream().filter(o -> o.blocksPath).map(o -> o.position),
			Stream.concat(players.stream(), aiPlayers.stream()).map(c -> c.position)
		).collect(Collectors.toList());
	}

	private void grimReaper() {
		this.players = players.stream()
			.filter(c -> !c.isDead).collect(Collectors.toList());
		this.aiPlayers = aiPlayers.stream()
			.filter(c -> !c.isDead).collect(Collectors.toList());
		this.objects = objects.stream()
			.filter(o -> !(o.isAttackable && o.hitsRemaining <= 0))
			.collect(Collectors.toList());
	}

	public void changeTurn() {
		grimReaper();
		turnNumber += 1;
		boolean isPlayerTurn = !turn.isPlayerTurn;
		turn = new Turn(
			isPlayerTurn,
			turnCharacters(isPlayerTurn, turnNumber),
			this, turnNumber);

		// notify the presentation layer if they player's turn has started
		if (isPlayerTurn && controller.onPlayerTurnStart != null) {
			controller.onPlayerTurnStart.accept(turn);
		}
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
}

