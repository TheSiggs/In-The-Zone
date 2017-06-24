package inthezone.battle.instant;

import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.Targetable;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;

public class Teleport extends InstantEffect {
	public final int range;
	public final List<Character> affectedCharacters = new ArrayList<>();

	private final List<MapPoint> targets = new ArrayList<>();
	private List<MapPoint> destinations = new ArrayList<>();

	private Teleport(
		final Optional<List<Character>> affectedCharacters,
		final int range,
		final Optional<List<MapPoint>> targets,
		final Optional<List<MapPoint>> destinations,
		final MapPoint agent
	) {
		super(agent);

		affectedCharacters.ifPresent(a -> {
			this.affectedCharacters.addAll(a);
			this.targets.addAll(a.stream()
				.map(c -> c.getPos()).collect(Collectors.toList()));
		});

		targets.ifPresent(t -> {
			this.targets.clear();
			this.targets.addAll(t);
		});

		destinations.ifPresent(d -> this.destinations.addAll(d));

		this.range = range;
	}

	public List<MapPoint> getDestinations() {
		return destinations;
	}

	@Override public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.TELEPORT.toString());
		o.put("agent", agent.getJSON());
		o.put("range", range);

		final JSONArray ts = new JSONArray();
		final JSONArray ds = new JSONArray();
		for (MapPoint t : targets) ts.put(t.getJSON());
		for (MapPoint d : destinations) ds.put(d.getJSON());

		o.put("targets", ts);
		o.put("destinations", ds);
		return o;
	}

	public static Teleport fromJSON(final JSONObject json) throws ProtocolException {
		try {
			final InstantEffectType kind = (new InstantEffectInfo(json.getString("kind"))).type;
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final int range = json.getInt("range");
			final JSONArray rawTargets = json.getJSONArray("targets");
			final JSONArray rawDestinations = json.getJSONArray("destinations");

			if (kind != InstantEffectType.TELEPORT)
				throw new ProtocolException("Expected teleport effect");

			final List<MapPoint> targets = new ArrayList<>();
			final List<MapPoint> destinations = new ArrayList<>();

			for (int i = 0; i < rawTargets.length(); i++) {
				targets.add(MapPoint.fromJSON(rawTargets.getJSONObject(i)));
			}
			for (int i = 0; i < rawDestinations.length(); i++) {
				destinations.add(MapPoint.fromJSON(rawDestinations.getJSONObject(i)));
			}

			return new Teleport(Optional.empty(), range, Optional.of(targets),
				Optional.of(destinations), agent);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing teleport effect", e);
		}
	}

	public static Teleport getEffect(
		final BattleState battle, final InstantEffectInfo info,
		final List<MapPoint> targets, final MapPoint agent
	) {
		final List<Character> affected = targets.stream()
			.flatMap(x -> battle.getCharacterAt(x)
				.map(v -> Stream.of(v)).orElse(Stream.empty()))
			.collect(Collectors.toList());
		return new Teleport(Optional.of(affected), info.param,
			Optional.empty(), Optional.empty(), agent);
	}

	@Override public List<Targetable> apply(final Battle battle)
		throws CommandException
	{
		if (destinations == null || targets.size() != destinations.size())
			throw new CommandException("Attempted to apply incomplete teleport");

		final List<Targetable> r = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			r.addAll(battle.doTeleport(targets.get(i), destinations.get(i)));
		}

		return r;
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		final Battle battle, final Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		final List<ExecutedCommand> r = new ArrayList<>();

		r.add(new ExecutedCommand(cmd.apply(this), apply(battle)));

		for (MapPoint p : destinations) {
			List<Command> triggers = battle.battleState.trigger.getAllTriggers(p);
			for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(battle));
		}

		return r;
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		final Map<MapPoint, MapPoint> r = new HashMap<>();

		for (int i = 0; i < targets.size(); i++) {
			r.put(targets.get(i), destinations.get(i));
		}
		return r;
	}

	@Override public InstantEffect retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		final List<MapPoint> newTargets =
			targets.stream().map(t -> retarget.getOrDefault(t, t))
			.collect(Collectors.toList());

		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.TELEPORT, range),
			newTargets, retarget.getOrDefault(agent, agent));
	}

	@Override public boolean isComplete() {
		return destinations.size() == targets.size();
	}

	@Override public boolean complete(
		final BattleState battle, final List<MapPoint> ps
	) {
		destinations = new ArrayList<>();
		destinations.addAll(ps);
		return targets.size() == destinations.size();
	}
}

