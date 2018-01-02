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
 * A player resigns
 * */
public class ResignCommand extends Command {
	private final CommandKind kind = CommandKind.RESIGN;

	public final Player player;
	public final ResignReason reason;

	public ResignCommand(final Player player) {
		this.player = player;
		this.reason = ResignReason.RESIGNED;
	}

	public ResignCommand(final Player player, final ResignReason reason) {
		this.player = player;
		this.reason = reason;
	}

	@JSONConstructor
	private ResignCommand(
		@Field("kind") final CommandKind kind,
		@Field("player") final Player player,
		@Field("reason") final ResignReason reason
	) throws ProtocolException {
		if (kind != CommandKind.RESIGN)
			throw new ProtocolException("Expected resign command");

		this.player = player;
		this.reason = reason;
	}

	@Override
	public List<Targetable> doCmd(final Battle turn) {
		turn.doResign(player, reason);
		return new ArrayList<>();
	}
}

