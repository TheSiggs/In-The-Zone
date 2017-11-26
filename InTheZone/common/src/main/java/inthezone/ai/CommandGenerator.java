package inthezone.ai;

import inthezone.battle.Battle;
import inthezone.battle.BattleListener;
import inthezone.battle.data.Player;

/**
 * Interface for AIs.
 * */
public interface CommandGenerator {
	/**
	 * Generate commands, execute them, and forward them on to the GUI.
	 * */
	public void generateCommands(
		Battle battle, BattleListener listener, Player forPlayer);
}

