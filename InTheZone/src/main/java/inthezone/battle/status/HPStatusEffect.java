package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * A status effect that does ongoing damage or healing before the players turn.
 * */
public class HPStatusEffect extends StatusEffect {
	private final int hp;

	public HPStatusEffect(StatusEffectInfo info, int hp) {
		super(info);

		this.hp = hp;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("hp", hp);
		return r;
	}

	public static HPStatusEffect fromJSON(JSONObject o)
		throws ProtocolException
	{
		Object oinfo = o.get("info");
		Object ohp = o.get("hp");

		if (oinfo == null) throw new ProtocolException("Missing status effect type");
		if (ohp == null) throw new ProtocolException("Missing hp buff");

		try {
			StatusEffectInfo info = new StatusEffectInfo((String) oinfo);

			return new HPStatusEffect(info, ((Number) ohp).intValue());
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing hp status effect", e);
		}
	}

	@Override public List<Command> doBeforeTurn(Battle battle, Character c) {
		c.pointsBuff(0, 0, hp);
		return new ArrayList<>();
	}
}

