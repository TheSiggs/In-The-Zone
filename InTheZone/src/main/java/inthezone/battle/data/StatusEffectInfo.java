package inthezone.battle.data;

import isogame.engine.CorruptDataException;

public class StatusEffectInfo {
	public final StatusEffectType type;
	public final StatusEffectKind kind;
	public final int param;

	private static final int DEFAULT_PARAMETER = 0;

	public StatusEffectInfo(final String effect)
		throws CorruptDataException
	{
		String parts[] = effect.split("\\s");
		if (parts.length < 1) throw new CorruptDataException("Expected status effect");
		this.type = StatusEffectType.parse(parts[0]);
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

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof StatusEffectInfo) {
			StatusEffectInfo x = (StatusEffectInfo) obj;
			return
				this.type == x.type &&
				this.kind == x.kind &&
				this.param == x.param;
		} else return false;
	}

	@Override
	public int hashCode() {
		return type.hashCode() + kind.hashCode() + param;
	}

	public String toNiceString() {
		if (param == DEFAULT_PARAMETER) {
			return type.toNiceString();
		} else {
			return type.toNiceString() + " " + (new Integer(param)).toString();
		}
	}

	@Override
	public String toString() {
		if (param == DEFAULT_PARAMETER) {
			return type.toString();
		} else {
			return type.toString() + " " + (new Integer(param)).toString();
		}
	}
}

