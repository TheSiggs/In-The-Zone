package inthezone.game.battle;

import static javafx.scene.control.Alert.AlertType;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.BattleListener;
import inthezone.protocol.ProtocolException;
import java.io.BufferedReader;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaybackGenerator implements CommandGenerator {
	private BufferedReader inReader = null;

	public StartBattleCommand start(
		final BufferedReader inReader,
		final GameDataFactory gameData
	) throws IOException, ProtocolException {
		this.inReader = inReader;

		try {
			final RecordedLine inLine = new RecordedLine(inReader.readLine());
			System.err.println("Recorded game was started by " + inLine.playerName);
			return StartBattleCommand.fromJSON(inLine.cmd, gameData);
		} catch (JSONException e) {
			throw new ProtocolException("Error parsing JSON", e);
		}
	}

	class RecordedLine {
		public final String playerName;
		public final JSONObject cmd;
		public final boolean isEOF;

		public RecordedLine(final String raw) throws JSONException {
			if (raw == null) {
				playerName = null;
				cmd = null;
				isEOF = true;
			} else {
				isEOF = false;
				final int i = raw.indexOf("/");
				this.playerName = raw.substring(0, i);
				this.cmd = new JSONObject(raw.substring(i + 1));
			}
		}
	}

	/**
	 * Release the next command
	 * */
	public synchronized void nextCommand() {
		this.notify();
	}

	/* A hack to make these values available for the close method*/
	Battle battle = null;
	BattleListener listener = null;
	Player forPlayer = null;

	/**
	 * Close this playback generator.
	 * */
	public synchronized void close() {
		if (inReader != null) {
			try {
				inReader.close();

				if (forPlayer != null && battle != null && listener != null) {
					final Command cmd = new ResignCommand(forPlayer, true);
					final ExecutedCommand ec =
						new ExecutedCommand(cmd, cmd.doCmd(battle));
					Platform.runLater(() -> listener.command(ec.markLastInSequence())); 
				}

			} catch (CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));

			} catch (IOException e) {
				/* ignore */
			}
		}
	}


	/**
	 * Generate the commands.  Waits for each command to be released before
	 * passing it on.
	 * */
	@Override public synchronized void generateCommands(
		final Battle battle,
		final BattleListener listener,
		final Player forPlayer
	) {
		this.battle = battle;
		this.listener = listener;
		this.forPlayer = forPlayer;

		while (true) {
			try {
				final RecordedLine inLine = new RecordedLine(inReader.readLine());
				if (inLine.isEOF) {
					this.close();
					return;
				}

				System.err.println("Next command " +
					inLine.playerName + "/" + inLine.cmd);

				this.wait(); // wait for this command to be released

				final Command cmd = Command.fromJSON(inLine.cmd);
				final ExecutedCommand ec =
					new ExecutedCommand(cmd, cmd.doCmd(battle));
				Platform.runLater(() -> listener.command(ec.markLastInSequence())); 
				if (battle.battleState.getBattleOutcome(forPlayer).isPresent()) {
					inReader.close();
					return;
				}

				if (cmd instanceof EndTurnCommand) return;

			} catch (IOException|JSONException|ProtocolException e) {
					Platform.runLater(() -> {
						final Alert a = new Alert(AlertType.ERROR,
							e.getMessage(), ButtonType.OK);
						a.setHeaderText("Error parsing playback file");
						a.showAndWait();
					});

					this.close();

			} catch (CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));

			} catch (InterruptedException e) {
				/* ignore */
			}
		}
	}
}

