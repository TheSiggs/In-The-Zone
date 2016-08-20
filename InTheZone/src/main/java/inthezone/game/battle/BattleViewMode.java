package inthezone.game.battle;

/**
 * Which mode the battle GUI is in.
 * */
public enum BattleViewMode {
	ANIMATING, // waiting for an animation to complete
	SELECT, // waiting for a character to be selected
	MOVE; // waiting for a move target
}

