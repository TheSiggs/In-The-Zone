package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSONConstructor;

/**
 * The create obstacles instant effect.
 * */
public class Obstacles extends InstantEffect {
	private final InstantEffectType kind = InstantEffectType.OBSTACLES;

	private final Collection<MapPoint> placements = new ArrayList<>();
	private final String abilityName;

	@JSONConstructor
	private Obstacles(
		@Field("kind") final InstantEffectType kind,
		@Field("placements") final Collection<MapPoint> placements,
		@Field("abilityName")@As("ability") final String abilityName,
		@Field("agent") final MapPoint agent
	) throws ProtocolException {
		this(placements, abilityName, agent);

		if (kind != InstantEffectType.OBSTACLES)
			throw new ProtocolException("Expected obstacles effect");
	}

	private Obstacles(
		final Collection<MapPoint> placements,
		final String abilityName,
		final MapPoint agent
	) {
		super(agent);
		this.abilityName = abilityName;
		this.placements.addAll(placements);
	}

	public static Obstacles getEffect(
		final MapPoint agent,
		final String abilityName,
		final Set<MapPoint> targets
	) {
		return new Obstacles(targets, abilityName, agent);
	}

	private final List<MapPoint> constructedObjects = new ArrayList<>();

	@Override public List<Targetable> apply(final Battle battle)
		throws CommandException
	{
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
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		return new Obstacles(new HashSet<>(placements), abilityName,
			retarget.getOrDefault(agent, agent));
	}
}

