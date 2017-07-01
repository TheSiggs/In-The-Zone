package inthezone.battle.commands;

import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.List;

import inthezone.battle.BattleState;
import inthezone.battle.data.Player;

public class MoveCommandRequest extends CommandRequest {
	private final MapPoint start;
	private final MapPoint target;
	private final Player player;

	public MoveCommandRequest(
		final MapPoint start, final MapPoint target, final Player player
	) {
		this.start = start;
		this.target = target;
		this.player = player;
	}
	
	@Override
	public List<Command> makeCommand(final BattleState battleState)
		throws CommandException
	{
		final List<MapPoint> path = battleState.findPath(start, target, player);
		if (battleState.canMove(path)) {
			final List<Command> r = new ArrayList<>();
			r.add(new MoveCommand(path, false));
			return r;
		} else {
			throw new CommandException("30: Bad path command request");
		}
	}
}

