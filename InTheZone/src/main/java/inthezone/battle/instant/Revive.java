package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.InstantEffectInfo;;
import inthezone.battle.data.InstantEffectType;;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Revive extends InstantEffect {
	public final List<MapPoint> targets;

	protected Revive(MapPoint agent, List<MapPoint> targets) {
		super(agent);
		this.targets = targets;
	}

	@SuppressWarnings("unchecked")
	@Override public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		o.put("kind", InstantEffectType.REVIVE.toString());
		o.put("agent", agent.getJSON());

		final JSONArray a = new JSONArray();
		for (MapPoint t : targets) a.add(t.getJSON());
		o.put("targets", a);
		return o;
	}

	public static Revive fromJSON(JSONObject json) throws ProtocolException {
		final Object okind = json.get("kind");
		final Object oagent = json.get("agent");
		final Object otargets = json.get("targets");

		if (okind == null) throw new ProtocolException("Missing effect type");
		if (oagent == null) throw new ProtocolException("Missing effect agent");
		if (otargets == null) throw new ProtocolException("Missing effect targets");

		try {
			final InstantEffectInfo type = new InstantEffectInfo((String) okind);
			if (type.type != InstantEffectType.REVIVE)
				throw new ProtocolException("Expected revive effect");

			final MapPoint agent = MapPoint.fromJSON(((JSONObject) oagent));

			final JSONArray rawTargets = (JSONArray) otargets;
			List<MapPoint> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(MapPoint.fromJSON((JSONObject) rawTargets.get(i)));
			}

			return new Revive(agent, targets);
		} catch (ClassCastException|CorruptDataException  e) {
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

