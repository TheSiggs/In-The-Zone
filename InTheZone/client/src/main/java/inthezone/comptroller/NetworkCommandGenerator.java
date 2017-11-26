package inthezone.comptroller;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.BattleListener;
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

	public NetworkCommandGenerator(final BlockingQueue<Command> commandQueue) {
		this.commandQueue = commandQueue;
	}

	@Override
	public void generateCommands(
		final Battle battle, final BattleListener listener, final Player forPlayer
	) {
		while (true) {
			try {
				final Command cmd = commandQueue.take();

				try {
					final ExecutedCommand ec = new ExecutedCommand(cmd, cmd.doCmd(battle));
					Platform.runLater(() -> listener.command(ec.markLastInSequence())); 
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

