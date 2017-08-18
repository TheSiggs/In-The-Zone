package inthezone.battle.instant;

import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.Targetable;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;

public class Move extends InstantEffect {
	public final List<List<MapPoint>> paths = new ArrayList<>();
	private final List<Character> affectedCharacters = new ArrayList<>();

	public final List<CharacterFrozen> getAffectedCharacters() {
		return affectedCharacters.stream()
			.map(c -> c.freeze()).collect(Collectors.toList());
	}

	public final int range;

	private boolean isComplete;

	private Move(
		final Optional<Set<Character>> affectedCharacters,
		final int range,
		final Optional<List<List<MapPoint>>> paths,
		final MapPoint agent,
		final boolean effectComplete
	) {
		super(agent);
		this.range = range;

		isComplete = effectComplete;

		affectedCharacters.ifPresent(a -> {
			this.affectedCharacters.addAll(a);
		});

		paths.ifPresent(p -> {
			this.paths.addAll(p);
		});
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

	public static Move fromJSON(final JSONObject json) throws ProtocolException {
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

			return new Move(Optional.empty(), range, Optional.of(paths), agent, true);
		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing move effect", e);
		}
	}

	public static Move getEffect(
		final BattleState battle,
		final InstantEffectInfo info,
		final Set<MapPoint> targets,
		final MapPoint agent
	) {
		final Set<Character> affected = targets.stream()
			.flatMap(x -> battle.getCharacterAt(x)
				.map(v -> Stream.of(v)).orElse(Stream.empty()))
			.collect(Collectors.toSet());

		return new Move(
			Optional.of(affected), info.param, Optional.empty(), agent, false);
	}

	@Override public List<Targetable> apply(final Battle battle) {
		final List<Targetable> affected = new ArrayList<>();

		for (List<MapPoint> path : paths) {
			battle.battleState.getCharacterAt(path.get(0))
				.ifPresent(c -> affected.add(c));
			battle.doMove(path, false);
		}

		return affected;
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		final Battle battle, final Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		final List<ExecutedCommand> r = new ArrayList<>();

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
			final List<List<MapPoint>> pathSections = new ArrayList<>();
			for (List<List<MapPoint>> sections : splitPaths) {
				if (!sections.isEmpty()) pathSections.add(sections.remove(0));
			}
			splitPaths = splitPaths.stream()
				.filter(x -> !x.isEmpty()).collect(Collectors.toList());

			final List<List<MapPoint>> validPathSections = pathSections.stream()
				.filter(x -> x.size() >= 2).collect(Collectors.toList());

			// do the move
			if (!validPathSections.isEmpty()) {
				InstantEffect eff = new Move(
					Optional.empty(), this.range,
					Optional.of(validPathSections), agent, true);
				r.add(new ExecutedCommand(cmd.apply(eff), eff.apply(battle)));
			}

			// do the triggers
			for (List<MapPoint> path : pathSections) {
				final MapPoint loc = path.get(path.size() - 1);
				final List<Command> triggers =
					battle.battleState.trigger.getAllTriggers(loc);
				for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(battle));
			}
		}

		return r;
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		final Map<MapPoint, MapPoint> r = new HashMap<>();

		for (List<MapPoint> path : paths) {
			r.put(path.get(0), path.get(path.size() - 1));
		}
		return r;
	}

	@Override public InstantEffect retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		final Set<MapPoint> targets =
			paths.stream().map(p -> retarget.getOrDefault(p.get(0), p.get(0)))
			.collect(Collectors.toSet());

		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.MOVE, range),
			targets, retarget.getOrDefault(agent, agent));
	}

	@Override public boolean isComplete() {return isComplete;}

	@Override public boolean complete(
		final BattleState battle, final List<MapPoint> ps
	) {
		if (ps == null || affectedCharacters.size() != ps.size()) return false;

		for (int i = 0; i < ps.size(); i++) {
			final Character c = affectedCharacters.get(i);
			final List<MapPoint> path =
				battle.findPath(c.getPos(), ps.get(i), c.player);
			if (!path.isEmpty()) paths.add(path);
		}

		isComplete = true;
		return true;
	}
}

