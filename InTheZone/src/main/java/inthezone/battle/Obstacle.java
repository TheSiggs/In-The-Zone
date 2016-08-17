package inthezone.battle;

import inthezone.battle.data.Player;

public interface Obstacle {
	public boolean blocksSpace(Player player);
	public boolean blocksPath(Player player);
}

