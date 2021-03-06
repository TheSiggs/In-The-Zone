package inthezone.battle.commands;

import inthezone.battle.Ability;
import inthezone.battle.BattleState;
import inthezone.battle.Casting;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.instant.InstantEffectFactory;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UseAbilityCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final AbilityAgentType agentType;
	private final Collection<Casting> castings = new ArrayList<>();
	private final Ability ability;
	private final Set<MapPoint> ineligible;

	public UseAbilityCommandRequest(
		final MapPoint agent, final AbilityAgentType agentType,
		final Ability ability, final Collection<Casting> castings
	) {
		this(agent, agentType, ability, new HashSet<>(), castings);
	}

	public UseAbilityCommandRequest(
		final MapPoint agent, final AbilityAgentType agentType,
		final Ability ability, final Collection<MapPoint> ineligible,
		final Collection<Casting> castings
	) {
		this.agent = agent;
		this.agentType = agentType;
		this.ability = ability;
		this.ineligible = new HashSet<>();
		this.ineligible.addAll(ineligible);
		this.castings.addAll(castings);
	}

	/**
	 * @param a The agent targetable.
	 * */
	private Collection<DamageToTarget> computeDamageToTargets(
		final BattleState battleState, final Targetable a
	) {
		System.err.println("Computing damage for " + a);
		final double revengeBonus = (agentType != AbilityAgentType.CHARACTER)? 0 :
			battleState.getRevengeBonus(((Character) a).player);

		final Collection<DamageToTarget> r = new ArrayList<>();
		final Set<MapPoint> targeted = new HashSet<>();
		for (final Casting casting : castings) {
			final Set<MapPoint> area =
				battleState.getAffectedArea(agent, agentType, ability, casting);

			final Collection<Targetable> targets =
				battleState.getAbilityTargets(agent, agentType, ability, area)
					.stream().filter(t ->
						!targeted.contains(t.getPos()) && !ineligible.contains(t.getPos()))
					.collect(Collectors.toList());

			System.err.println("Casting " + casting + " with agent " + agent + " yielded " + targets + " with aoe " + ability.info.range.radius + " in area " + area);

			targeted.addAll(targets.stream()
				.map(t -> t.getPos()).collect(Collectors.toList()));
			for (final Targetable t : targets) r.add(
				ability.computeDamageToTarget(
					battleState, a, t, casting.castFrom, revengeBonus));
		}

		return r;
	}

	@Override
	public List<Command> makeCommand(final BattleState battleState)
		throws CommandException
	{
		final Collection<MapPoint> targetSquares = castings.stream()
			.map(c -> c.target).collect(Collectors.toList());

		final List<Command> commands = new ArrayList<>();

		if ((ability.info.trap) && agentType == AbilityAgentType.CHARACTER) {
			commands.add(new UseAbilityCommand(agent, agentType,
				ability.rootName, ability.manaRootName, targetSquares, new ArrayList<>(), 0));

		} else {
			// get the targets
			final Collection<DamageToTarget> allTargets =
				battleState.getAgentAt(agent, agentType)
					.map(a -> computeDamageToTargets(battleState, a))
					.orElseThrow(() ->
						new CommandException("60: Invalid ability command request"));

			System.err.println("All targets: " + allTargets);

			// get the instant effect targets
			final List<Casting> preTargets = new ArrayList<>();
			final List<Casting> postTargets = new ArrayList<>();

			// Only do instant effects when the agent is a character.
			if (agentType == AbilityAgentType.CHARACTER) {
				ability.info.instantBefore.ifPresent(i -> {
					if (i.isField()) {
						preTargets.addAll(castings);
					} else {
						for (final DamageToTarget t : allTargets)
							if (t.pre) preTargets.add(t.target);
					}
				});

				ability.info.instantAfter.ifPresent(i -> {
					if (i.isField()) {
						postTargets.addAll(castings);
					} else {
						for (final DamageToTarget t : allTargets)
							if (t.post) postTargets.add(t.target);
					}
				});
			}

			InstantEffectCommand preEffect = null;
			final UseAbilityCommand mainEffect;
			InstantEffectCommand postEffect = null;

			// deal with vampirism
			if (agentType == AbilityAgentType.CHARACTER) {
				System.err.println("Doing vampirism");
				battleState.getCharacterAt(agent).ifPresent(a -> {
					if (a.isVampiric()) allTargets.add(ability.computeVampirismEffect(
						battleState, a, allTargets));
				});
			}
			
			// Main damage
			mainEffect = new UseAbilityCommand(
				agent, agentType, ability.rootName, ability.manaRootName,
				targetSquares, allTargets, ability.subsequentLevel);
				
			// Post instant effect
			if (postTargets.size() > 0 && ability.info.instantAfter.isPresent()) {
				postEffect = makeInstantEffect(
					battleState, postTargets, ability.info.instantAfter.get(),
					Optional.empty(), Optional.empty());
			}

			// pre instant effect
			if (preTargets.size() > 0 && ability.info.instantBefore.isPresent()) {
				preEffect = makeInstantEffect(
						battleState, preTargets, ability.info.instantBefore.get(),
						Optional.of(mainEffect), Optional.ofNullable(postEffect));
			}

			if (preEffect != null) {
				mainEffect.notifyHasPreEffect();
				commands.add(preEffect);
			}
			commands.add(mainEffect);
			if (postEffect != null) commands.add(postEffect);
		}

		return commands;
	}

	private InstantEffectCommand makeInstantEffect(
		final BattleState battleState,
		final List<Casting> effectTargets,
		final InstantEffectInfo effect,
		final Optional<UseAbilityCommand> postCommand,
		final Optional<InstantEffectCommand> postEffect
	) throws CommandException {
		final Set<MapPoint> targetArea = castings.stream()
			.flatMap(casting -> battleState.getAffectedArea(
				agent, agentType, ability, casting).stream())
			.collect(Collectors.toSet());

		final Set<MapPoint> targets =
			effectTargets.stream().map(c -> c.target).collect(Collectors.toSet());
		return new InstantEffectCommand(InstantEffectFactory.getEffect(
			ability.rootName, battleState, effect, agent,
			targetArea, targets), postCommand, postEffect);
	}
}

