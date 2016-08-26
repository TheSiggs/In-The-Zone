package inthezone.ai;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.data.Player;
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
		while (true) {
			CommandRequest crq = nextCommand();

			try {
				Command cmd = crq.makeCommand(battle.battleState);
				List<Character> affectedCharacters = cmd.doCmd(battle);
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
	}

	private CommandRequest nextCommand() {
		return new EndTurnCommandRequest();
	}
}

