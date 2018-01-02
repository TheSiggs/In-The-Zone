package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import isogame.engine.CorruptDataException;
import isogame.engine.CorruptDataException;
import isogame.engine.KeyBinding;
import isogame.engine.KeyBindingTable;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import ssjsjs.JSONDeserializeException;
import ssjsjs.JSONSerializeException;
import ssjsjs.SSJSJS;

/**
 * Client configuration.
 * */
public class ClientConfig {
	private final GameDataFactory gameData;

	// The configuration file
	private final File configFile =
		new File(GameDataFactory.gameDataCacheDir, "client.json");
	
	// The configuration data
	private ClientConfigData data = null;

	// The default key binding table
	public static final KeyBindingTable defaultKeyBindingTable = new KeyBindingTable();

	/**
	 * @param gameData the game data
	 * */
	public ClientConfig(final GameDataFactory gameData) {
		this.gameData = gameData;

		defaultKeyBindingTable.setSecondaryKey(KeyBinding.scrollUp        , new KeyCodeCombination(KeyCode.UP)    );
		defaultKeyBindingTable.setSecondaryKey(KeyBinding.scrollDown      , new KeyCodeCombination(KeyCode.DOWN)  );
		defaultKeyBindingTable.setSecondaryKey(KeyBinding.scrollLeft      , new KeyCodeCombination(KeyCode.LEFT)  );
		defaultKeyBindingTable.setSecondaryKey(KeyBinding.scrollRight     , new KeyCodeCombination(KeyCode.RIGHT) );
		defaultKeyBindingTable.setPrimaryKey(  KeyBinding.rotateLeft      , new KeyCodeCombination(KeyCode.Q)     );
		defaultKeyBindingTable.setPrimaryKey(  KeyBinding.rotateRight     , new KeyCodeCombination(KeyCode.E)     );
		defaultKeyBindingTable.setPrimaryKey(  KeyBinding.scrollUp        , new KeyCodeCombination(KeyCode.W)     );
		defaultKeyBindingTable.setPrimaryKey(  KeyBinding.scrollLeft      , new KeyCodeCombination(KeyCode.A)     );
		defaultKeyBindingTable.setPrimaryKey(  KeyBinding.scrollDown      , new KeyCodeCombination(KeyCode.S)     );
		defaultKeyBindingTable.setPrimaryKey(  KeyBinding.scrollRight     , new KeyCodeCombination(KeyCode.D)     );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.cancel , new KeyCodeCombination(KeyCode.ESCAPE));
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.enter  , new KeyCodeCombination(KeyCode.ENTER) );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.altpath, new KeyCodeCombination(KeyCode.TAB)   );

		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.next   ,
			new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN) );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.prev   ,
			new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN)  );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.character1, new KeyCodeCombination(KeyCode.Z)  );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.character2, new KeyCodeCombination(KeyCode.X)  );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.character3, new KeyCodeCombination(KeyCode.C)  );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.character4, new KeyCodeCombination(KeyCode.V)  );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.clearSelected, new KeyCodeCombination(KeyCode.BACK_SPACE));
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.attack , new KeyCodeCombination(KeyCode.DIGIT1)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.push   , new KeyCodeCombination(KeyCode.DIGIT2)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.potion , new KeyCodeCombination(KeyCode.DIGIT3)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.special, new KeyCodeCombination(KeyCode.DIGIT4)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.a1     , new KeyCodeCombination(KeyCode.DIGIT5)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.a2     , new KeyCodeCombination(KeyCode.DIGIT6)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.a3     , new KeyCodeCombination(KeyCode.DIGIT7)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.a4     , new KeyCodeCombination(KeyCode.DIGIT8)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.a5     , new KeyCodeCombination(KeyCode.DIGIT9)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.a6     , new KeyCodeCombination(KeyCode.DIGIT0)   );
		defaultKeyBindingTable.setPrimaryKey(  InTheZoneKeyBinding.endTurn, new KeyCodeCombination(KeyCode.END)   );

		gameData.addUpdateWatcher(() ->
			Platform.runLater(() -> loadConfigFile(configFile, gameData)));
		loadConfigFile(configFile, gameData);
	}

	/**
	 * Load configuration data from a file.
	 * @param configFile the configuration file
	 * @param gameData the game data
	 * */
	private void loadConfigFile(
		final File configFile, final GameDataFactory gameData
	) {
		try (
			final BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(configFile), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + configFile);

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			loadConfig(new JSONObject(raw.toString()), gameData);

		} catch (final Exception e) {
			System.err.println("Error reading config file: " + e.getMessage());
			resetConfigFile();
		}
	}

	/**
	 * Get a default name for a new loadout.
	 * */
	public String newLoadoutName() {
		if (data == null) throw new NullPointerException("Missing configuration");

		final String base = "New loadout";
		String r = base;
		int n = 2;
		while (!isValidLoadoutName(r)) {
			r = base + " " + n;
			n += 1;
		}
		return r;
	}

	/**
	 * Determine if a loadout name is value.
	 * @param n the loadout name
	 * */
	public boolean isValidLoadoutName(final String n) {
		if (data == null) throw new NullPointerException("Missing configuration");

		return !data.loadouts.stream().anyMatch(l -> l.name.equals(n));
	}

	/**
	 * Update a loadout.
	 * @param id the id number of the loadout
	 * @param loadout the new loadout
	 * */
	public void setLoadout(final int id, final Loadout loadout) {
		if (data == null) throw new NullPointerException("Missing configuration");

		data.loadouts.set(id, loadout);
		writeConfig();
	}

	/**
	 * Add a new loadout.
	 * @param loadout the new loadout
	 * */
	public void addLoadout(final Loadout loadout) {
		if (data == null) throw new NullPointerException("Missing configuration");

		data.loadouts.add(0, loadout);
		writeConfig();
	}

	/**
	 * Get the id number of a loadout.
	 * @param loadout the loadout to search for
	 * @return the loadout id or -1 if there is no such loadout
	 * */
	public int getLoadoutID(final Loadout loadout) {
		if (data == null) throw new NullPointerException("Missing configuration");

		return data.loadouts.indexOf(loadout);
	}

	/**
	 * Delete a loadout.
	 * @param loadout the loadout to delete
	 * */
	public void deleteLoadout(final Loadout loadout) {
		if (data == null) throw new NullPointerException("Missing configuration");

		data.loadouts.remove(loadout);
		writeConfig();
	}

	/**
	 * Get the loadouts.
	 * */
	public List<Loadout> getLoadouts() {
		if (data == null) throw new NullPointerException("Missing configuration");

		return data.loadouts;
	}

	/**
	 * Get the keybinding table.
	 * */
	public KeyBindingTable getKeyBindingTable() {
		if (data == null) throw new NullPointerException("Missing configuration");

		return data.keyBindings;
	}

	/**
	 * Load new keybindings.
	 * @param table the new keybindings table
	 * */
	public void loadKeyBindings(final KeyBindingTable table) {
		if (data == null) throw new NullPointerException("Missing configuration");

		data.keyBindings.loadBindings(table);
		writeConfig();
	}

	/**
	 * The server to connect to.
	 * */
	public String getServer() {
		if (data == null) throw new NullPointerException("Missing configuration");

		return data.server;
	}

	/**
	 * The server port to connect to.
	 * */
	public int getPort() {
		if (data == null) throw new NullPointerException("Missing configuration");

		return data.port;
	}

	/**
	 * The default player name for when connecting to the server
	 * */
	public Optional<String> getDefaultPlayerName() {
		if (data == null) throw new NullPointerException("Missing configuration");

		return data.defaultPlayerName;
	}

	/**
	 * Set the server.
	 * @param server the new server
	 * */
	public void setServer(final String server) {
		if (data == null) throw new NullPointerException("Missing configuration");

		this.data = this.data.setServer(server);
		writeConfig();
	}

	/**
	 * Set the server port.
	 * @param port the new server port
	 * */
	public void setPort(final int port) {
		if (data == null) throw new NullPointerException("Missing configuration");

		this.data = this.data.setPort(port);
		writeConfig();
	}

	/**
	 * Load the configuration from JSON
	 * @param json the JSON containing the configuration data
	 * @param gameData the game data to load
	 * */
	public void loadConfig(
		final JSONObject json, final GameDataFactory gameData
	) throws CorruptDataException, JSONDeserializeException {
		final Map<String, Object> env = new HashMap<>();
		env.put("gameData", gameData);
		env.put("defaultKeyBindingTable", defaultKeyBindingTable);
		this.data = SSJSJS.deserialize(json, ClientConfigData.class, env);
	}

	/**
	 * Erase all customizations and replace the configuration file with a
	 * default one.
	 * */
	private void resetConfigFile() {
		this.data = new ClientConfigData(gameData, defaultKeyBindingTable);
		writeConfig();
	}

	/**
	 * Write the configuration file to a JSON file.
	 * */
	private void writeConfig() {
		if (!GameDataFactory.gameDataCacheDir.exists()) {
			GameDataFactory.gameDataCacheDir.mkdir();
		}

		try (
			final PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(configFile), "UTF-8"))
		) {
			out.print(SSJSJS.serialize(data).toString());
		} catch (final IOException|JSONSerializeException e) {
			e.printStackTrace();
			final Alert a = new Alert(Alert.AlertType.ERROR,
				e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Cannot create configuration file \"" +
				configFile.toString() + "\".  Your loadouts cannot be saved.");
			a.setContentText(e.getMessage());
			a.showAndWait();
		}
	}
}

