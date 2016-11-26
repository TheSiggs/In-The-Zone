package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.Stats;
import inthezone.battle.instant.PullPush;
import inthezone.battle.Targetable;
import inthezone.battle.Trap;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PushCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint target;

	private final InstantEffectInfo pushEffect;

	public PushCommandRequest(MapPoint agent, MapPoint target) {
		this.agent = agent;
		this.target = target;

		try {
			pushEffect = new InstantEffectInfo("push 1");
		} catch (CorruptDataException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	@Override
	public List<Command> makeCommand(BattleState battleState) throws CommandException {
		Targetable t = battleState.getTargetableAt(target).stream()
			.filter(x -> !(x instanceof Trap)).findFirst()
			.orElseThrow(() -> new CommandException("1: Invalid push command"));

		Collection<MapPoint> targets = new ArrayList<>();
		targets.add(target);

		Command r = battleState.getCharacterAt(agent).flatMap(a -> {
			if (a != null && t != null) {
				boolean effective = t.isPushable();
				return Optional.of(new PushCommand(agent, PullPush.getEffect(
					battleState, pushEffect, agent, targets, false), effective));
			}

			return Optional.empty();
		}).orElseThrow(() -> new CommandException("2: Invalid push command request"));

		List<Command> lr = new ArrayList<>();
		lr.add(r);
		return lr;
	}
}

