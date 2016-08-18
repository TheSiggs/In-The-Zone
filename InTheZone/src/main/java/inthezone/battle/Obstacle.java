package inthezone.battle;

import inthezone.battle.data.Player;

/**
 * Interface for objects which can act as obstacles to movement.
 * */
public interface Obstacle {
	public boolean blocksSpace(Player player);
	public boolean blocksPath(Player player);
}

