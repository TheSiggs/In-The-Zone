package inthezone.battle;

import isogame.engine.MapPoint;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.data.InstantEffectInfo;

public class DamageToTarget {
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
}

