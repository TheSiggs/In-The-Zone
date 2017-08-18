package inthezone.battle.commands;

import isogame.engine.CorruptDataException;

public enum AbilityAgentType {
	CHARACTER, TRAP, ZONE;

	public static AbilityAgentType fromString(final String s)
		throws CorruptDataException
	{
		switch (s) {
			case "Character": return CHARACTER;
			case "Trap": return TRAP;
			case "Zone": return ZONE;
			default: throw new CorruptDataException("Invalid ability agent type " + s);
		}
	}

	@Override
	public String toString() {
		switch (this) {
			case CHARACTER: return "Character";
			case TRAP: return "Trap";
			case ZONE: return "Zone";
			default: throw new RuntimeException("This cannot happen");
		}
	}
}

