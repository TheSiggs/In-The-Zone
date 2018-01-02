package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Targetable;
import inthezone.battle.data.Player;
import java.util.List;

/**
 * A dummy command not intended to be relayed between players.
 * */
public class StartTurnCommand extends Command {
	public final Player player;

	private final List<Targetable> affected;

	public StartTurnCommand(final Player player, final List<Targetable> affected) {
		this.affected = affected;
		this.player = player;
	}

	@Override
	public List<Targetable> doCmd(final Battle turn) {
		return affected;
	}
}

