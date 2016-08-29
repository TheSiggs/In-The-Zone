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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Pull implements InstantEffect {
	private final MapPoint castFrom;
	public final List<List<MapPoint>> paths;
	private final int param;

	private Pull(
		int param,
		MapPoint castFrom,
		List<List<MapPoint>> paths
	) {
		this.param = param;
		this.castFrom = castFrom;
		this.paths = paths;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.PULL.toString());
		o.put("param", param);
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
		Object oparam = json.get("param");
		Object ocastFrom = json.get("castFrom");
		Object opaths = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (oparam == null) throw new ProtocolException("Missing effect parameter");
		if (ocastFrom == null) throw new ProtocolException("Missing tile where effect acts from");
		if (opaths == null) throw new ProtocolException("Missing effect paths");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.PULL)
				throw new ProtocolException("Expected pull effect");

			MapPoint castFrom = MapPoint.fromJSON((JSONObject) ocastFrom);
			Number param = (Number) oparam;
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
			return new Pull(param.intValue(), castFrom, paths);
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
			List<MapPoint> path1 = getPullPath(battle, t, castFrom, info.param, true);
			List<MapPoint> path2 = getPullPath(battle, t, castFrom, info.param, false);

			List<MapPoint> path = path1.size() > path2.size()?  path1 : path2; 
			if (path.size() > 0) paths.add(path);
		}

		return new Pull(info.param, castFrom, paths);
	}

	private static List<MapPoint> getPullPath(
		BattleState battle, MapPoint from, MapPoint to, int limit, boolean bias
	) {
		List<MapPoint> los = LineOfSight.getLOS(from, to, bias);
		List<MapPoint> path = new ArrayList<>();

		if (los == null || los.size() < 1) return path;

		MapPoint last = los.remove(0);
		path.add(last);
		while (
			path.size() <= limit &&
			los.size() > 0 &&
			battle.isSpaceFree(los.get(0)) &&
			PathFinderNode.canTraverseBoundary(
				last, los.get(0), battle.terrain.terrain)
		) {
			last = los.remove(0);
			path.add(last);
		}

		return path.size() >= 2? path : new ArrayList<>();
	}

	@Override public List<Character> apply(Battle battle) {
		return paths.stream()
			.flatMap(path -> battle.doPushPull(path).stream())
			.collect(Collectors.toList());
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		Map<MapPoint, MapPoint> r = new HashMap<>();

		for (List<MapPoint> path : paths) {
			r.put(path.get(0), path.get(path.size() - 1));
		}
		return r;
	}

	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		Collection<MapPoint> targets =
			paths.stream().map(p -> retarget.getOrDefault(p.get(0), p.get(0)))
			.collect(Collectors.toList());

		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.PULL, param),
			castFrom, targets);
	}

	@Override public boolean isComplete() {return true;}
	@Override public boolean complete(List<MapPoint> p) {return true;}
}

