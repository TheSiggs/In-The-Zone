package inthezone.battle;

/**
 * Outcome of the battle.
 * */
public enum BattleOutcome {
	WIN,              // player wins
	LOSE,             // player loses
	DRAW,             // it's a draw
	RESIGN,           // player resigned
	OTHER_RESIGNED,   // other player resigned
	OTHER_LOGGED_OUT, // other player logged out or disconnected
	ERROR,            // there was an error in this client
	OTHER_ERROR       // there was an error in the other client
}

