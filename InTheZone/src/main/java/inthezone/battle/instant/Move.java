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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		o.put("kind", InstantEffectType.MOVE.toString());
		o.put("range", range);
		o.put("agent", agent.getJSON());
		for (List<MapPoint> path : paths) {
			JSONArray pp = new JSONArray();
			for (MapPoint p : path) pp.add(p.getJSON());
			a.add(pp);
		}
		o.put("paths", a);
		return o;
	}

	public static Move fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object orange = json.get("range");
		Object oagent = json.get("agent");
		Object opaths = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (orange == null) throw new ProtocolException("Missing effect range");
		if (oagent == null) throw new ProtocolException("Missing effect agent");
		if (opaths == null) throw new ProtocolException("Missing effect paths");

		try {
			InstantEffectInfo type = new InstantEffectInfo((String) okind);
			if (type.type != InstantEffectType.MOVE)
				throw new ProtocolException("Expected move effect");

			JSONArray rawPaths = (JSONArray) opaths;
			MapPoint agent = MapPoint.fromJSON(((JSONObject) oagent));
			List<List<MapPoint>> paths = new ArrayList<>();
			for (int i = 0; i < rawPaths.size(); i++) {
				List<MapPoint> path = new ArrayList<>();
				JSONArray rawPath = (JSONArray) rawPaths.get(i);
				for (int j = 0; j < rawPath.size(); j++) {
					path.add(MapPoint.fromJSON((JSONObject) rawPath.get(j)));
				}

				paths.add(path);
			}

			return new Move(null, ((Number) orange).intValue(), paths, agent);
		} catch (ClassCastException|CorruptDataException  e) {
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
			battle.doMove(path);
		}

		return affected;
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		Battle battle, Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		List<ExecutedCommand> r = new ArrayList<>();

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

