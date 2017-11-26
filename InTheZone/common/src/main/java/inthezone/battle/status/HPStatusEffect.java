package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A status effect that does ongoing damage or healing before the players turn.
 * */
public class HPStatusEffect extends StatusEffect {
	private final int hp;

	public HPStatusEffect(
		final StatusEffectInfo info, final int hp, final int startTurn
	) {
		super(info, startTurn);

		this.hp = hp;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("startTurn", startTurn);
		r.put("hp", hp);
		return r;
	}

	public static HPStatusEffect fromJSON(final JSONObject o)
		throws ProtocolException
	{
		try {
			final StatusEffectInfo info = new StatusEffectInfo(o.getString("info"));
			final int startTurn = o.getInt("startTurn");
			final int hp = o.getInt("hp");

			return new HPStatusEffect(info, hp, startTurn);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing hp status effect", e);
		}
	}

	@Override public List<Command> doBeforeTurn(
		final Battle battle, final Character c
	) {
		c.pointsBuff(0, 0, hp);
		return new ArrayList<>();
	}
}

