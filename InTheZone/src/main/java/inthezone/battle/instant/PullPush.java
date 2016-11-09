package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.LineOfSight;
import inthezone.battle.PathFinderNode;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PullPush extends InstantEffect {
	private final InstantEffectInfo type;
	private final MapPoint castFrom;
	public final List<List<MapPoint>> paths;
	private final int param;

	private final boolean isFear;

	/**
	 * @param isFear Set to true if this effect was created by the feared status
	 * effect, and the triggers have not been resolved yet.
	 * */
	private PullPush(
		InstantEffectInfo type,
		MapPoint castFrom,
		List<List<MapPoint>> paths,
		boolean isFear
	) {
		super(castFrom);
		this.type = type;
		this.param = type.param;
		this.castFrom = castFrom;
		this.paths = paths;
		this.isFear = isFear;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", type.toString());
		o.put("castFrom", castFrom.getJSON());
		for (List<MapPoint> path : paths) {
			JSONArray pp = new JSONArray();
			for (MapPoint p : path) pp.add(p.getJSON());
			a.add(pp);
		}
		o.put("paths", a);
		return o;
	}

	public static PullPush fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object ocastFrom = json.get("castFrom");
		Object opaths = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (ocastFrom == null) throw new ProtocolException("Missing tile where effect acts from");
		if (opaths == null) throw new ProtocolException("Missing effect paths");

		try {
			InstantEffectInfo type = new InstantEffectInfo((String) okind);
			if (!(type.type == InstantEffectType.PULL | type.type == InstantEffectType.PUSH))
				throw new ProtocolException("Expected push or pull effect");

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

			// isFear is always false here because the triggers have been resolved at
			// this point
			return new PullPush(type, castFrom, paths, false);
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing push/pull effect", e);
		}
	}

	/**
	 * @param isFear Same as for the constructor
	 * */
	public static PullPush getEffect(
		BattleState battle,
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> targets,
		boolean isFear
	) {
		if (info.type == InstantEffectType.PULL) {
			return getPullEffect(battle, info, castFrom, targets, isFear);
		} else if (info.type == InstantEffectType.PUSH) {
			return getPushEffect(battle, info, castFrom, targets, isFear);
		} else {
			throw new RuntimeException(
				"Attempted to build " + info.toString() + " effect with PullPush class");
		}
	}

	/**
	 * @param isFear Same as for the constructor
	 * */
	public static PullPush getPullEffect(
		BattleState battle,
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> targets,
		boolean isFear
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

		return new PullPush(info, castFrom, paths, isFear);
	}

	/**
	 * @param isFear Same as for the constructor
	 * */
	public static PullPush getPushEffect(
		BattleState battle,
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> targets,
		boolean isFear
	) {
		List<List<MapPoint>> paths = new ArrayList<>();

		List<MapPoint> sortedTargets = new ArrayList<>(targets);
		sortedTargets.sort(Collections.reverseOrder(
			Comparator.comparingInt(x -> castFrom.distance(x))));

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

				if (path.size() >= 2) paths.add(path);
			}
		}

		return new PullPush(info, castFrom, paths, isFear);
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

	@Override public List<Targetable> apply(Battle battle) {
		return paths.stream()
			.flatMap(path -> battle.doPushPull(path).stream())
			.collect(Collectors.toList());
	}

	@Override public List<Command> applyComputingTriggers(
		Battle battle, List<Targetable> affected, Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		List<Command> r = new ArrayList<>();

		List<List<List<MapPoint>>> splitPaths = paths.stream()
			.map(path -> battle.battleState.trigger.splitPath(path))
			.collect(Collectors.toList());

		while (!splitPaths.isEmpty()) {
			List<List<MapPoint>> pathSections = new ArrayList<>();
			for (List<List<MapPoint>> sections : splitPaths) {
				if (!sections.isEmpty()) pathSections.add(sections.remove(0));
			}
			splitPaths = splitPaths.stream()
				.filter(x -> !x.isEmpty()).collect(Collectors.toList());

			List<List<MapPoint>> validPathSections = pathSections.stream()
				.filter(x -> x.size() >= 2).collect(Collectors.toList());

			// do the push/pull
			if (!validPathSections.isEmpty()) {
				InstantEffect eff = new PullPush(
					this.type, this.castFrom, validPathSections, false);
				affected.addAll(eff.apply(battle));
				r.add(cmd.apply(eff));
			}

			// do the triggers
			boolean doneContinueTurn = false;
			for (List<MapPoint> path : pathSections) {
				MapPoint loc = path.get(path.size() - 1);
				List<Command> triggers = battle.battleState.trigger.getAllTriggers(loc);
				for (Command c : triggers)
					r.addAll(c.doCmdComputingTriggers(battle, affected));

				if (isFear && !triggers.isEmpty()) {
					battle.battleState.getCharacterAt(loc)
						.ifPresent(c -> r.addAll(c.continueTurnReset(battle)));
					doneContinueTurn = true;
				}
			}

			if (doneContinueTurn) break;
		}

		return r;
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

		return getEffect(battle, type,
			retarget.getOrDefault(castFrom, castFrom), targets, false);
	}
}

