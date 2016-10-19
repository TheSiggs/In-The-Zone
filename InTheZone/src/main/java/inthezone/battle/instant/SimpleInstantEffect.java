package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SimpleInstantEffect implements InstantEffect {
	private final Collection<MapPoint> targets;
	private final InstantEffectType type;

	private SimpleInstantEffect(
		Collection<MapPoint> targets, InstantEffectType type
	) {
		this.targets = targets;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", type.toString());
		for (MapPoint t : targets) a.add(t.getJSON());
		o.put("targets", a);
		return o;
	}

	public static SimpleInstantEffect fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object otargets = json.get("targets");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (otargets == null) throw new ProtocolException("Missing effect targets");

		try {
			InstantEffectType type = InstantEffectType.fromString((String) okind);
			if (!(type == InstantEffectType.CLEANSE ||
				type == InstantEffectType.DEFUSE ||
				type == InstantEffectType.PURGE)
			) throw new ProtocolException("Expected cleanse, defuse or purge effect");

			JSONArray rawTargets = (JSONArray) otargets;
			List<MapPoint> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(MapPoint.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new SimpleInstantEffect(targets, type);
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing cleanse/purge/defuse effect", e);
		}
	}

	public static SimpleInstantEffect getEffect(
		Collection<MapPoint> targets, InstantEffectType type
	) {
		return new SimpleInstantEffect(targets, type);
	}

	@Override public List<Targetable> apply(Battle battle) {
		switch (type) {
			case CLEANSE: return battle.doCleanse(targets);
			case PURGE: return battle.doPurge(targets);
			case DEFUSE: return battle.doDefuse(targets);
			default: throw new RuntimeException(
				"Invalid simple effect, this cannot happen");
		}
	}

	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		return new SimpleInstantEffect(targets.stream()
			.map(x -> retarget.getOrDefault(x, x))
			.collect(Collectors.toList()), type);
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {return new HashMap<>();}
	@Override public boolean isComplete() {return true;}
	@Override public boolean complete(List<MapPoint> p) {return true;}
}

