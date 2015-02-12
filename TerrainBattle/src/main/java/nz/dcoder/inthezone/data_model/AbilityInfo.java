package nz.dcoder.inthezone.data_model;

import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.EffectName;

/**
 * Static information about abilities
 * */
public class AbilityInfo {
	public final AbilityName name;
	public final int cost;
	public final int amount;
	public final int range;
	public final int areaOfEffect;
	public final boolean hasAOEShading;
	public final boolean isPiercing;
	public final boolean canPassObstacles;
	public final boolean requiresMana;
	public final int repeats;
	public final String aClass;
	public final EffectName effect;

	public AbilityInfo(
		AbilityName name,
		int cost,
		int amount,
		int range,
		int areaOfEffect,
		boolean hasAOEShading,
		boolean isPiercing,
		boolean canPassObstacles,
		boolean requiresMana,
		int repeats,
		String aClass,
		EffectName effect
	) {
		this.name = name;
		this.cost = cost;
		this.amount = amount;
		this.range = range;
		this.areaOfEffect = areaOfEffect;
		this.hasAOEShading = hasAOEShading;
		this.isPiercing = isPiercing;
		this.canPassObstacles = canPassObstacles;
		this.requiresMana = requiresMana;
		this.repeats = repeats;
		this.aClass = aClass;
		this.effect = effect;
	}
}

