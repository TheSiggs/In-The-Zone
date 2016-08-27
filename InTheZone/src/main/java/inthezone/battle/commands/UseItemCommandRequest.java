package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.Item;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;

public class UseItemCommandRequest extends CommandRequest {
	private final MapPoint agent;
	private final Item item;

	public UseItemCommandRequest(MapPoint agent, Item item) {
		this.agent = agent;
		this.item = item;
	}

	@Override
	public List<Command> makeCommand(BattleState battleState) throws CommandException {
		if (battleState.canUseItem(agent, item)) {
			List<Command> r = new ArrayList<>();
			r.add(new UseItemCommand(agent, item));
			return r;
		} else {
			throw new CommandException("Invalid item command request");
		}
	}
}

