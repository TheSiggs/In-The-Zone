package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Teleport extends InstantEffect {
	public final int range;
	public final List<Character> affectedCharacters;

	private final List<MapPoint> targets;
	private List<MapPoint> destinations;

	private Teleport(
		List<Character> affectedCharacters,
		int range,
		List<MapPoint> targets,
		List<MapPoint> destinations,
		MapPoint agent
	) {
		super(agent);
		this.affectedCharacters = affectedCharacters;
		this.range = range;
		this.targets = targets;
		this.destinations = destinations;
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

	public static Teleport fromJSON(JSONObject json) throws ProtocolException {
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

			return new Teleport(null, range, targets, destinations, agent);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing teleport effect", e);
		}
	}

	public static Teleport getEffect(
		BattleState battle, InstantEffectInfo info,
		List<MapPoint> targets, MapPoint agent
	) {
		List<Character> affected = targets.stream()
			.flatMap(x -> battle.getCharacterAt(x)
				.map(v -> Stream.of(v)).orElse(Stream.empty()))
			.collect(Collectors.toList());
		return new Teleport(affected, info.param, targets, null, agent);
	}

	@Override public List<Targetable> apply(Battle battle)
		throws CommandException
	{
		if (destinations == null || targets.size() != destinations.size())
			throw new CommandException("Attempted to apply incomplete teleport");

		List<Targetable> r = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			r.addAll(battle.doTeleport(targets.get(i), destinations.get(i)));
		}

		return r;
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		Battle battle, Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		List<ExecutedCommand> r = new ArrayList<>();

		r.add(new ExecutedCommand(cmd.apply(this), apply(battle)));

		for (MapPoint p : destinations) {
			List<Command> triggers = battle.battleState.trigger.getAllTriggers(p);
			for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(battle));
		}

		return r;
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		Map<MapPoint, MapPoint> r = new HashMap<>();

		for (int i = 0; i < targets.size(); i++) {
			r.put(targets.get(i), destinations.get(i));
		}
		return r;
	}

	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		List<MapPoint> newTargets =
			targets.stream().map(t -> retarget.getOrDefault(t, t))
			.collect(Collectors.toList());

		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.TELEPORT, range),
			newTargets, retarget.getOrDefault(agent, agent));
	}

	@Override public boolean isComplete() {return !(destinations == null);}
	@Override public boolean complete(BattleState battle, List<MapPoint> ps) {
		destinations = new ArrayList<>();
		destinations.addAll(ps);
		return targets.size() == destinations.size();
	}
}

