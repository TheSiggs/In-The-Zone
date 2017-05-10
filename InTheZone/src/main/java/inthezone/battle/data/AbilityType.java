package inthezone.battle.data;

import isogame.engine.CorruptDataException;

public enum AbilityType {
	BASIC, SPECIAL, SKILL, SPELL;

	public static AbilityType parse(String s)
		throws CorruptDataException
	{
		switch (s.toLowerCase()) {
			case "basic": return BASIC;
			case "special": return SPECIAL;
			case "skill": return SKILL;
			case "spell": return SPELL;
			default: throw new CorruptDataException("Unknown ability type " + s);
		}
	}

	@Override public String toString() {
		switch (this) {
			case BASIC: return "basic";
			case SPECIAL: return "special";
			case SKILL: return "skill";
			case SPELL: return "spell";
			default:
				throw new RuntimeException("This cannot happen");
		}
	}
}

