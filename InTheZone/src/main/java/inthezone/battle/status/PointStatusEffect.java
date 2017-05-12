package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class PointStatusEffect extends StatusEffect {
	private final int ap;
	private final int mp;

	public PointStatusEffect(StatusEffectInfo info, int ap, int mp) {
		super(info);

		this.ap = ap;
		this.mp = mp;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("ap", ap);
		r.put("mp", mp);
		return r;
	}

	public static PointStatusEffect fromJSON(JSONObject o)
		throws ProtocolException
	{
		try {
			final StatusEffectInfo info = new StatusEffectInfo(o.getString("info"));
			int ap = o.getInt("ap");
			int mp = o.getInt("mp");

			return new PointStatusEffect(info, ap, mp);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing point status effect", e);
		}
	}

	@Override public Stats getBaseStatsBuff() {
		return new Stats(ap, mp, 0, 0, 0, 0);
	}

	@Override public List<Command> doNow(Character c) {
		c.pointsBuff(ap, mp, 0);
		return new ArrayList<>();
	}
}

