package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.Stats;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.Optional;

public class PushCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint target;

	public PushCommandRequest(MapPoint agent, MapPoint target) {
		this.agent = agent;
		this.target = target;
	}

	@Override
	public Command makeCommand(BattleState battleState) throws CommandException {
		Targetable t = battleState.getTargetableAt(target)
			.orElseThrow(() -> new CommandException("Invalid push command"));

		return battleState.getCharacterAt(agent).flatMap(a -> {
			if (a != null && t != null) {
				boolean effective = t.isPushable();
				if (battleState.canPush(agent, target, effective)) {
					return Optional.of(new PushCommand(agent, target, effective));
				}
			}

			return Optional.empty();
		}).orElseThrow(() -> new CommandException("Invalid push command request"));
	}
}

