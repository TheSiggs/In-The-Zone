package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import java.util.ArrayList;
import java.util.List;

public class StatusEffect {
	private static final int TOTAL_ROUNDS = 2;
	private int remainingTurns = 0;

	public StatusEffect() {
		remainingTurns = TOTAL_ROUNDS;
	}

	public final boolean canRemoveNow() {
		remainingTurns -= 1;
		return remainingTurns <= 0;
	}

	public double getAttackBuff() {return 0.0;}
	public double getDefenceBuff() {return 0.0;}
	public double getChanceBuff() {return 0.0;}
	public List<Command> doBeforeTurn(Battle battle, Character c) {
		return new ArrayList<>();
	}
}

