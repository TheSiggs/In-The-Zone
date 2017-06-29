package inthezone.game.battle;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONObject;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.BattleListener;
import inthezone.protocol.ProtocolException;

public class PlaybackGenerator implements CommandGenerator {
	public PlaybackGenerator() {
	}

	private BufferedReader inReader = null;

	public StartBattleCommand start(
		final BufferedReader inReader,
		final GameDataFactory gameData
	) throws IOException, ProtocolException {
		this.inReader = inReader;

		final RecordedLine inLine = new RecordedLine(inReader.readLine());
		System.err.println("Recorded game was started by " + inLine.playerName);
		return StartBattleCommand.fromJSON(inLine.cmd, gameData);
	}

	class RecordedLine {
		public final String playerName;
		public final JSONObject cmd;

		public RecordedLine(final String raw) {
			final int i = raw.indexOf("/");
			this.playerName = raw.substring(0, i);
			this.cmd = new JSONObject(raw.substring(i + 1));
		}
	}

	@Override public void generateCommands(
		final Battle battle,
		final BattleListener listener,
		final Player forPlayer
	) {
	}
}

