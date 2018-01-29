package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
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
 * The revive instant effect.
 * */
public class Revive extends InstantEffect {
	public final List<MapPoint> targets = new ArrayList<>();

	private Revive(final MapPoint agent, final Collection<MapPoint> targets) {
		super(agent);
		this.targets.addAll(targets);
	}

	@JSON
	private Revive(
		@Field("kind") final InstantEffectType kind,
		@Field("agent") final MapPoint agent,
		@Field("targets") final Collection<MapPoint> targets
	) throws ProtocolException {
		this(agent, targets);

		if (kind != InstantEffectType.REVIVE)
			throw new ProtocolException("Expected revive effect");
	}

	public static Revive getEffect(
		final Set<MapPoint> targets, final MapPoint agent
	) {
		return new Revive(agent, targets);
	}

	/**
	 * Apply this effect assuming traps and zones have been triggered.
	 * */
	@Override public List<Targetable> apply(final Battle battle)
		throws CommandException
	{
		return battle.doRevive(targets);
	}

	/**
	 * Update the locations of targets to this effect.
	 * */
	@Override public InstantEffect retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		return new Revive(agent, targets.stream()
			.map(t -> retarget.getOrDefault(t, t)).collect(Collectors.toSet()));
	}
}

