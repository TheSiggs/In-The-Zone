package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import java.util.ArrayList;
import java.util.List;

public class EndTurnCommand extends Command {
	public EndTurnCommand() {
	}

	@Override
	public List<Character> doCmd(Battle turn) {
		return new ArrayList<>();
	}
}

