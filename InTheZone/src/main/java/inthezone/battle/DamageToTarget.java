package inthezone.battle;

import inthezone.battle.status.StatusEffect;
import inthezone.battle.status.StatusEffectFactory;
import inthezone.protocol.ProtocolException;
import isogame.engine.HasJSONRepresentation;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public class DamageToTarget implements HasJSONRepresentation {
	public final Casting target;
	public final boolean isTargetATrap;
	public final boolean isTargetAZone;
	public final int damage;
	public final Optional<StatusEffect> statusEffect;
	public final boolean pre;
	public final boolean post;

	public DamageToTarget(
		Casting target,
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
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("target", target.getJSON());
		r.put("trap", isTargetATrap);
		r.put("zone", isTargetAZone);
		r.put("damage", damage);
		statusEffect.ifPresent(s -> r.put("status", s.getJSON()));
		return r;
	}

	public static DamageToTarget fromJSON(JSONObject json) throws ProtocolException {
		try {
			final Casting target = Casting.fromJSON(json.getJSONObject("target"));
			final boolean trap = json.getBoolean("trap");
			final boolean zone = json.getBoolean("zone");
			final int damage = json.getInt("damage");
			final JSONObject ostatus = json.optJSONObject("status");

			Optional<StatusEffect> effect = Optional.empty();
			if (ostatus != null) {
				effect = Optional.of(StatusEffectFactory.fromJSON(ostatus));
			}

			return new DamageToTarget(target, trap, zone,
				damage, effect, false, false);

		} catch (JSONException e) {
			throw new ProtocolException("Error parsing damage", e);
		}
	}

	/**
	 * The target has moved since the damage was calculated
	 * */
	public DamageToTarget retarget(Casting to) {
		if (isTargetATrap || isTargetAZone) {
			return this;
		} else {
			return new DamageToTarget(to, false, false, damage, statusEffect, pre, post);
		}
	}

	@Override public String toString() {
		return "Damage " + damage + " " + target +
			(isTargetATrap? " (trap) " : "") +
			(isTargetAZone? " (zone) " : "") +
			" " + statusEffect;
	}
}

