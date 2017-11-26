package inthezone.ai;

import inthezone.battle.Battle;
import inthezone.battle.BattleListener;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.Player;
import inthezone.battle.Targetable;
import javafx.application.Platform;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleAI implements CommandGenerator {
	public SimpleAI() {
	}

	@Override
	public void generateCommands(
		final Battle battle, final BattleListener listener, final Player forPlayer
	) {
		for (final Command cmd : battle.getTurnStart(forPlayer)) {
			try {
				final ExecutedCommand ec = new ExecutedCommand(cmd, cmd.doCmd(battle));
				Platform.runLater(() -> listener.command(ec)); 
				if (battle.battleState.getBattleOutcome(forPlayer).isPresent()) {
					return;
				}

				if (cmd instanceof EndTurnCommand) return;
			} catch (CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));
			}
		}

		try {
			final Command cmd = new EndTurnCommand(forPlayer);
			final ExecutedCommand ec = new ExecutedCommand(cmd, cmd.doCmd(battle));
			Platform.runLater(() -> listener.command(ec));
		} catch (CommandException e) {
			Platform.runLater(() -> listener.badCommand(e));
		}
	}
}

