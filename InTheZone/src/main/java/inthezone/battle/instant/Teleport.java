package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Teleport implements InstantEffect {
	public final int range;
	public final List<Character> affectedCharacters;

	private final List<MapPoint> targets;
	private List<MapPoint> destinations;

	private Teleport(
		List<Character> affectedCharacters,
		int range,
		List<MapPoint> targets,
		List<MapPoint> destinations
	) {
		this.affectedCharacters = affectedCharacters;
		this.range = range;
		this.targets = targets;
		this.destinations = destinations;
	}

	public List<MapPoint> getDestinations() {
		return destinations;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.TELEPORT.toString());
		o.put("range", range);

		JSONArray ts = new JSONArray();
		JSONArray ds = new JSONArray();
		for (MapPoint t : targets) ts.add(t.getJSON());
		for (MapPoint d : destinations) ds.add(d.getJSON());

		o.put("targets", ts);
		o.put("destinations", ds);
		return o;
	}

	public static Teleport fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object orange = json.get("range");
		Object otargets = json.get("targets");
		Object odestinations = json.get("destinations");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (orange == null) throw new ProtocolException("Missing effect range");
		if (otargets == null) throw new ProtocolException("Missing effect targets");
		if (odestinations == null) throw new ProtocolException("Missing effect destinations");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.TELEPORT)
				throw new ProtocolException("Expected teleport effect");

			List<MapPoint> targets = new ArrayList<>();
			List<MapPoint> destinations = new ArrayList<>();

			JSONArray rawTargets = (JSONArray) otargets;
			JSONArray rawDestinations = (JSONArray) odestinations;
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(MapPoint.fromJSON((JSONObject) rawTargets.get(i)));
			}
			for (int i = 0; i < rawDestinations.size(); i++) {
				destinations.add(MapPoint.fromJSON((JSONObject) rawDestinations.get(i)));
			}

			Number range = (Number) orange;
			return new Teleport(null, range.intValue(), targets, destinations);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing teleport effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing teleport effect", e);
		}
	}

	public static Teleport getEffect(
		BattleState battle, InstantEffectInfo info, List<MapPoint> targets
	) {
		List<Character> affected = targets.stream()
			.flatMap(x -> battle.getCharacterAt(x)
				.map(v -> Stream.of(v)).orElse(Stream.empty()))
			.collect(Collectors.toList());
		return new Teleport(affected, info.param, targets, null);
	}

	@Override public List<Character> apply(Battle battle)
		throws CommandException
	{
		if (destinations == null || targets.size() != destinations.size())
			throw new CommandException("Attempted to apply incomplete teleport");

		List<Character> r = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			r.addAll(battle.doTeleport(targets.get(i), destinations.get(i)));
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
			new InstantEffectInfo(InstantEffectType.TELEPORT, range), newTargets);
	}

	@Override public boolean isComplete() {return !(destinations == null);}
	@Override public boolean complete(List<MapPoint> ps) {
		destinations = new ArrayList<>();
		destinations.addAll(ps);
		return destinations != null && targets.size() == destinations.size();
	}
}


