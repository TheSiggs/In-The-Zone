package inthezone.battle;

import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.StatusEffectFactory;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.Optional;
import org.json.simple.JSONObject;

public class DamageToTarget implements HasJSONRepresentation {
	public final MapPoint target;
	public final boolean isTargetATrap;
	public final boolean isTargetAZone;
	public final int damage;
	public final Optional<StatusEffect> statusEffect;
	public final boolean pre;
	public final boolean post;

	public DamageToTarget(
		MapPoint target,
		boolean isTargetATrap,
		boolean isTargetAZone,
		int damage,
		Optional<StatusEffect> statusEffect,
		boolean pre,
		boolean post
	) {
		this.target = target;
		this.isTargetATrap = isTargetATrap;
		this.isTargetAZone = isTargetAZone;
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
		r.put("trap", isTargetATrap);
		r.put("zone", isTargetAZone);
		r.put("damage", damage);
		statusEffect.ifPresent(s -> r.put("status", s.getJSON()));
		return r;
	}

	public static DamageToTarget fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object otarget = json.get("target");
		Object otrap = json.get("trap");
		Object ozone = json.get("zone");
		Object odamage = json.get("damage");
		Object ostatus = json.get("status");

		if (otarget == null) throw new ProtocolException("Missing damage target");
		if (otrap == null) throw new ProtocolException("Missing damage trap switch");
		if (ozone == null) throw new ProtocolException("Missing damage zone switch");
		if (odamage == null) throw new ProtocolException("Missing damage amount");

		try {
			MapPoint target = MapPoint.fromJSON((JSONObject) otarget);
			Boolean trap = (Boolean) otrap;
			Boolean zone = (Boolean) ozone;
			Number damage = (Number) odamage;

			Optional<StatusEffect> effect = Optional.empty();
			if (ostatus != null) {
				effect = Optional.of(StatusEffectFactory.fromJSON((JSONObject) ostatus));
			}

			return new DamageToTarget(target, trap, zone,
				damage.intValue(), effect, false, false);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing damage", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing damage", e);
		}
	}

	/**
	 * The target has moved since the damage was calculated
	 * */
	public DamageToTarget retarget(MapPoint to) {
		if (isTargetATrap || isTargetAZone) {
			return this;
		} else {
			return new DamageToTarget(to, false, false, damage, statusEffect, pre, post);
		}
	}
}

