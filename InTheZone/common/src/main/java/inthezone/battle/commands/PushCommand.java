package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.Targetable;
import inthezone.battle.instant.PullPush;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.annotations.JSON;
import ssjsjs.annotations.Field;

/**
 * Push another character.
 * */
public class PushCommand extends Command {
	private final CommandKind kind = CommandKind.PUSH;

	private final MapPoint agent;
	public final PullPush effect;
	private final boolean effective; // determines if the push is effective

	public PushCommand(
		final MapPoint agent,
		final PullPush effect,
		final boolean effective
	) {
		this.agent = agent;
		this.effect = effect;
		this.effective = effective;
	}

	@JSON
	private PushCommand(
		@Field("kind") final CommandKind kind,
		@Field("agent") final MapPoint agent,
		@Field("effect") final PullPush effect,
		@Field("effective") final boolean effective
	) throws ProtocolException {
		this(agent, effect, effective);

		if (kind != CommandKind.PUSH)
			throw new ProtocolException("Expected push command");
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		List<Targetable> r = new ArrayList<>();
		if (effective) {
			Character user = battle.battleState.getCharacterAt(agent)
				.orElseThrow(() -> new CommandException("40: Cannot find push agent"));

			user.usePush();
			// by convention, we always put the agent first in the affected characters list.
			r.add(user);
			r.addAll(effect.apply(battle));
			return r;
		} else {
			return r;
		}
	}
}

