package inthezone.battle.commands;

/**
 * The root cause of a resignation event.
 * */
public enum ResignReason {
	RESIGNED,   // The other player resigned
	LOGGED_OFF, // The other player logged off
	ERROR;      // The other player's client had an error
}

