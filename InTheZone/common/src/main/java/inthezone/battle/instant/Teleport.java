package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * Teleport instant effect.
 * */
public class Teleport extends InstantEffect {
	private final InstantEffectType kind = InstantEffectType.TELEPORT;

	public final int range;
	private final List<Character> affectedCharacters = new ArrayList<>();

	public final List<CharacterFrozen> getAffectedCharacters() {
		return affectedCharacters.stream()
			.map(c -> c.freeze()).collect(Collectors.toList());
	}

	private final List<MapPoint> targets = new ArrayList<>();
	private List<MapPoint> destinations = new ArrayList<>();

	private Teleport(
		final Optional<Collection<Character>> affectedCharacters,
		final int range,
		final Optional<List<MapPoint>> targets,
		final Optional<List<MapPoint>> destinations,
		final MapPoint agent
	) {
		super(agent);

		affectedCharacters.ifPresent(a -> {
			this.affectedCharacters.addAll(a);
			this.targets.addAll(a.stream()
				.map(c -> c.getPos()).collect(Collectors.toList()));
		});

		targets.ifPresent(t -> {
			this.targets.clear();
			this.targets.addAll(t);
		});

		destinations.ifPresent(d -> this.destinations.addAll(d));

		this.range = range;
	}

	@JSON
	private Teleport(
		@Field("kind") final InstantEffectType kind,
		@Field("range") final int range,
		@Field("targets") final Optional<List<MapPoint>> targets,
		@Field("destinations") final Optional<List<MapPoint>> destinations,
		@Field("agent") final MapPoint agent
	) throws ProtocolException {
		this(Optional.empty(), range, targets, destinations, agent);

		if (kind != InstantEffectType.TELEPORT)
			throw new ProtocolException("Expected teleport effect");
	}

	public List<MapPoint> getDestinations() {
		return destinations;
	}

	public static Teleport getEffect(
		final BattleState battle, final InstantEffectInfo info,
		final Set<MapPoint> targets, final MapPoint agent
	) {
		final Set<Character> affected = targets.stream()
			.flatMap(x -> battle.getCharacterAt(x)
				.map(v -> Stream.of(v)).orElse(Stream.empty()))
			.collect(Collectors.toSet());
		return new Teleport(Optional.of(affected), info.param,
			Optional.empty(), Optional.empty(), agent);
	}

	@Override public List<Targetable> apply(final Battle battle)
		throws CommandException
	{
		if (destinations == null || targets.size() != destinations.size())
			throw new CommandException("Attempted to apply incomplete teleport");

		final List<Targetable> r = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			r.addAll(battle.doTeleport(targets.get(i), destinations.get(i)));
		}

		return r;
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		final Battle battle, final Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		final List<ExecutedCommand> r = new ArrayList<>();

		r.add(new ExecutedCommand(cmd.apply(this), apply(battle)));

		for (final MapPoint p : destinations) {
			final List<Command> triggers =
				battle.battleState.trigger.getAllTriggers(p);
			for (final Command c : triggers)
				r.addAll(c.doCmdComputingTriggers(battle));
		}

		return r;
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		final Map<MapPoint, MapPoint> r = new HashMap<>();

		for (int i = 0; i < targets.size(); i++) {
			r.put(targets.get(i), destinations.get(i));
		}
		return r;
	}

	@Override public InstantEffect retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		final Set<MapPoint> newTargets =
			targets.stream().map(t -> retarget.getOrDefault(t, t))
			.collect(Collectors.toSet());

		return getEffect(battle,
			new InstantEffectInfo(InstantEffectType.TELEPORT, range),
			newTargets, retarget.getOrDefault(agent, agent));
	}

	@Override public boolean isComplete() {
		return destinations.size() == targets.size();
	}

	@Override public boolean complete(
		final BattleState battle, final List<MapPoint> ps
	) {
		destinations = new ArrayList<>();
		destinations.addAll(ps);
		return targets.size() == destinations.size();
	}
}

