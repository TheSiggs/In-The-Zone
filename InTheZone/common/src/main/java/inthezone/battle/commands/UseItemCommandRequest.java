package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.Item;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;

public class UseItemCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final MapPoint target;

	public UseItemCommandRequest(final MapPoint agent, final MapPoint target) {
		this.agent = agent;
		this.target = target;
	}

	@Override
	public List<Command> makeCommand(final BattleState battleState)
		throws CommandException
	{
		final List<Command> r = new ArrayList<>();
		r.add(new UseItemCommand(agent, target));
		return r;
	}
}

