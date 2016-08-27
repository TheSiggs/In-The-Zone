package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.data.Player;
import java.util.ArrayList;
import java.util.List;

public class ResignCommandRequest extends CommandRequest {
	private final Player player;

	public ResignCommandRequest(Player player) {
		this.player = player;
	}

	@Override
	public List<Command> makeCommand(BattleState battleState) throws CommandException {
		List<Command> r = new ArrayList<>();
		r.add(new ResignCommand(player));
		return r;
	}
}


