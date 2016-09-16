package inthezone.ai;

import inthezone.battle.Battle;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.data.Player;
import inthezone.battle.Targetable;
import inthezone.comptroller.BattleListener;
import javafx.application.Platform;
import java.util.List;

public class SimpleAI implements CommandGenerator {
	public SimpleAI() {
	}

	@Override
	public void generateCommands(
		Battle battle, BattleListener listener, Player forPlayer
	) {
		for (Command cmd : battle.doTurnStart(forPlayer)) {
			try {
				List<Targetable> affectedCharacters = cmd.doCmd(battle);
				Platform.runLater(() -> {
					listener.command(cmd, affectedCharacters);
				});

				if (battle.battleState.getBattleOutcome(forPlayer).isPresent()) {
					return;
				}

				if (cmd instanceof EndTurnCommand) return;
			} catch (CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));
			}
		}

		try {
			Command cmd = new EndTurnCommand();
			List<Targetable> affectedCharacters = cmd.doCmd(battle);
			Platform.runLater(() -> {
				listener.command(cmd, affectedCharacters);
			});
		} catch (CommandException e) {
			Platform.runLater(() -> listener.badCommand(e));
		}
	}
}

