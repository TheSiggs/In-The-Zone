package inthezone.battle.commands;

import inthezone.battle.Ability;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.instant.InstantEffectFactory;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UseAbilityCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint castFrom;
	private final Collection<MapPoint> targets = new ArrayList<>();
	private final Ability ability;

	public UseAbilityCommandRequest(
		MapPoint agent, MapPoint castFrom,
		Collection<MapPoint> targets, Ability ability
	) {
		this.agent = agent;
		this.castFrom = castFrom;
		this.targets.addAll(targets);
		this.ability = ability;
	}

	@Override
	public List<Command> makeCommand(BattleState battleState) throws CommandException {
		List<Command> commands = new ArrayList<>();

		if (ability.info.trap) {
			commands.add(new UseAbilityCommand(agent, castFrom,
				ability.rootName, targets, new ArrayList<>(), 0, 0));

		} else {
			// get the targets
			Collection<DamageToTarget> allTargets =
				battleState.getCharacterAt(agent).map(a -> {
					double revengeBonus = battleState.getRevengeBonus(a.player);

					return targets.stream()
						.flatMap(t ->
							battleState.getAbilityTargets(agent, castFrom, ability, t).stream())
						.map(t -> ability.computeDamageToTarget(a, t, revengeBonus))
						.collect(Collectors.toList());
				}).orElseThrow(() -> new CommandException("Invalid ability command request"));

			// get the instant effect targets
			List<MapPoint> preTargets = new ArrayList<>();
			List<MapPoint> postTargets = new ArrayList<>();
			for (DamageToTarget t : allTargets) {
				if (t.pre) preTargets.add(t.target);
				if (t.post) postTargets.add(t.target);
			}
			ability.info.instantBefore.ifPresent(i -> {
				if (i.isField()) preTargets.addAll(targets);});
			ability.info.instantAfter.ifPresent(i -> {
				if (i.isField()) postTargets.addAll(targets);});

			InstantEffectCommand preEffect = null;
			UseAbilityCommand mainEffect;
			InstantEffectCommand postEffect = null;

			// deal with vampirism
			battleState.getCharacterAt(agent).ifPresent(a -> {
				if (a.isVampiric()) allTargets.add(ability.computeVampirismEffect(
					battleState, a, allTargets));
			});
			
			// Main damage
			mainEffect = new UseAbilityCommand(agent, castFrom, ability.rootName,
				targets, allTargets, ability.subsequentLevel, ability.recursionLevel);
				
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

			if (preEffect != null) commands.add(preEffect);
			commands.add(mainEffect);
			if (postEffect != null) commands.add(postEffect);
		}

		return commands;
	}

	private InstantEffectCommand makeInstantEffect(
		BattleState battleState,
		List<MapPoint> effectTargets,
		InstantEffectInfo effect,
		Optional<UseAbilityCommand> postCommand,
		Optional<InstantEffectCommand> postEffect
	) {
		Collection<MapPoint> targetArea = targets.stream()
			.flatMap(t -> battleState.getAffectedArea(
				agent, castFrom, ability, t).stream())
			.collect(Collectors.toList());

		return new InstantEffectCommand(InstantEffectFactory.getEffect(
			battleState, effect, castFrom, targetArea, effectTargets),
			postCommand, postEffect);
	}
}

