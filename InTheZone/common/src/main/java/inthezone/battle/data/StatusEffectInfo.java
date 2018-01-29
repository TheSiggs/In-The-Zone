package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import ssjsjs.annotations.Field;
import ssjsjs.JSONable;
import ssjsjs.annotations.JSON;

public class StatusEffectInfo implements JSONable {
	public final StatusEffectType type;
	public final StatusEffectKind kind;
	public final int param;

	private static final int DEFAULT_PARAMETER = 0;

	@JSON
	public StatusEffectInfo(
		@Field("type") final StatusEffectType type,
		@Field("kind") final StatusEffectKind kind,
		@Field("param") final int param
	) {
		this.type = type;
		this.kind = kind;
		this.param = param;
	}

	public StatusEffectInfo(final String effect)
		throws CorruptDataException
	{
		String parts[] = effect.split("\\s");
		if (parts.length < 1) throw new CorruptDataException("Expected status effect");
		this.type = StatusEffectType.valueOf(parts[0]);
		this.kind = type.getEffectKind();

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

	@Override public boolean equals(final Object obj) {
		if (this == obj) return true;
		else if (!(obj instanceof StatusEffectInfo)) return false;
		else {
			StatusEffectInfo x = (StatusEffectInfo) obj;
			return
				this.type == x.type &&
				this.kind == x.kind &&
				this.param == x.param;
		}
	}

	@Override public int hashCode() {
		return type.hashCode() + kind.hashCode() + param;
	}

	public String toNiceString() {
		if (param == DEFAULT_PARAMETER) {
			return type.toNiceString();
		} else {
			return type.toNiceString() + " " + param;
		}
	}

	@Override public String toString() {
		if (param == DEFAULT_PARAMETER) {
			return type.toString();
		} else {
			return type.toString() + " " + param;
		}
	}
}

