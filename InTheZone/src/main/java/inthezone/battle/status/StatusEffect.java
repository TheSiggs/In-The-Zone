package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.StatusEffectInfo;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class StatusEffect implements HasJSONRepresentation {
	private static final int TOTAL_ROUNDS = 2;
	private int remainingTurns = 0;
	protected StatusEffectInfo info;

	public StatusEffect(StatusEffectInfo info) {
		remainingTurns = TOTAL_ROUNDS;
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

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("info", info.toString());
		return o;
	}
}

