package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import java.util.ArrayList;
import java.util.Collection;

public class EndTurnCommand extends Command {
	public EndTurnCommand() {
	}

	@Override
	public Collection<Character> doCmd(Battle turn) {
		return new ArrayList<>();
	}
}

