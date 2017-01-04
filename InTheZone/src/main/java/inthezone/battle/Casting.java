package inthezone.battle;

import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import org.json.simple.JSONObject;

/**
 * Represents a single casting or recasting of an ability.
 * */
public class Casting implements HasJSONRepresentation {
	public final MapPoint castFrom;
	public final MapPoint target;

	public Casting(MapPoint castFrom, MapPoint target) {
		this.castFrom = castFrom;
		this.target = target;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("castFrom", castFrom.getJSON());
		r.put("target", target.getJSON());
		return r;
	}

	public static Casting fromJSON(JSONObject json) throws ProtocolException {
		Object ocastFrom = json.get("castFrom");
		Object otarget = json.get("target");

		if (ocastFrom == null) throw new ProtocolException("Missing castFrom in casting");
		if (otarget == null) throw new ProtocolException("Missing target in casting");

		try {
			return new Casting(
				MapPoint.fromJSON((JSONObject) ocastFrom),
				MapPoint.fromJSON((JSONObject) otarget));
		} catch (ClassCastException|CorruptDataException e) {
			throw new ProtocolException("Error parsing casting");
		}
	}

	@Override public String toString() {
		return "Casting from " + castFrom + " of " + target;
	}
}

