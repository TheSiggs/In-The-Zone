package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.annotations.JSON;
import ssjsjs.annotations.Field;

/**
 * A command that does nothing.
 * */
public class NullCommand extends Command {
	private CommandKind kind = CommandKind.NULL;

	public NullCommand() { }

	@JSON
	private NullCommand(
		@Field("kind") final CommandKind kind
	) throws ProtocolException {
		if (kind != CommandKind.NULL)
			throw new ProtocolException("Expected null command");
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		return new ArrayList<>();
	}
}


