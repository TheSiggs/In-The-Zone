package inthezone.battle;

import inthezone.battle.data.Player;

/**
 * Interface for objects which can act as obstacles to movement.
 * */
public interface Obstacle {
	public boolean blocksSpace();
	public boolean blocksPath(Player player);
}

