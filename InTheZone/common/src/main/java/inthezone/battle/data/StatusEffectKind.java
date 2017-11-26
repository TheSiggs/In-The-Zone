package inthezone.battle.data;

public enum StatusEffectKind {
	BUFF, DEBUFF;

	@Override public String toString() {
		switch (this) {
			case BUFF: return "buff";
			case DEBUFF: return "debuff";
			default: throw new RuntimeException("This cannot happen");
		}
	}
}

