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

public class PointStatusEffect extends StatusEffect {
	private final int ap;
	private final int mp;
	private final int hp;

	public PointStatusEffect(StatusEffectInfo info, int ap, int mp, int hp) {
		super(info);

		this.ap = ap;
		this.mp = mp;
		this.hp = hp;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("ap", ap);
		r.put("mp", mp);
		r.put("hp", hp);
		return r;
	}

	public static PointStatusEffect fromJSON(JSONObject o)
		throws ProtocolException
	{
		Object oinfo = o.get("info");
		Object oap = o.get("ap");
		Object omp = o.get("mp");
		Object ohp = o.get("hp");

		if (oinfo == null) throw new ProtocolException("Missing status effect type");
		if (oap == null) throw new ProtocolException("Missing ap buff");
		if (omp == null) throw new ProtocolException("Missing mp buff");
		if (ohp == null) throw new ProtocolException("Missing hp buff");

		try {
			StatusEffectInfo info = new StatusEffectInfo((String) oinfo);

			return new PointStatusEffect(info,
				((Number) oap).intValue(),
				((Number) omp).intValue(),
				((Number) ohp).intValue());
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing basic status effect", e);
		}
	}

	@Override public List<Command> doBeforeTurn(Battle battle, Character c) {
		c.pointsBuff(ap, mp, hp);
		return new ArrayList<>();
	}
}

