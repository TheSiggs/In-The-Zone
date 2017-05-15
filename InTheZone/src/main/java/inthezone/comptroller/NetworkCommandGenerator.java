package inthezone.comptroller;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.Player;
import javafx.application.Platform;
import java.util.concurrent.BlockingQueue;

/**
 * A bridge between the battle controller and the network code.
 * */
public class NetworkCommandGenerator implements CommandGenerator {
	private final BlockingQueue<Command> commandQueue;

	public NetworkCommandGenerator(BlockingQueue<Command> commandQueue) {
		this.commandQueue = commandQueue;
	}

	@Override
	public void generateCommands(
		Battle battle, BattleListener listener, Player forPlayer
	) {
		while (true) {
			try {
				Command cmd = commandQueue.take();

				try {
					ExecutedCommand ec = new ExecutedCommand(cmd, cmd.doCmd(battle));
					Platform.runLater(() -> listener.command(ec)); 
					if (battle.battleState.getBattleOutcome(forPlayer).isPresent()) {
						return;
					}

					if (cmd instanceof EndTurnCommand) return;
				} catch (CommandException e) {
					Platform.runLater(() -> listener.badCommand(e));
				}
			} catch (InterruptedException e) {
				/* ignore */
			}
		}
	}
}

