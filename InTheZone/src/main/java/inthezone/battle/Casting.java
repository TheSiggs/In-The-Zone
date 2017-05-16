package inthezone.battle;

import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import org.json.JSONException;
import org.json.JSONObject;

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
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("castFrom", castFrom.getJSON());
		r.put("target", target.getJSON());
		return r;
	}

	public static Casting fromJSON(JSONObject json) throws ProtocolException {
		try {
			final MapPoint castFrom = MapPoint.fromJSON(json.getJSONObject("castFrom"));
			final MapPoint target = MapPoint.fromJSON(json.getJSONObject("target"));

			return new Casting(castFrom, target);

		} catch (JSONException|CorruptDataException e) {
			throw new ProtocolException("Error parsing casting");
		}
	}

	@Override public String toString() {
		return "Casting from " + castFrom + " of " + target;
	}
}

