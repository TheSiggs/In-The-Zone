package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Targetable;
import inthezone.battle.Trigger;
import inthezone.battle.instant.InstantEffect;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.annotations.Field;

/**
 * One of the instant effects.
 * */
public class InstantEffectCommand extends Command {
	private final CommandKind kind = CommandKind.INSTANT;

	private InstantEffect effect;
	private boolean isComplete;
	private boolean waitingForCompletion = false;

	private final Optional<UseAbilityCommand> postAbility;
	private final Optional<InstantEffectCommand> postEffect;

	public InstantEffect getEffect() { return effect; }

	public InstantEffectCommand(
		final InstantEffect effect,
		final Optional<UseAbilityCommand> postAbility,
		final Optional<InstantEffectCommand> postEffect
	) {
		this.effect = effect;
		this.postAbility = postAbility;
		this.postEffect = postEffect;
		isComplete = effect.isComplete();
		canCancel = postAbility.map(a -> a.subsequentLevel == 0).orElse(false);
	}

	@JSONConstructor
	private InstantEffectCommand(
		@Field("kind") final CommandKind kind,
		@Field("effect") final InstantEffect effect,
		@Field("postAbility") final Optional<UseAbilityCommand> postAbility,
		@Field("postEffect") final Optional<InstantEffectCommand> postEffect
	) throws ProtocolException {
		this(effect, postAbility, postEffect);

		if (kind != CommandKind.INSTANT)
			throw new ProtocolException("Expected effect command");
	}

	/**
	 * Returns true if the command is ready to run, otherwise false.
	 * @throws CommandException if it is called while the command is already
	 * waiting for completion.
	 * */
	public boolean isCompletedOrRequestCompletion() throws CommandException {
		if (isComplete) return true; else {
			if (waitingForCompletion) {
				throw new CommandException("10: Incomplete instant effect");
			} else {
				waitingForCompletion = true;
				return false;
			}
		}
	}

	/**
	 * Attempt to complete this command.
	 * */
	public void complete(
		final BattleState battle, final List<MapPoint> ps
	) throws CommandException
	{
		if (!effect.complete(battle, ps))
			throw new CommandException("11: Could not complete instant effect");
		isComplete = true;
		waitingForCompletion = false;
	}

	private void retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		this.effect = effect.retarget(battle, retarget);
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		// assume the retargeting was already done by doCmdComputingTriggers
		return effect.apply(battle);
	}

	@Override public List<ExecutedCommand> doCmdComputingTriggers(
		final Battle turn
	) throws CommandException
	{
		final List<ExecutedCommand> r = effect.applyComputingTriggers(turn,
			eff -> new InstantEffectCommand(eff, Optional.empty(), Optional.empty()));

		if (postAbility.isPresent() || postEffect.isPresent()) {
			final Map<MapPoint, MapPoint> retarget = effect.getRetargeting();

			retarget.values().stream()
				.flatMap(p -> turn.battleState.getTargetableAt(p).stream())
				.forEach(t -> t.currentZone = turn.battleState.getZoneAt(t.getPos()));

			postAbility.ifPresent(a -> {
				a.registerConstructedObjects(effect.getConstructed());
				a.retarget(retarget);
			});
			postEffect.ifPresent(a -> a.retarget(turn.battleState, retarget));
		}

		final Trigger trigger = new Trigger(turn.battleState);
		final List<Command> triggers = new ArrayList<>(effect.getConstructed().stream()
			.flatMap(p -> trigger.getAllTriggers(p).stream())
			.collect(Collectors.toList()));

		for (final Command c : triggers) r.addAll(c.doCmdComputingTriggers(turn));
		return r;
	}
}

