package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Move extends InstantEffect {
	public List<List<MapPoint>> paths;
	public final List<Character> affectedCharacters;
	public final int range;

	private Move(
		List<Character> affectedCharacters,
		int range,
		List<List<MapPoint>> paths,
		MapPoint agent
	) {
		super(agent);
		this.affectedCharacters = affectedCharacters;
		this.range = range;
		this.paths = paths;
	}

	@Override public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		final JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.MOVE.toString());
		o.put("range", range);
		o.put("agent", agent.getJSON());
		for (List<MapPoint> path : paths) {
			final JSONArray pp = new JSONArray();
			for (MapPoint p : path) pp.put(p.getJSON());
			a.put(pp);
		}
		o.put("paths", a);
		return o;
	}

	public static Move fromJSON(JSONObject json) throws ProtocolException {
		try {
			final InstantEffectType kind = (new InstantEffectInfo(json.getString("kind"))).type;
			int range = json.getInt("range");
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final JSONArray rawPaths = json.getJSONArray("paths");

			if (kind != InstantEffectType.MOVE)
				throw new ProtocolException("Expected move effect");

			final List<List<MapPoint>> paths = new ArrayList<>();
			for (int i = 0; i < rawPaths.length(); i++) {
				final List<MapPoint> path = new ArrayList<>();
				final JSONArray rawPath = rawPaths.getJSONArray(i);
				for (int j = 0; j < rawPath.length(); j++) {
					path.add(MapPoint.fromJSON(rawPath.getJSONObject(j)));
				}

				paths.add(path);
			}

			return new Move(null, range, paths, agent);
		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing move effect", e);
		}
	}

	public static Move getEffect(
		BattleState battle,
		InstantEffectInfo info,
		Collection<MapPoint> targets,
		MapPoint agent
	) {
		List<Character> affected = targets.stream()
			.flatMap(x -> battle.getCharacterAt(x)
				.map(v -> Stream.of(v)).orElse(Stream.empty()))
			.collect(Collectors.toList());
		return new Move(affected, info.param, null, agent);
	}

	@Override public List<Targetable> apply(Battle battle) {
		final List<Targetable> affected = new ArrayList<>();

		for (List<MapPoint> path : paths) {
			battle.battleState.getCharacterAt(path.get(0)).ifPresent(c -> affected.add(c));
			battle.doMove(path, false);
		}

		return affected;
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

			// do the move
			if (!validPathSections.isEmpty()) {
				InstantEffect eff = new Move(
					null, this.range, validPathSections, agent);
				r.add(new ExecutedCommand(cmd.apply(eff), eff.apply(battle)));
			}

			// do the triggers
			for (List<MapPoint> path : pathSections) {
				MapPoint loc = path.get(path.size() - 1);
				List<Command> triggers = battle.battleState.trigger.getAllTriggers(loc);
				for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(battle));
			}
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

		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.MOVE, range),
			targets, retarget.getOrDefault(agent, agent));
	}

	@Override public boolean isComplete() {return !(paths == null);}
	@Override public boolean complete(BattleState battle, List<MapPoint> ps) {
		if (ps == null || affectedCharacters.size() != ps.size()) return false;

		paths = new ArrayList<>();

		for (int i = 0; i < ps.size(); i++) {
			Character c = affectedCharacters.get(i);
			List<MapPoint> path = battle.findPath(c.getPos(), ps.get(i), c.player);
			if (!path.isEmpty()) paths.add(path);
		}

		return true;
	}
}

