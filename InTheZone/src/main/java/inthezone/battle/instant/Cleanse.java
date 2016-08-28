package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Cleanse implements InstantEffect {
	private final Collection<MapPoint> targets;

	private Cleanse(Collection<MapPoint> targets) {
		this.targets = targets;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.CLEANSE.toString());
		for (MapPoint t : targets) a.add(t.getJSON());
		o.put("targets", a);
		return o;
	}

	public static Cleanse fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object otargets = json.get("targets");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (otargets == null) throw new ProtocolException("Missing effect targets");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.CLEANSE)
				throw new ProtocolException("Expected cleanse effect");

			JSONArray rawTargets = (JSONArray) otargets;
			List<MapPoint> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(MapPoint.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new Cleanse(targets);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing cleanse effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing cleanse effect", e);
		}
	}

	public static Cleanse getEffect(
		Collection<MapPoint> targets
	) {
		return new Cleanse(targets);
	}

	@Override public List<Character> apply(Battle battle) {
		return targets.stream().flatMap(t ->
			battle.doCleanse(t).stream()).collect(Collectors.toList());
	}

	@Override public boolean isComplete() {return true;}
	@Override public boolean complete(List<MapPoint> p) {return true;}
}

