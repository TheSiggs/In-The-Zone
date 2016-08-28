package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.LineOfSight;
import inthezone.battle.PathFinderNode;
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

public class Pull implements InstantEffect {
	private final MapPoint castFrom;
	private final List<List<MapPoint>> paths;

	private Pull(
		MapPoint castFrom, List<List<MapPoint>> paths
	) {
		this.castFrom = castFrom;
		this.paths = paths;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.PULL.toString());
		o.put("castFrom", castFrom.getJSON());
		for (List<MapPoint> path : paths) {
			JSONArray pp = new JSONArray();
			for (MapPoint p : path) pp.add(p.getJSON());
			a.add(pp);
		}
		o.put("paths", a);
		return o;
	}

	public static Pull fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object ocastFrom = json.get("castFrom");
		Object opaths = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (ocastFrom == null) throw new ProtocolException("Missing tile where effect acts from");
		if (opaths == null) throw new ProtocolException("Missing effect paths");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.PULL)
				throw new ProtocolException("Expected pull effect");

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
			return new Pull(castFrom, paths);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing pull effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing pull effect", e);
		}
	}

	public static Pull getEffect(
		BattleState battle,
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> targets
	) {
		List<List<MapPoint>> paths = new ArrayList<>();

		List<MapPoint> sortedTargets = new ArrayList<>(targets);
		sortedTargets.sort(Comparator.comparingInt(x -> castFrom.distance(x)));

		for (MapPoint t : sortedTargets) {
			List<MapPoint> path1 = getPullPath(battle, t, castFrom, true);
			List<MapPoint> path2 = getPullPath(battle, t, castFrom, false);

			if (path1.size() > path2.size()) paths.add(path1); else paths.add(path2);
		}

		return new Pull(castFrom, paths);
	}

	private static List<MapPoint> getPullPath(
		BattleState battle, MapPoint from, MapPoint to, boolean bias
	) {
		List<MapPoint> los = LineOfSight.getLOS(from, to, bias);
		List<MapPoint> path = new ArrayList<>();

		if (los == null || los.size() < 1) return path;

		MapPoint last = los.remove(0);
		while (
			los.size() > 0 &&
			battle.isSpaceFree(los.get(0)) &&
			PathFinderNode.canTraverseBoundary(
				last, los.get(0), battle.terrain.terrain)
		) {
			last = los.remove(0);
			path.add(last);
		}

		return path;
	}

	@Override public List<Character> apply(Battle battle) {
		return paths.stream()
			.filter(p -> p.size() >= 2)
			.flatMap(path -> battle.doPushPull(path).stream())
			.collect(Collectors.toList());
	}

	@Override public boolean isComplete() {return true;}
	@Override public boolean complete(List<MapPoint> p) {return true;}
}

