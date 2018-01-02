package inthezone.battle;

import inthezone.battle.status.StatusEffect;
import java.util.Optional;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.JSONable;

public class DamageToTarget implements JSONable {
	public final Casting target;
	public final boolean isTargetATrap;
	public final boolean isTargetAZone;
	public final int damage;
	public final Optional<StatusEffect> statusEffect;
	public final boolean pre;
	public final boolean post;

	@JSONConstructor
	public DamageToTarget(
		@Field("target") final Casting target,
		@Field("trap") final boolean isTargetATrap,
		@Field("zone") final boolean isTargetAZone,
		@Field("damage") final int damage,
		@Field("status") final Optional<StatusEffect> statusEffect
	) {
		this(target, isTargetATrap, isTargetAZone, damage, statusEffect, false, false);
	}

	public DamageToTarget(
		final Casting target,
		final boolean isTargetATrap,
		final boolean isTargetAZone,
		final int damage,
		final Optional<StatusEffect> statusEffect,
		final boolean pre,
		final boolean post
	) {
		this.target = target;
		this.isTargetATrap = isTargetATrap;
		this.isTargetAZone = isTargetAZone;
		this.damage = damage;
		this.statusEffect = statusEffect;
		this.pre = pre;
		this.post = post;
	}

	/**
	 * The target has moved since the damage was calculated
	 * */
	public DamageToTarget retarget(final Casting to) {
		if (isTargetATrap || isTargetAZone) {
			return this;
		} else {
			return new DamageToTarget(
				to, false, false, damage, statusEffect, pre, post);
		}
	}

	@Override public String toString() {
		return "Damage " + damage + " " + target +
			(isTargetATrap? " (trap) " : "") +
			(isTargetAZone? " (zone) " : "") +
			" " + statusEffect;
	}
}

