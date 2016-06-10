package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import isogame.engine.CorruptDataException;
import javafx.scene.control.Alert;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Optional;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Client configuration.
 * */
public class ClientConfig {
	private final File configFile =
		new File(GameDataFactory.gameDataCacheDir, "client.json");

	public ClientConfig() {
		boolean doResetFile = false;

		try (
			BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(configFile), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + configFile);
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(in);
			loadConfig(json);
		} catch (Exception e) {
			System.err.println("Error reading config file: " + e.getMessage());
			doResetFile = true;
		}

		if (doResetFile) resetConfigFile();
	}

	private Optional<String> defaultPlayerName = Optional.empty();

	public void loadConfig(JSONObject json) throws CorruptDataException {
		Object oname = json.get("name");
		try {
			defaultPlayerName = Optional.ofNullable(oname).map(x -> (String) x);
		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in config file");
		}
	}

	public Optional<String> getDefaultPlayerName() {
		return defaultPlayerName;
	}

	private void resetConfigFile() {
		// TODO: truncate the existing config file, reset it to defaults
	}
}

