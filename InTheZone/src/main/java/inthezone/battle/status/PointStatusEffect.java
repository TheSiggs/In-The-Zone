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

	public PointStatusEffect(
		final StatusEffectInfo info, final int startTurn, final int ap, final int mp
	) {
		super(info, startTurn);

		this.ap = ap;
		this.mp = mp;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("startTurn", startTurn);
		r.put("ap", ap);
		r.put("mp", mp);
		return r;
	}

	public static PointStatusEffect fromJSON(final JSONObject o)
		throws ProtocolException
	{
		try {
			final StatusEffectInfo info = new StatusEffectInfo(o.getString("info"));
			final int startTurn = o.getInt("startTurn");
			int ap = o.getInt("ap");
			int mp = o.getInt("mp");

			return new PointStatusEffect(info, startTurn, ap, mp);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing point status effect", e);
		}
	}

	@Override public Stats getBaseStatsBuff() {
		return new Stats(ap, mp, 0, 0, 0, 0);
	}

	@Override public List<Command> doNow(final Character c) {
		c.pointsBuff(ap, mp, 0);
		return new ArrayList<>();
	}
}

