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
	private final int param;
	private final Collection<MapPoint> placements;
	private Collection<MapPoint> attackArea = null;

	private Obstacles(
		int param,
		Collection<MapPoint> placements
	) {
		this.param = param;
		this.placements = placements;
	}

	private Obstacles(
		int param,
		Collection<MapPoint> placements,
		Collection<MapPoint> attackArea
	) {
		this.param = param;
		this.placements = placements;
		this.attackArea = attackArea;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.OBSTACLES.toString());
		o.put("param", param);

		JSONArray a = new JSONArray();
		for (MapPoint p : placements) a.add(p.getJSON());
		o.put("placements", a);
		return o;
	}

	public static Obstacles fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oparam = json.get("param");
		Object oplacements = json.get("paths");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (oparam == null) throw new ProtocolException("Missing effect parameter");
		if (oplacements == null) throw new ProtocolException("Missing effect placements");

		try {
			if (InstantEffectType.fromString((String) okind) != InstantEffectType.OBSTACLES)
				throw new ProtocolException("Expected obstacles effect");

			Collection<MapPoint> placements = new ArrayList<>();
			Number param = (Number) oparam;
			JSONArray rawPlacements = (JSONArray) oplacements;
			for (int i = 0; i < rawPlacements.size(); i++) {
				placements.add(MapPoint.fromJSON((JSONObject) rawPlacements.get(i)));
			}
			return new Obstacles(param.intValue(), placements);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing obstacles effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing obstacles effect", e);
		}
	}

	public static Obstacles getEffect(
		BattleState battle,
		InstantEffectInfo info,
		Collection<MapPoint> attackArea
	) {
		List<MapPoint> area = attackArea.stream()
			.filter(p -> battle.isSpaceFree(p))
			.collect(Collectors.toList());
		Collections.shuffle(area);
		int n = info.param > area.size() ? area.size() : info.param;
		Collection<MapPoint> placements = new ArrayList<>();
		placements.addAll(area.subList(0, n));
		return new Obstacles(info.param, placements, attackArea);
	}

	@Override public List<Targetable> apply(Battle battle) throws CommandException {
		if (!placements.stream().allMatch(p -> battle.battleState.isSpaceFree(p)))
			throw new CommandException("Attempted to place obstacles in occupied space");

		return battle.doObstacles(placements);
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		return new HashMap<>();
	}

	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.OBSTACLES, this.param),
			this.attackArea);
	}

	@Override public boolean isComplete() {return true;}
	@Override public boolean complete(List<MapPoint> p) {return true;}
}

