package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
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
	private final List<List<MapPoint>> paths;

	private Push(
		MapPoint castFrom, List<List<MapPoint>> paths
	) {
		this.castFrom = castFrom;
		this.paths = paths;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.PUSH.toString());
		o.put("castFrom", castFrom.getJSON());
		for (List<MapPoint> path : paths) {
			JSONArray pp = new JSONArray();
			for (MapPoint p : path) pp.add(p.getJSON());
			a.add(pp);
		}
		o.put("paths", a);
		return o;
	}

	public static Push fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object ocastFrom = json.get("castFrom");
		Object opaths = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (ocastFrom == null) throw new ProtocolException("Missing tile where effect acts from");
		if (opaths == null) throw new ProtocolException("Missing effect paths");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.PUSH)
				throw new ProtocolException("Expected push effect");

			MapPoint castFrom = MapPoint.fromJSON((JSONObject) ocastFrom);
			JSONArray rawPaths = (JSONArray) opaths;
			List<List<MapPoint>> paths = new ArrayList<>();
			for (int i = 0; i < rawPaths.size(); i++) {
				List<MapPoint> path = new ArrayList<>();
				JSONArray rawPath = (JSONArray) rawPaths.get(i);
				for (int j = 0; j < rawPath.size(); j++) {
					path.add(MapPoint.fromJSON((JSONObject) rawPath.get(j)));
				}

				paths.add(path);
			}
			return new Push(castFrom, paths);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing push effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing push effect", e);
		}
	}

	public static Push getEffect(
		BattleState battle,
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> targets
	) {
		List<List<MapPoint>> paths = new ArrayList<>();

		List<MapPoint> sortedTargets = new ArrayList<>(targets);
		sortedTargets.sort(Comparator.comparingInt(x -> castFrom.distance(x)));

		for (MapPoint t : sortedTargets) {
			if (castFrom.x == t.x || castFrom.y == t.y) {
				MapPoint dp = t.subtract(castFrom).normalise();
				List<MapPoint> path = new ArrayList<>();

				MapPoint x = t;
				path.add(x);
				for (int i = 0; i < info.param; i++) {
					MapPoint z = x.add(dp);
					if (!battle.isSpaceFree(z)) break; else {
						x = z;
						path.add(x);
					}
				}

				paths.add(path);
			}
		}

		return new Push(castFrom, paths);
	}

	@Override public List<Character> apply(Battle battle) {
		return paths.stream()
			.filter(p -> p.size() >= 2)
			.flatMap(path -> battle.doPush(path).stream())
			.collect(Collectors.toList());
	}
}

