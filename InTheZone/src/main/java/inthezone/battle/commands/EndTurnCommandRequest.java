package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.data.Player;
import java.util.ArrayList;
import java.util.List;

public class EndTurnCommandRequest extends CommandRequest {
	private final Player player;

	public EndTurnCommandRequest(Player player) {
		this.player = player;
	}

	@Override
	public List<Command> makeCommand(BattleState turn) {
		List<Command> r = new ArrayList<>();
		r.add(new EndTurnCommand(player));
		return r;
	}
}

