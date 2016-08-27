package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Push implements InstantEffect {
	private final MapPoint castFrom;
	private final Collection<MapPoint> targets;
	private final int amount;

	private Push(
		MapPoint castFrom, Collection<MapPoint> targets, int amount
	) {
		this.castFrom = castFrom;
		this.targets = targets;
		this.amount = amount;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.PUSH.toString());
		o.put("amount", amount);
		o.put("castFrom", castFrom.getJSON());
		for (MapPoint t : targets) a.add(t.getJSON());
		o.put("targets", a);
		return o;
	}

	public static Push fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object ocastFrom = json.get("castFrom");
		Object otargets = json.get("targets");
		Object oamount = json.get("amount");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (ocastFrom == null) throw new ProtocolException("Missing tile where effect acts from");
		if (otargets == null) throw new ProtocolException("Missing effect targets");
		if (oamount == null) throw new ProtocolException("Missing effect amount");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.PUSH)
				throw new ProtocolException("Expected push effect");

			Number amount = (Number) oamount;
			MapPoint castFrom = MapPoint.fromJSON((JSONObject) ocastFrom);
			JSONArray rawTargets = (JSONArray) otargets;
			List<MapPoint> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(MapPoint.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new Push(castFrom, targets, amount.intValue());
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing cleanse effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing cleanse effect", e);
		}
	}

	public static InstantEffect getEffect(
		InstantEffectInfo info, MapPoint castFrom, Collection<MapPoint> targets
	) {
		return new Push(castFrom, targets, info.param);
	}

	@Override public List<Character> apply(Battle battle) {
		return targets.stream()
			.sorted(Comparator.comparingInt(x -> castFrom.distance(x)))
			.flatMap(t ->
				battle.doPush(castFrom, t, amount).stream())
			.collect(Collectors.toList());
	}
}


