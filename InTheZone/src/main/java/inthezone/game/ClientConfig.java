package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Client configuration.
 * */
public class ClientConfig implements HasJSONRepresentation {
	private final File configFile =
		new File(GameDataFactory.gameDataCacheDir, "client.json");

	public ClientConfig(GameDataFactory gameData) {
		try (
			BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(configFile), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + configFile);

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			loadConfig(new JSONObject(raw.toString()), gameData);

		} catch (Exception e) {
			System.err.println("Error reading config file: " + e.getMessage());

			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error reading config file \"" + configFile.toString() + "\"");
			a.setContentText(e.getMessage());
			a.showAndWait();

			resetConfigFile();
		}
	}

	public Optional<String> defaultPlayerName = Optional.empty();
	public final Collection<Loadout> loadouts = new ArrayList<>();

	public void loadConfig(JSONObject json, GameDataFactory gameData)
		throws CorruptDataException
	{
		this.defaultPlayerName = Optional.empty();
		loadouts.clear();

		try {
			final String name = json.optString("name", null);
			final JSONArray loadouts = json.optJSONArray("loadouts");

			this.defaultPlayerName = Optional.ofNullable(name);
			if (loadouts != null) {
				final List<JSONObject> ls =
					jsonArrayToList(loadouts, JSONObject.class);
				for (JSONObject l : ls) this.loadouts.add(Loadout.fromJSON(l, gameData));
			}

		} catch (JSONException|ClassCastException e) {
			throw new CorruptDataException("Type error in config file");
		}
	}

	private void resetConfigFile() {
		defaultPlayerName = Optional.empty();
		loadouts.clear();
		writeConfig();
	}

	@Override
	public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		defaultPlayerName.ifPresent(n -> o.put("name", n));
		final JSONArray a = new JSONArray();
		for (Loadout l : loadouts) a.put(l.getJSON());
		o.put("loadouts", a);
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
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Cannot create configuration file \"" +
				configFile.toString() + "\".  Your loadouts cannot be saved.");
			a.setContentText(e.getMessage());
			a.showAndWait();
		}
	}

	private static <T> List<T> jsonArrayToList(JSONArray a, Class<T> clazz)
		throws ClassCastException
	{
		final List<T> r = new ArrayList<>();
		int limit = a.length();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}
}

