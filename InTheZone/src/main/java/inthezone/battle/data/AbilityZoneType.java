package inthezone.battle.data;

import isogame.engine.CorruptDataException;

public enum AbilityZoneType {
	NONE, ZONE, BOUND_ZONE;

	@Override public String toString() {
		switch (this) {
			case NONE: return "None";
			case ZONE: return "Zone";
			case BOUND_ZONE: return "BoundZone";
			default: throw new RuntimeException("This can't happen");
		}
	}

	public static AbilityZoneType fromString(String s)
		throws CorruptDataException
	{
		switch (s.toLowerCase()) {
			case "none": return NONE;
			case "zone": return ZONE;
			case "boundzone": return BOUND_ZONE;
			default: throw new CorruptDataException("Invalid zone type " + s);
		}
	}
}

