package inthezone.game.battle;

/**
 * Which mode the battle GUI is in.
 * */
public enum BattleViewMode {
	OTHER_TURN, // waiting for the other player's turn
	ANIMATING, // waiting for an animation to complete
	SELECT, // waiting for a character to be selected
	MOVE, // waiting for a move target
	TARGET, // waiting for the player to select a target
	TELEPORT; // waiting for the player to select teleport targets
}

