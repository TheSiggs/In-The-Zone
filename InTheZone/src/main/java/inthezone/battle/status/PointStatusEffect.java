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
import org.json.simple.JSONObject;

public class PointStatusEffect extends StatusEffect {
	private final int ap;
	private final int mp;

	public PointStatusEffect(StatusEffectInfo info, int ap, int mp) {
		super(info);

		this.ap = ap;
		this.mp = mp;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("ap", ap);
		r.put("mp", mp);
		return r;
	}

	public static PointStatusEffect fromJSON(JSONObject o)
		throws ProtocolException
	{
		Object oinfo = o.get("info");
		Object oap = o.get("ap");
		Object omp = o.get("mp");

		if (oinfo == null) throw new ProtocolException("Missing status effect type");
		if (oap == null) throw new ProtocolException("Missing ap buff");
		if (omp == null) throw new ProtocolException("Missing mp buff");

		try {
			StatusEffectInfo info = new StatusEffectInfo((String) oinfo);

			return new PointStatusEffect(info,
				((Number) oap).intValue(),
				((Number) omp).intValue());
		} catch (ClassCastException|CorruptDataException  e) {
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

