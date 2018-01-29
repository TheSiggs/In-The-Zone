package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.HealthPotion;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * A character uses a command.
 * */
public class UseItemCommand extends Command {
	private final CommandKind kind = CommandKind.ITEM;

	private final MapPoint agent;
	private final MapPoint target;

	public UseItemCommand(
		final MapPoint agent,
		final MapPoint target
	) {
		this.agent = agent;
		this.target = target;
	}

	@JSON
	private UseItemCommand(
		@Field("kind") final CommandKind kind,
		@Field("agent") final MapPoint agent,
		@Field("target") final MapPoint target
	) throws ProtocolException {
		this(agent, target);

		if (kind != CommandKind.ITEM)
			throw new ProtocolException("Expected move command");
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		// there is only one item for now, so just create a health potion
		battle.doUseItem(agent, target, new HealthPotion());

		final List<Targetable> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(x -> r.add(x));
		battle.battleState.getCharacterAt(target).ifPresent(x -> r.add(x));
		r.addAll(battle.battleState.characters);
		return r;
	}
}

