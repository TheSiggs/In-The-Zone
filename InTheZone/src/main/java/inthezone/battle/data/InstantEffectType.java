package inthezone.battle.data;

import isogame.engine.CorruptDataException;

public enum InstantEffectType {
	CLEANSE, DEFUSE, PURGE, PUSH, PULL, SCAN, TELEPORT, OBSTACLES;

	public static InstantEffectType fromString(String s)
		throws CorruptDataException
	{
		switch(s.toLowerCase()) {
			case "cleanse": return CLEANSE;
			case "defuse": return DEFUSE;
			case "purge": return PURGE;
			case "push": return PUSH;
			case "pull": return PULL;
			case "scan": return SCAN;
			case "teleport": return TELEPORT;
			case "obstacles": return OBSTACLES;
			default:
				throw new CorruptDataException("Invalid instant effect " + s);
		}
	}
}

