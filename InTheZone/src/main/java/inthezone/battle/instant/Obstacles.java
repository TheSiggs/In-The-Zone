package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Obstacles implements InstantEffect {
	private final MapPoint agent;
	@Override public MapPoint getAgent() {return agent;}

	private final Collection<MapPoint> placements;

	private Obstacles(Collection<MapPoint> placements, MapPoint agent) {
		this.placements = placements;
		this.agent = agent;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.OBSTACLES.toString());
		o.put("agent", agent.getJSON());

		JSONArray a = new JSONArray();
		for (MapPoint p : placements) a.add(p.getJSON());
		o.put("placements", a);
		return o;
	}

	public static Obstacles fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oagent = json.get("agent");
		Object oplacements = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (oagent == null) throw new ProtocolException("Missing effect agent");
		if (oplacements == null) throw new ProtocolException("Missing effect placements");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.OBSTACLES)
				throw new ProtocolException("Expected obstacles effect");

			Collection<MapPoint> placements = new ArrayList<>();
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			JSONArray rawPlacements = (JSONArray) oplacements;
			for (int i = 0; i < rawPlacements.size(); i++) {
				placements.add(MapPoint.fromJSON((JSONObject) rawPlacements.get(i)));
			}
			return new Obstacles(placements, agent);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing obstacles effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing obstacles effect", e);
		}
	}

	public static Obstacles getEffect(
		MapPoint agent,
		BattleState battle,
		InstantEffectInfo info,
		Collection<MapPoint> targets
	) {
		return new Obstacles(targets, agent);
	}

	@Override public List<Targetable> apply(Battle battle) throws CommandException {
		return battle.doObstacles(placements.stream()
			.filter(p -> battle.battleState.isSpaceFree(p))
			.collect(Collectors.toList()));
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		return new HashMap<>();
	}

	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		return new Obstacles(placements, retarget.getOrDefault(agent, agent));
	}

	@Override public boolean isComplete() {return true;}
	@Override public boolean complete(List<MapPoint> p) {return true;}
}

