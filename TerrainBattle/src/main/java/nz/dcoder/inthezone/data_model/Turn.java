package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Information about a turn
 * */
public class Turn {
	final boolean isPlayerTurn;
	final private Collection<TurnCharacter> turnCharacters;
	final private Battle battle;
	final private int turnNumber;

	public Turn (
		boolean isPlayerTurn,
		Collection<TurnCharacter> turnCharacters,
		Battle battle,
		int turnNumber
	) {
		this.isPlayerTurn = isPlayerTurn;
		this.turnCharacters = turnCharacters;
		this.battle = battle;
		this.turnNumber = turnNumber;
	}

	/**
	 * May return null
	 * */
	public TurnCharacter turnCharacterAt(Position pos) {
		return turnCharacters.stream()
			.filter(t -> t.getPos().equals(pos)).findFirst().orElse(null);
	}

	/**
	 * Get all the turn characters
	 * */
	public Collection<TurnCharacter> getCharacters() {
		return turnCharacters;
	}

	/**
	 * Determine if there is anything more that can be done on this turn
	 * @param items Consumable items available to the player
	 * @return true if there is nothing more that can be done on this turn
	 * */
	boolean isTurnOver(Collection<Item> items) {
		return !turnCharacters.stream().anyMatch(t -> t.hasOptions(items));
	}

	public void endTurn() {
		battle.changeTurn();
	}
}

