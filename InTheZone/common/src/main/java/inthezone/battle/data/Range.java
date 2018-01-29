package inthezone.battle.data;

import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;
import ssjsjs.JSONable;

/**
 * Range information for an ability.
 * */
public class Range implements JSONable {
	public final int range;
	public final int radius;
	public final boolean piercing;
	public final TargetMode targetMode;
	public final int nTargets;
	public final boolean los;

	private final String targetModeRaw;

	public Range(
		final int range,
		final int radius,
		final boolean piercing,
		final TargetMode targetMode,
		final int nTargets,
		final boolean los
	) {
		this(range, radius, piercing, targetMode.toString(), nTargets, los);
	}

	@JSON
	public Range(
		@Field("range") final int range,
		@Field("radius") final int radius,
		@Field("piercing") final boolean piercing,
		@Field("targetModeRaw")@As("targetMode") final String targetModeRaw,
		@Field("nTargets") final int nTargets,
		@Field("los") final boolean los
	) {
		this.targetMode = new TargetMode(targetModeRaw);
		this.range = range;
		this.piercing = piercing;
		this.radius = radius;
		this.targetModeRaw = targetModeRaw;
		this.nTargets = nTargets;
		this.los = los;
	}
}

