package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoveCommand extends Command {
	public final List<MapPoint> path;

	public MoveCommand(List<MapPoint> path) throws CommandException {
		if (path.size() < 2) throw new CommandException("Bad path in move command");
		this.path = path;
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canMove(path)) throw new CommandException("Invalid move command");
		Optional<Character> oc = battle.battleState.getCharacterAt(path.get(0));

		battle.doMove(path);

		List<Character> r = new ArrayList<>();
		oc.ifPresent(c -> r.add(c.clone()));
		return r;
	}
}

