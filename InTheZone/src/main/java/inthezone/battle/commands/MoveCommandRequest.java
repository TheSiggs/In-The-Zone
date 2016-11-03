package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;

public class MoveCommandRequest extends CommandRequest {
	private final MapPoint start;
	private final MapPoint target;
	private final Player player;

	public MoveCommandRequest(
		MapPoint start, MapPoint target, Player player
	) {
		this.start = start;
		this.target = target;
		this.player = player;
	}
	
	@Override
	public List<Command> makeCommand(BattleState battleState) throws CommandException {
		List<MapPoint> path = battleState.findPath(start, target, player);
		if (battleState.canMove(path)) {
			List<Command> r = new ArrayList<>();
			r.add(new MoveCommand(path));
			return r;
		} else {
			throw new CommandException("Bad path command request");
		}
	}
}

