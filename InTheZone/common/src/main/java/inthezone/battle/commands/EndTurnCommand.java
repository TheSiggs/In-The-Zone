package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Targetable;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.annotations.Field;

/**
 * One of the players ends their turn.
 * */
public class EndTurnCommand extends Command {
	public final Player player;

	private final CommandKind kind = CommandKind.ENDTURN;

	public EndTurnCommand(final Player player) {
		this.player = player;
	}

	@JSONConstructor
	private EndTurnCommand(
		@Field("kind") final CommandKind kind,
		@Field("player") final Player player
	) throws ProtocolException {
		this(player);

		if (kind != CommandKind.ENDTURN)
			throw new ProtocolException("Expected end turn command");
	}

	@Override
	public List<? extends Targetable> doCmd(final Battle turn) {
		return new ArrayList<>(turn.battleState.characters);
	}
}

