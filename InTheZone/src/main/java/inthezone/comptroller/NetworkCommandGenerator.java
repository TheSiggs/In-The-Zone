package inthezone.comptroller;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import javafx.application.Platform;
import java.util.concurrent.BlockingQueue;
import java.util.List;

/**
 * A bridge between the battle controller and the network code.
 * */
public class NetworkCommandGenerator implements CommandGenerator {
	private final BlockingQueue<CommandRequest> commandQueue;

	public NetworkCommandGenerator(BlockingQueue<CommandRequest> commandQueue) {
		this.commandQueue = commandQueue;
	}

	@Override
	public void generateCommands(Battle battle, BattleListener listener) {
		while (true) {
			try {
				CommandRequest crq = commandQueue.take();

				try {
					Command cmd = crq.makeCommand(battle.battleState);
					List<Character> affectedCharacters = cmd.doCmd(battle);
					Platform.runLater(() -> {
						listener.command(cmd, affectedCharacters);
					});
					if (cmd instanceof EndTurnCommand) {
						return;
					}
				} catch (CommandException e) {
					Platform.runLater(() -> listener.badCommand(e));
				}
			} catch (InterruptedException e) {
				/* ignore */
			}
		}
	}
}

