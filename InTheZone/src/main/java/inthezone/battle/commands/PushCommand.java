package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class PushCommand extends Command {
	private final MapPoint agent;
	private final MapPoint target;
	private final boolean effective; // determines if the push is effective

	public PushCommand(MapPoint agent, MapPoint target, boolean effective) {
		this.agent = agent;
		this.target = target;
		this.effective = effective;
	}

	@Override
	public Collection<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canPush(agent, target, effective))
			throw new CommandException("Invalid push command");

		Collection<Character> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
		battle.battleState.getCharacterAt(target).ifPresent(c -> r.add(c));

		battle.doPush(agent, target, effective);

		return r.stream().map(c -> c.clone()).collect(Collectors.toList());
	}
}

