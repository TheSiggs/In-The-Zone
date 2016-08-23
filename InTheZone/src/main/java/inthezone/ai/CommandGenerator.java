package inthezone.ai;

import inthezone.battle.Battle;
import inthezone.comptroller.BattleListener;

/**
 * Interface for AIs.
 * */
public interface CommandGenerator {
	/**
	 * Generate commands, execute them, and forward them on to the GUI.
	 * */
	public void generateCommands(Battle battle, BattleListener listener);
}

