package inthezone.battle.commands;

import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import inthezone.battle.BattleState;
import inthezone.battle.data.Player;

public class MoveCommandRequest extends CommandRequest {
	private final MapPoint start;
	private final MapPoint target;
	private final Player player;

	private final Optional<List<MapPoint>> pathSpec;

	public MoveCommandRequest(
		final MapPoint start, final MapPoint target, final Player player
	) {
		this.start = start;
		this.target = target;
		this.player = player;
		this.pathSpec = Optional.empty();
	}

	public MoveCommandRequest(
		final List<MapPoint> pathSpec, final Player player
	) {
		this.start = pathSpec.get(0);
		this.target = pathSpec.get(pathSpec.size() - 1);
		this.player = player;
		this.pathSpec = Optional.of(pathSpec);
	}
	
	@Override
	public List<Command> makeCommand(final BattleState battleState)
		throws CommandException
	{
		final List<MapPoint> path =
			pathSpec.orElseGet(() -> battleState.findPath(start, target, player));

		if (battleState.canMove(path)) {
			final List<Command> r = new ArrayList<>();
			r.add(new MoveCommand(path, false));
			return r;
		} else {
			throw new CommandException("30: Bad path command request");
		}
	}
}

