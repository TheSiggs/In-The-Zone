package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Information about a turn
 * */
public class Turn {
	final boolean isPlayerTurn;
	final public Collection<TurnCharacter> turnCharacters;
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
		// TODO: implement this method
		return null;
	}

	boolean isTurnOver() {
		// TODO: implement this method
		// check if turn characters have any moves left
		return false;
	}

	public void endTurn() {
		battle.changeTurn();
	}
}

