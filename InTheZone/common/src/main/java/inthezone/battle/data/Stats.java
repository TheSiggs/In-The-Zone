package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import org.json.JSONException;
import org.json.JSONObject;

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
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
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
		try {
			final int ap       = json.getInt("ap");
			final int mp       = json.getInt("mp");
			final int power    = json.getInt("power");
			final int hp       = json.getInt("hp");
			final int attack   = json.getInt("attack");
			final int defence  = json.getInt("defence");
			return new Stats(ap, mp, power, hp, attack, defence);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing stats, " + e.getMessage(), e);
		}
	}
}

