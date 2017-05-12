package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.Player;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", type.toString());
		o.put("castFrom", castFrom.getJSON());
		for (List<MapPoint> path : paths) {
			JSONArray pp = new JSONArray();
			for (MapPoint p : path) pp.put(p.getJSON());
			a.put(pp);
		}
		o.put("paths", a);
		return o;
	}

	public static PullPush fromJSON(JSONObject json) throws ProtocolException {
		try {
			final InstantEffectInfo kind = new InstantEffectInfo(json.getString("kind"));
			final MapPoint castFrom = MapPoint.fromJSON(json.getJSONObject("castFrom"));
			final JSONArray rawPaths = json.getJSONArray("paths");

			if (!(kind.type == InstantEffectType.PULL || kind.type == InstantEffectType.PUSH))
				throw new ProtocolException("Expected push or pull effect");

			final List<List<MapPoint>> paths = new ArrayList<>();
			for (int i = 0; i < rawPaths.length(); i++) {
				final List<MapPoint> path = new ArrayList<>();
				final JSONArray rawPath = rawPaths.getJSONArray(i);
				for (int j = 0; j < rawPath.length(); j++) {
					path.add(MapPoint.fromJSON(rawPath.getJSONObject(j)));
				}

				paths.add(path);
			}

			// isFear is always false here because the triggers have been resolved at
			// this point
			return new PullPush(kind, castFrom, paths, false);

		} catch (JSONException|CorruptDataException  e) {
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
		final List<List<MapPoint>> paths = new ArrayList<>();

		final List<MapPoint> sortedTargets = new ArrayList<>(targets);
		sortedTargets.sort(Comparator.comparingInt(x -> castFrom.distance(x)));

		final Set<MapPoint> cleared = new HashSet<>();
		final Set<MapPoint> occupied = new HashSet<>();

		for (MapPoint t : sortedTargets) {
			final MapPoint to = getTo(info.type, t, castFrom, info.param);
			final List<MapPoint> path1 = getPullPath(battle, t, to, occupied, cleared, info.param, true);
			final List<MapPoint> path2 = getPullPath(battle, t, to, occupied, cleared, info.param, false);

			final List<MapPoint> path = path1.size() > path2.size()?  path1 : path2; 
			if (path.size() > 0) {
				paths.add(path);
				cleared.add(path.get(0));
				occupied.add(path.get(path.size() - 1));
			}
		}

		return new PullPush(info, castFrom, paths, isFear);
	}

	private static MapPoint getTo(
		InstantEffectType type, MapPoint from, MapPoint castFrom, int range
	) {
		if (type == InstantEffectType.PULL) {
			return castFrom;
		} else if (type == InstantEffectType.PUSH) {
			return from.addScale(from.subtract(castFrom), range);
		} else {
			throw new RuntimeException("Invalid effect type, expected PUSH or PULL");
		}
	}

	private static List<MapPoint> getPullPath(
		BattleState battle, MapPoint from, MapPoint to,
		Set<MapPoint> occupied, Set<MapPoint> cleared,
		int limit, boolean bias
	) {
		final List<MapPoint> los = LineOfSight.getLOS(from, to, bias);
		final List<MapPoint> path = new ArrayList<>();

		if (los == null || los.isEmpty()) return path;

		// Stop the path short at the first impassable object.
		final Player player = battle.getCharacterAt(from).map(c -> c.player).get();
		if (player == null)
			throw new RuntimeException("Cannot find character to push/pull!");

		MapPoint last = los.remove(0);
		path.add(last);
		while (
			battle.pathCost(path) < limit &&
			los.size() > 0 &&
			!occupied.contains(los.get(0)) &&
			(battle.canMoveThrough(los.get(0), player) || cleared.contains(los.get(0))) &&
			PathFinderNode.canTraverseBoundary(
				last, los.get(0), battle.terrain.terrain)
		) {
			last = los.remove(0);
			path.add(last);
		}

		// Trim invalid destinations off the end of the path.
		while (
			path.size() > 0 &&
				(occupied.contains(path.get(path.size() - 1)) ||
					!(battle.isSpaceFree(path.get(path.size() - 1)) ||
						cleared.contains(path.get(path.size() - 1))))
		) {
			path.remove(path.size() - 1);
		}

		return path.size() >= 2? path : new ArrayList<>();
	}

	@Override public List<Targetable> apply(Battle battle) {
		return paths.stream()
			.flatMap(path -> battle.doPushPull(path).stream())
			.collect(Collectors.toList());
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		Battle battle, Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		List<ExecutedCommand> r = new ArrayList<>();

		List<List<List<MapPoint>>> splitPaths = paths.stream()
			.map(path -> {
				if (path.size() == 0) return new ArrayList<List<MapPoint>>(); else {
					Character agent =
						battle.battleState.getCharacterAt(path.get(0)).orElse(null);
					if (agent == null) return new ArrayList<List<MapPoint>>(); else {
						return battle.battleState.trigger.splitPath(agent, path);
					}
				}
			}).collect(Collectors.toList());

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
				r.add(new ExecutedCommand(cmd.apply(eff), eff.apply(battle)));
			}

			// do the triggers
			boolean doneContinueTurn = false;
			for (List<MapPoint> path : pathSections) {
				MapPoint loc = path.get(path.size() - 1);
				List<Command> triggers = battle.battleState.trigger.getAllTriggers(loc);
				for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(battle));

				if (isFear && !triggers.isEmpty()) {
					Optional<Character> oc = battle.battleState.getCharacterAt(loc);
					if (oc.isPresent()) {
						List<Command> cont = oc.get().continueTurnReset(battle);
						for (Command c : cont) r.addAll(c.doCmdComputingTriggers(battle));
						doneContinueTurn = true;
					}
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

