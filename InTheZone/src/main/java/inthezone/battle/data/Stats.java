package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import org.json.simple.JSONObject;

public class Stats implements HasJSONRepresentation {
	public final int ap;
	public final int mp;
	public final int power;
	public final int hp;
	public final int attack;
	public final int defence;

	public Stats() {
		this.ap = 0;
		this.mp = 0;
		this.power = 0;
		this.hp = 0;
		this.attack = 0;
		this.defence = 0;
	}

	public Stats(
		int ap, int mp,
		int power, int hp,
		int attack, int defence
	) {
		this.ap = ap;
		this.mp = mp;
		this.power = power;
		this.hp = hp;
		this.attack = attack;
		this.defence = defence;
	}

	public Stats add(Stats stats) {
		return new Stats(
			ap + stats.ap,
			mp + stats.mp,
			power + stats.power,
			hp + stats.hp,
			attack + stats.attack,
			defence + stats.defence);
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("ap", ap);
		r.put("mp", mp);
		r.put("power", power);
		r.put("hp", hp);
		r.put("attack", attack);
		r.put("defence", defence);
		return r;
	}

	public static Stats fromJSON(JSONObject json)
		throws CorruptDataException
	{
		Object rap       = json.get("ap");
		Object rmp       = json.get("mp");
		Object rpower    = json.get("power");
		Object rhp = json.get("hp");
		Object rattack   = json.get("attack");
		Object rdefence  = json.get("defence");

		if (rap       == null) throw new CorruptDataException("Missing ap");
		if (rmp       == null) throw new CorruptDataException("Missing mp");
		if (rpower    == null) throw new CorruptDataException("Missing power");
		if (rhp == null) throw new CorruptDataException("Missing hp");
		if (rattack   == null) throw new CorruptDataException("Missing attack");
		if (rdefence  == null) throw new CorruptDataException("Missing defence");

		try {
			Number ap = (Number) rap;
			Number mp = (Number) rmp;
			Number power = (Number) rpower;
			Number hp = (Number) rhp;
			Number attack = (Number) rattack;
			Number defence = (Number) rdefence;

			return new Stats(
				ap.intValue(),
				mp.intValue(),
				power.intValue(),
				hp.intValue(),
				attack.intValue(),
				defence.intValue());
		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in stats", e);
		}
	}
}

