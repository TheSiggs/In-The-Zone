package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * Basic instant effects that don't require special handling.
 * */
public class SimpleInstantEffect extends InstantEffect {
	private final InstantEffectType kind;

	private final List<MapPoint> targets = new ArrayList<>();

	@JSON
	private SimpleInstantEffect(
		@Field("kind") final InstantEffectType kind,
		@Field("targets") final Collection<MapPoint> targets,
		@Field("agent") final MapPoint agent
	) throws ProtocolException {
		super(agent);
		this.targets.addAll(targets);
		this.kind = kind;

		if (!(kind == InstantEffectType.CLEANSE ||
			kind == InstantEffectType.DEFUSE ||
			kind == InstantEffectType.PURGE)
		) throw new ProtocolException("Expected cleanse, defuse or purge effect");
	}

	public static SimpleInstantEffect getEffect(
		final Set<MapPoint> targets,
		final MapPoint agent, final InstantEffectType type
	) throws ProtocolException {
		return new SimpleInstantEffect(type, targets, agent);
	}

	@Override public List<Targetable> apply(final Battle battle) {
		switch (kind) {
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
		try {
			return new SimpleInstantEffect(
				kind, targets.stream()
					.map(x -> retarget.getOrDefault(x, x))
					.collect(Collectors.toSet()),
				retarget.getOrDefault(agent, agent));
		} catch (final ProtocolException e) {
			throw new RuntimeException("Wrong kind for SimpleInstantEffect, this cannot happen", e);
		}
	}
}

