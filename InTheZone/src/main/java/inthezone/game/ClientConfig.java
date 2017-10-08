package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.server.Server;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.KeyBinding;
import isogame.engine.KeyBindingTable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Client configuration.
 * */
public class ClientConfig implements HasJSONRepresentation {
	private final static String DEFAULT_SERVER = "127.0.0.1"; // TODO: update with the ip of the real sever
	private final static int DEFAULT_PORT = Server.DEFAULT_PORT;

	private final File configFile =
		new File(GameDataFactory.gameDataCacheDir, "client.json");

	private KeyBindingTable keyBindings = new KeyBindingTable();
	public static final KeyBindingTable defaultKeyBindingTable =
		new KeyBindingTable();

	public ClientConfig(final GameDataFactory gameData) {
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

	public Optional<String> defaultPlayerName = Optional.empty();
	public final List<Loadout> loadouts = new ArrayList<>();

	public String server = DEFAULT_SERVER;
	public int port = DEFAULT_PORT;

	public String newLoadoutName() {
		final String base = "New loadout";
		String r = base;
		int n = 2;
		while (!isValidLoadoutName(r)) {
			r = base + " " + n;
			n += 1;
		}
		return r;
	}

	public boolean isValidLoadoutName(final String n) {
		return !loadouts.stream().anyMatch(l -> l.name.equals(n));
	}

	public KeyBindingTable getKeyBindingTable() {
		return keyBindings;
	}

	public void loadConfig(
		final JSONObject json, final GameDataFactory gameData
	) throws CorruptDataException {

		this.defaultPlayerName = Optional.empty();
		loadouts.clear();

		try {
			final String name = json.optString("name", null);
			final JSONArray loadouts = json.optJSONArray("loadouts");
			final JSONObject keys = json.optJSONObject("keys");
			this.server = json.optString("server", DEFAULT_SERVER);
			this.port = json.optInt("port", DEFAULT_PORT);

			this.defaultPlayerName = Optional.ofNullable(name);
			if (loadouts != null) {
				final List<JSONObject> ls =
					jsonArrayToList(loadouts, JSONObject.class);
				for (final JSONObject l : ls)
					this.loadouts.add(Loadout.fromJSON(l, gameData));
			}

			if (keys != null) {
				keyBindings = KeyBindingTable.fromJSON(
					keys, InTheZoneKeyBinding::valueOf);
			} else {
				copyDefaultKeysTable();
			}

		} catch (final JSONException|ClassCastException e) {
			throw new CorruptDataException("Type error in config file");
		}
	}

	private void copyDefaultKeysTable() {
		keyBindings.loadBindings(defaultKeyBindingTable);
	}

	private void resetConfigFile() {
		defaultPlayerName = Optional.empty();
		loadouts.clear();
		copyDefaultKeysTable();
		writeConfig();
	}

	@Override
	public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		defaultPlayerName.ifPresent(n -> o.put("name", n));
		o.put("server", server);
		o.put("port", port);
		final JSONArray a = new JSONArray();
		for (final Loadout l : loadouts) a.put(l.getJSON());
		o.put("loadouts", a);
		o.put("keys", keyBindings.getJSON());

		return o;
	}

	public void writeConfig() {
		if (!GameDataFactory.gameDataCacheDir.exists()) {
			GameDataFactory.gameDataCacheDir.mkdir();
		}

		try (
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(configFile), "UTF-8"))
		) {
			out.print(getJSON().toString());
		} catch (final IOException e) {
			final Alert a = new Alert(Alert.AlertType.ERROR,
				e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Cannot create configuration file \"" +
				configFile.toString() + "\".  Your loadouts cannot be saved.");
			a.setContentText(e.getMessage());
			a.showAndWait();
		}
	}

	private static <T> List<T> jsonArrayToList(
		final JSONArray a, final Class<T> clazz
	) throws ClassCastException {
		final List<T> r = new ArrayList<>();
		int limit = a.length();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}
}

