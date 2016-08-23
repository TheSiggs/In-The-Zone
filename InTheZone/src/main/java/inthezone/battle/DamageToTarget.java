package inthezone.battle;

import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import org.json.simple.JSONObject;

public class DamageToTarget implements HasJSONRepresentation {
	public final MapPoint target;
	public final int damage;
	public final StatusEffectInfo statusEffect;
	public final InstantEffectInfo pre;
	public final InstantEffectInfo post;

	public DamageToTarget(
		MapPoint target,
		int damage,
		StatusEffectInfo statusEffect,
		InstantEffectInfo pre,
		InstantEffectInfo post
	) {
		this.target = target;
		this.damage = damage;
		this.statusEffect = statusEffect;
		this.pre = pre;
		this.post = post;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("target", target.getJSON());
		r.put("damage", damage);
		return r;
	}

	public static DamageToTarget fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object otarget = json.get("target");
		Object odamage = json.get("damage");

		if (otarget == null) throw new ProtocolException("Missing damage target");
		if (odamage == null) throw new ProtocolException("Missing damage amount");

		try {
			MapPoint target = MapPoint.fromJSON((JSONObject) otarget);
			Number damage = (Number) odamage;
			return new DamageToTarget(target, damage.intValue(), null, null, null);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing damage", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing damage", e);
		}
	}

}

