package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Revive extends InstantEffect {
	public final List<MapPoint> targets;

	protected Revive(MapPoint agent, List<MapPoint> targets) {
		super(agent);
		this.targets = targets;
	}

	@Override public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.REVIVE.toString());
		o.put("agent", agent.getJSON());

		final JSONArray a = new JSONArray();
		for (MapPoint t : targets) a.put(t.getJSON());
		o.put("targets", a);
		return o;
	}

	public static Revive fromJSON(JSONObject json) throws ProtocolException {
		try {
			final InstantEffectType kind = (new InstantEffectInfo(json.getString("kind"))).type;
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final JSONArray rawTargets = json.getJSONArray("targets");

			if (kind != InstantEffectType.REVIVE)
				throw new ProtocolException("Expected revive effect");

			final List<MapPoint> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.length(); i++) {
				targets.add(MapPoint.fromJSON(rawTargets.getJSONObject(i)));
			}

			return new Revive(agent, targets);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing revive effect", e);
		}
	}

	/**
	 * Apply this effect assuming traps and zones have been triggered.
	 * */
	@Override public List<Targetable> apply(Battle battle) throws CommandException {
		return battle.doRevive(targets);
	}

	/**
	 * Update the locations of targets to this effect.
	 * */
	@Override public InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget
	) {
		return new Revive(agent, targets.stream()
			.map(t -> retarget.getOrDefault(t, t)).collect(Collectors.toList()));
	}
}

