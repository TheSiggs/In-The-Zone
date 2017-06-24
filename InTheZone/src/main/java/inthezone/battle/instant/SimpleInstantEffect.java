package inthezone.battle.instant;

import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Targetable;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;

public class SimpleInstantEffect extends InstantEffect {
	private final Collection<MapPoint> targets;
	private final InstantEffectType type;

	private SimpleInstantEffect(
		final Collection<MapPoint> targets,
		final MapPoint agent,
		final InstantEffectType type
	) {
		super(agent);
		this.targets = targets;
		this.type = type;
	}

	@Override public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		final JSONArray a = new JSONArray();
		o.put("kind", type.toString());
		o.put("agent", agent.getJSON()); 
		for (MapPoint t : targets) a.put(t.getJSON());
		o.put("targets", a);
		return o;
	}

	public static SimpleInstantEffect fromJSON(final JSONObject json)
		throws ProtocolException
	{
		try {
			final InstantEffectType kind =
				(new InstantEffectInfo(json.getString("kind"))).type;
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final JSONArray rawTargets = json.getJSONArray("targets");

			if (!(kind == InstantEffectType.CLEANSE ||
				kind == InstantEffectType.DEFUSE ||
				kind == InstantEffectType.PURGE)
			) throw new ProtocolException("Expected cleanse, defuse or purge effect");

			final List<MapPoint> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.length(); i++) {
				targets.add(MapPoint.fromJSON(rawTargets.getJSONObject(i)));
			}

			return new SimpleInstantEffect(targets, agent, kind);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing cleanse/purge/defuse effect", e);
		}
	}

	public static SimpleInstantEffect getEffect(
		final Collection<MapPoint> targets,
		final MapPoint agent, final InstantEffectType type
	) {
		return new SimpleInstantEffect(targets, agent, type);
	}

	@Override public List<Targetable> apply(final Battle battle) {
		switch (type) {
			case CLEANSE: return battle.doCleanse(targets);
			case PURGE: return battle.doPurge(targets);
			case DEFUSE: return battle.doDefuse(targets);
			default: throw new RuntimeException(
				"Invalid simple effect, this cannot happen");
		}
	}

	@Override public InstantEffect retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		return new SimpleInstantEffect(
			targets.stream()
				.map(x -> retarget.getOrDefault(x, x))
				.collect(Collectors.toList()),
			retarget.getOrDefault(agent, agent), type);
	}
}

