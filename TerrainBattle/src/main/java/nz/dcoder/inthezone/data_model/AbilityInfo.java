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
	public final boolean isPiercing;
	public final boolean canPassObstacles;
	public final boolean requiresMana;
	public final String aClass;
	public final EffectName effect;

	AbilityInfo(
		AbilityName name,
		int cost,
		int amount,
		int range,
		int areaOfEffect,
		boolean isPiercing,
		boolean canPassObstacles,
		boolean requiresMana,
		String aClass,
		EffectName effect
	) {
		this.name = name;
		this.cost = cost;
		this.amount = amount;
		this.range = range;
		this.areaOfEffect = areaOfEffect;
		this.isPiercing = isPiercing;
		this.canPassObstacles = canPassObstacles;
		this.requiresMana = requiresMana;
		this.aClass = aClass;
		this.effect = effect;
	}
}

