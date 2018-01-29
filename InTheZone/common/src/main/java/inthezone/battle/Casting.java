package inthezone.battle;

import isogame.engine.MapPoint;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;
import ssjsjs.JSONable;

/**
 * Represents a single casting or recasting of an ability.
 * */
public class Casting implements JSONable {
	public final MapPoint castFrom;
	public final MapPoint target;

	@JSON
	public Casting(
		@Field("castFrom") MapPoint castFrom,
		@Field("target") MapPoint target
	) {
		this.castFrom = castFrom;
		this.target = target;
	}

	@Override public String toString() {
		return "Casting from " + castFrom + " of " + target;
	}
}

