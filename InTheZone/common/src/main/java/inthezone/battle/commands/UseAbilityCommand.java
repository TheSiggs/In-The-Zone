package inthezone.battle.commands;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.DamageToTarget;
import inthezone.battle.data.AbilityZoneType;
import inthezone.battle.RoadBlock;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * A character uses an ability.
 * */
public class UseAbilityCommand extends Command {
	private CommandKind kind = CommandKind.ABILITY;

	private MapPoint agent;
	public final AbilityAgentType agentType;
	public final String ability;
	public final String friendlyAbilityName;
	private final Collection<MapPoint> targetSquares = new ArrayList<>();
	private final Collection<DamageToTarget> targets = new ArrayList<>();
	public final int subsequentLevel;

	private final List<MapPoint> constructed = new ArrayList<>();

	public Collection<DamageToTarget> getTargets() {return targets;}

	// These are set when the command is executed so they can be available to the
	// GUI layer.
	public boolean placedTraps = false;
	public boolean placedZones = false;

	public UseAbilityCommand(
		final MapPoint agent,
		final AbilityAgentType agentType,
		final String ability,
		final String friendlyAbilityName,
		final Collection<MapPoint> targetSquares,
		final Collection<DamageToTarget> targets,
		final int subsequentLevel
	) {
		this(agent, agentType, ability, friendlyAbilityName,
			targetSquares, targets, new ArrayList<>(), subsequentLevel);
	}

	public UseAbilityCommand(
		final MapPoint agent,
		final AbilityAgentType agentType,
		final String ability,
		final String friendlyAbilityName,
		final Collection<MapPoint> targetSquares,
		final Collection<DamageToTarget> targets,
		final Collection<MapPoint> constructed,
		final int subsequentLevel
	) {
		this.agent = agent;
		this.agentType = agentType;
		this.friendlyAbilityName = friendlyAbilityName;
		this.ability = ability;
		this.targetSquares.addAll(targetSquares);
		this.targets.addAll(targets);
		this.constructed.addAll(constructed);
		this.subsequentLevel = subsequentLevel;
		canCancel = subsequentLevel == 0;
	}

	@JSON
	private UseAbilityCommand(
		@Field("kind") final CommandKind kind,
		@Field("agent") final MapPoint agent,
		@Field("agentType") final AbilityAgentType agentType,
		@Field("ability") final String ability,
		@Field("friendlyAbilityName")@As("fability") final String friendlyAbilityName,
		@Field("targetSquares") final Collection<MapPoint> targetSquares,
		@Field("targets") final Collection<DamageToTarget> targets,
		@Field("constructed") final List<MapPoint> constructed,
		@Field("subsequentLevel") final int subsequentLevel
	) throws ProtocolException {
		this(agent, agentType, ability, friendlyAbilityName,
			targetSquares, targets, constructed, subsequentLevel);
	}

	/**
	 * A hack to mark that this command has a pre-effect, and therefore cannot be
	 * cancelled after all.
	 * */
	void notifyHasPreEffect() {
		canCancel = false;
	}

	@Override
	public List<? extends Targetable> doCmd(final Battle battle) throws CommandException {
		final Ability abilityData = battle.battleState.getCharacterAt(agent)
			.flatMap(c -> Stream.concat(Stream.of(c.basicAbility), c.abilities.stream())
				.filter(a -> a.rootName.equals(ability)).findFirst())
			.flatMap(a -> a.getNext(
				battle.battleState.hasMana(agent), subsequentLevel))
			.orElse(null);

		if (abilityData == null && agentType == AbilityAgentType.CHARACTER)
			throw new CommandException("52: No such ability " + ability);

		final List<Targetable> r = new ArrayList<>();

		if (agentType == AbilityAgentType.CHARACTER && abilityData.info.trap) {
			placedTraps = true;

			// don't place traps on defusing zones
			final Collection<MapPoint> realTargets = targetSquares.stream()
				.filter(p -> !battle.battleState.hasDefusingZone(p))
				.collect(Collectors.toList());

			battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
			r.addAll(battle.battleState.getCharacterAt(agent)
				.map(c -> battle.createTrap(abilityData, c, realTargets))
				.orElseThrow(() -> new CommandException("53: Missing ability agent for " + ability)));

		} else {
			battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
			battle.battleState.getTrapAt(agent).ifPresent(t -> r.add(t));
			for (DamageToTarget d : targets) {
				battle.battleState.getTargetableAt(d.target.target).forEach(t -> r.add(t));
			}

			// do the ability now
			battle.doAbility(agent, agentType, abilityData, targets);
			r.addAll(battle.battleState.removeExpiredZones());

			// If it's a zone ability, also create the zone
			// bound zones
			if (
				agentType == AbilityAgentType.CHARACTER &&
				abilityData.info.zone == AbilityZoneType.BOUND_ZONE
			) {
				Optional<RoadBlock> o = constructed.stream()
					.flatMap(p -> battle.battleState.getTargetableAt(p).stream())
					.filter(t -> t instanceof RoadBlock).findFirst().map(t -> (RoadBlock) t);

				if (o.isPresent()) {
					placedZones = true;
					r.addAll(battle.battleState.getCharacterAt(agent)
						.map(c -> battle.createZone(abilityData, c, o, targetSquares))
						.orElseThrow(() -> new CommandException("54: Invalid ability command")));
				}

			// unbound zones
			} else if (
				agentType == AbilityAgentType.CHARACTER &&
				abilityData.info.zone == AbilityZoneType.ZONE
			) {
				placedZones = true;
				r.addAll(battle.battleState.getCharacterAt(agent)
					.map(c -> battle.createZone(
						abilityData, c, Optional.empty(), targetSquares))
					.orElseThrow(() -> new CommandException("55: Invalid ability command")));
			}
		}

		r.addAll(battle.battleState.characters);
		return r;
	}

	/**
	 * Called when the targets are moved by an instant effect.
	 * @param retarget A mapping from old character positions to their new positions.
	 * */
	public void retarget(final Map<MapPoint, MapPoint> retarget) {
		this.agent = retarget.getOrDefault(agent, agent);
		final List<DamageToTarget> targets1 = targets.stream()
			.map(t -> t.retarget(new Casting(
				retarget.getOrDefault(t.target.castFrom, t.target.castFrom),
				retarget.getOrDefault(t.target.target, t.target.target))))
			.collect(Collectors.toList());
		targets.clear();
		targets.addAll(targets1);
	}

	/**
	 * Called when new objects are created by instant effects.
	 * */
	public void registerConstructedObjects(final List<MapPoint> constructed) {
		this.constructed.addAll(constructed);
	}
}

