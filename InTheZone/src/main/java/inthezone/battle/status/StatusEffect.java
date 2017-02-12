package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class StatusEffect implements HasJSONRepresentation {
	private static final int TOTAL_ROUNDS = 2;
	private int remainingTurns = TOTAL_ROUNDS;
	public StatusEffectInfo info;

	public StatusEffect(StatusEffectInfo info) {
		this.info = info;
	}

	public final boolean canRemoveNow() {
		remainingTurns -= 1;
		return remainingTurns <= 0;
	}

	public final StatusEffectInfo getInfo() {
		return info;
	}

	public double getAttackBuff() {return 0.0;}
	public double getDefenceBuff() {return 0.0;}
	public double getChanceBuff() {return 0.0;}
	public List<Command> doBeforeTurn(Battle battle, Character c) {
		return new ArrayList<>();
	}

	public Stats getBaseStatsBuff() {
		return new Stats();
	}

	public List<Command> doNow(Character c) {
		return new ArrayList<>();
	}

	public boolean isBeforeTurnExhaustive() {return false;}

	/**
	 * Some status effects require extra information not available at parsing
	 * time.  This command supplies the extra information.  It should be called
	 * before applying a status effect to a character.
	 * */
	public StatusEffect resolve(BattleState battle) throws ProtocolException {
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("info", info.toString());
		return o;
	}
}

