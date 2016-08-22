package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.Item;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UseItemCommand extends Command {
	private final MapPoint agent;
	private final Item item;

	public UseItemCommand(MapPoint agent, Item item) {
		this.agent = agent;
		this.item = item;
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canUseItem(agent, item))
			throw new CommandException("Invalid item command");

		Optional<Character> oc = battle.battleState.getCharacterAt(agent);

		battle.doUseItem(agent, item);

		List<Character> r = new ArrayList<>();
		oc.ifPresent(c -> r.add(c.clone()));
		return r;
	}
}

