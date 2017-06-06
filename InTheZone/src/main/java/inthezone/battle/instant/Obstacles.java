package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Obstacles extends InstantEffect {
	private final Collection<MapPoint> placements = new ArrayList<>();
	private final String abilityName;

	private Obstacles(
		Collection<MapPoint> placements, String abilityName, MapPoint agent
	) {
		super(agent);
		this.abilityName = abilityName;
		this.placements.addAll(placements);
	}

	@Override public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.OBSTACLES.toString());
		o.put("agent", agent.getJSON());
		o.put("ability", abilityName);

		final JSONArray a = new JSONArray();
		for (MapPoint p : placements) a.put(p.getJSON());
		o.put("placements", a);
		return o;
	}

	public static Obstacles fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final InstantEffectType kind = (new InstantEffectInfo(json.getString("kind"))).type;
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final String abilityName = json.getString("ability");
			final JSONArray rawPlacements = json.getJSONArray("placements");

			if (kind != InstantEffectType.OBSTACLES)
				throw new ProtocolException("Expected obstacles effect");

			final Collection<MapPoint> placements = new ArrayList<>();
			for (int i = 0; i < rawPlacements.length(); i++) {
				placements.add(MapPoint.fromJSON(rawPlacements.getJSONObject(i)));
			}

			return new Obstacles(placements, abilityName, agent);

		} catch (JSONException|CorruptDataException e) {
			throw new ProtocolException("Error parsing obstacles effect", e);
		}
	}

	public static Obstacles getEffect(
		MapPoint agent,
		String abilityName,
		Collection<MapPoint> targets
	) {
		return new Obstacles(targets, abilityName, agent);
	}

	private final List<MapPoint> constructedObjects = new ArrayList<>();

	@Override public List<Targetable> apply(Battle battle) throws CommandException {
		final Optional<AbilityInfo> abilityData;

		if (abilityName == null) {
			abilityData = Optional.empty();
		} else {
			abilityData = battle.battleState.getCharacterAt(agent)
				.flatMap(c -> Stream.concat(Stream.of(c.basicAbility), c.abilities.stream())
					.filter(a -> a.info.name.equals(abilityName)).findFirst())
				.flatMap(a -> a.getNext(
					battle.battleState.hasMana(agent), 0))
				.map(a -> a.info);
		}

		final List<Targetable> r = battle.doObstacles(abilityData, placements.stream()
			.filter(p -> battle.battleState.isSpaceFree(p))
			.collect(Collectors.toList()));
		constructedObjects.clear();
		for (Targetable t : r) constructedObjects.add(t.getPos());
		return r;
	}

	@Override public List<MapPoint> getConstructed() {return constructedObjects;}

	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		return new Obstacles(placements, abilityName, retarget.getOrDefault(agent, agent));
	}
}

