package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import ssjsjs.JSONable;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * Static data regarding an instant effect.
 * */
public class InstantEffectInfo implements JSONable {
	public final InstantEffectType type;
	public final int param;

	private static final int DEFAULT_PARAMETER = 0;

	@JSON
	public InstantEffectInfo(
		@Field("type") final InstantEffectType type,
		@Field("param") final int param
	) {
		this.type = type;
		this.param = param;
	}

	public InstantEffectInfo(final String effect)
		throws CorruptDataException
	{
		String parts[] = effect.split("\\s");
		if (parts.length < 1) throw new CorruptDataException("Expected instant effect");
		this.type = InstantEffectType.valueOf(parts[0]);

		int paramv = DEFAULT_PARAMETER;
		if (parts.length >= 2) {
			try {
				paramv = Integer.parseInt(parts[1]);
			} catch (final NumberFormatException e) {
				// ignore
			}
		}
		this.param = paramv;
	}

	@Override public int hashCode() {
		return type.hashCode() * param;
	}

	@Override public boolean equals(final Object b) {
		if (!(b instanceof InstantEffectInfo)) return false;
		else {
			final InstantEffectInfo i = (InstantEffectInfo) b;
			return i.type == this.type && i.param == this.param;
		}
	}

	/**
	 * Determine if this instant effect is a field effect (i.e. it doesn't need
	 * an explicit target).
	 * */
	public boolean isField() {
		return type == InstantEffectType.OBSTACLES;
	}

	@Override public String toString() {
		if (param == DEFAULT_PARAMETER) {
			return type.toString();
		} else {
			return type.toString() + " " + param;
		}
	}
}

