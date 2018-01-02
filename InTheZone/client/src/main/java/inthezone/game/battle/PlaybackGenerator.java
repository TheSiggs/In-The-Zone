package inthezone.game.battle;

import static javafx.scene.control.Alert.AlertType;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.BattleListener;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.ResignReason;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import ssjsjs.JSONDeserializeException;
import ssjsjs.SSJSJS;

/**
 * A command generator that reads commands from a recorded game (to facilitate
 * replaying recorded games.
 * */
public class PlaybackGenerator implements CommandGenerator {
	private BufferedReader inReader = null;

	/**
	 * @param inReader the file containing the game to replay
	 * @param gameData the game data
	 * */
	public StartBattleCommand start(
		final BufferedReader inReader,
		final GameDataFactory gameData
	) throws IOException, ProtocolException {
		this.inReader = inReader;

		try {
			final RecordedLine inLine = new RecordedLine(inReader.readLine());
			System.err.println("Recorded game was started by " + inLine.playerName);
			final Map<String, Object> env = new HashMap<>();
			env.put("gameData", gameData);
			return SSJSJS.deserialize(inLine.cmd, StartBattleCommand.class, env);
		} catch (final JSONException|JSONDeserializeException e) {
			throw new ProtocolException("Error parsing JSON", e);
		}
	}

	/**
	 * A line from a recorded game file.
	 * */
	class RecordedLine {
		public final String playerName;
		public final JSONObject cmd;
		public final boolean isEOF;

		/**
		 * @param raw the raw unparsed line
		 * */
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
					final Command cmd = new ResignCommand(
						forPlayer, ResignReason.LOGGED_OFF);
					final ExecutedCommand ec =
						new ExecutedCommand(cmd, cmd.doCmd(battle));
					Platform.runLater(() -> listener.command(ec.markLastInSequence())); 
				}

			} catch (final CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));

			} catch (final IOException e) {
				/* ignore */
			}
		}
	}


	/**
	 * Generate the commands.  Waits for each command to be released before
	 * passing it on.
	 * @param battle the Battle object
	 * @param listener the handler to send the commands to
	 * @param forPlayer which player we are generating commands for
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

			} catch (final IOException|JSONException|ProtocolException e) {
				Platform.runLater(() -> {
					final Alert a = new Alert(AlertType.ERROR,
						e.getMessage(), ButtonType.OK);
					a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
					a.setHeaderText("Error parsing playback file");
					a.showAndWait();
				});

				this.close();
				return;

			} catch (final CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));
				return;

			} catch (final InterruptedException e) {
				/* ignore */
			}
		}
	}
}

