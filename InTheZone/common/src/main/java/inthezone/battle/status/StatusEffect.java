package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.JSONable;

/**
 * Base class for status effects.
 * */
public abstract class StatusEffect implements JSONable {
	private static final int TOTAL_TURNS = 4;
	protected final int startTurn;
	public StatusEffectInfo info;

	@Override public String toString() {
		return info.toString();
	}

	public StatusEffect(final StatusEffectInfo info, final int startTurn) {
		this.info = info;
		this.startTurn = startTurn;
	}

	public final boolean canRemoveNow(final int turn) {
		return turn - startTurn >= TOTAL_TURNS;
	}

	public final StatusEffectInfo getInfo() {
		return info;
	}

	public double getAttackBuff() {return 0.0;}
	public double getDefenceBuff() {return 0.0;}
	public double getChanceBuff() {return 0.0;}
	public List<Command> doBeforeTurn(final Battle battle, final Character c) {
		return new ArrayList<>();
	}

	public Stats getBaseStatsBuff() {
		return new Stats();
	}

	public List<Command> doNow(final Character c) {
		return new ArrayList<>();
	}

	public boolean isBeforeTurnExhaustive() {return false;}

	/**
	 * Some status effects require extra information not available at parsing
	 * time.  This command supplies the extra information.  It should be called
	 * before applying a status effect to a character.
	 * */
	public StatusEffect resolve(final BattleState battle) throws ProtocolException {
		return this;
	}
}

