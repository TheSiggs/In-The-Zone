package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import org.json.JSONException;
import org.json.JSONObject;

public class Range implements HasJSONRepresentation {
	public final int range;
	public final int radius;
	public final boolean piercing; // ignore for now
	public final TargetMode targetMode;
	public final int nTargets;
	public final boolean los;

	public Range(
		int range, int radius,
		boolean piercing,
		TargetMode targetMode,
		int nTargets, boolean los
	) {
		this.range = range;
		this.piercing = piercing;
		this.radius = radius;
		this.targetMode = targetMode;
		this.nTargets = nTargets;
		this.los = los;
	}

	@Override
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("range", range);
		r.put("piercing", piercing);
		r.put("radius", radius);
		r.put("targetMode", targetMode.toString());
		r.put("nTargets", nTargets);
		r.put("los", los);
		return r;
	}

	public static Range fromJSON(JSONObject json)
		throws CorruptDataException
	{
		try {
			final int range = json.getInt("range");
			final boolean piercing = json.getBoolean("piercing");
			final int radius = json.getInt("radius");
			final String targetMode = json.getString("targetMode");
			final int nTargets = json.getInt("nTargets");
			final boolean los = json.getBoolean("los");

			return new Range(range, radius, piercing,
				new TargetMode(targetMode), nTargets, los);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing range, " + e.getMessage(), e);
		}
	}
}

