package inthezone.battle.commands;

import inthezone.battle.BattleState;
import java.util.ArrayList;
import java.util.List;

public class EndTurnCommandRequest extends CommandRequest {
	public EndTurnCommandRequest() {
	}

	@Override
	public List<Command> makeCommand(BattleState turn) {
		List<Command> r = new ArrayList<>();
		r.add(new EndTurnCommand());
		return r;
	}
}

