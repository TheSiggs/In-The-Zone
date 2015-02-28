package nz.dcoder.inthezone.data_model.pure;

/**
 * Static information about abilities
 * */
public class AbilityInfo {
	public final AbilityName name;
	public final int cost;
	public final double s;
	public final int range;
	public final int areaOfEffect;
	public final boolean hasAOEShading;
	public final boolean isPiercing;
	public final boolean requiresLOS;
	public final boolean requiresMana;
	public final int repeats;
	public final AbilityClass aClass;
	public final EffectName effect;

	public AbilityInfo(
		AbilityName name,
		int cost,
		double s,
		int range,
		int areaOfEffect,
		boolean hasAOEShading,
		boolean isPiercing,
		boolean requiresLOS,
		boolean requiresMana,
		int repeats,
		AbilityClass aClass,
		EffectName effect
	) {
		this.name = name;
		this.cost = cost;
		this.s = s;
		this.range = range;
		this.areaOfEffect = areaOfEffect;
		this.hasAOEShading = hasAOEShading;
		this.isPiercing = isPiercing;
		this.requiresLOS = requiresLOS;
		this.requiresMana = requiresMana;
		this.repeats = repeats;
		this.aClass = aClass;
		this.effect = effect;
	}
}

