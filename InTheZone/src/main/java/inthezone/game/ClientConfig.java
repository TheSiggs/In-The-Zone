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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(in);
			loadConfig(json, gameData);
		} catch (Exception e) {
			System.err.println("Error reading config file: " + e.getMessage());
			resetConfigFile();
		}
	}

	public Optional<String> defaultPlayerName = Optional.empty();
	public final Collection<Loadout> loadouts = new ArrayList<>();

	public void loadConfig(JSONObject json, GameDataFactory gameData)
		throws CorruptDataException
	{
		defaultPlayerName = Optional.empty();
		loadouts.clear();

		Object oname = json.get("name");
		Object oloadouts = json.get("loadouts");
		try {
			defaultPlayerName = Optional.ofNullable(oname).map(x -> (String) x);
			if (oloadouts != null) {
				final List<JSONObject> ls =
					jsonArrayToList((JSONArray) oloadouts, JSONObject.class);
				for (JSONObject l : ls) loadouts.add(Loadout.fromJSON(l, gameData));
			}
		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in config file");
		}
	}

	private void resetConfigFile() {
		defaultPlayerName = Optional.empty();
		loadouts.clear();
		writeConfig();
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		defaultPlayerName.ifPresent(n -> o.put("name", n));
		JSONArray a = new JSONArray();
		for (Loadout l : loadouts) a.add(l.getJSON());
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
			a.setHeaderText("Cannot create configuration file " +
				configFile.toString() + ".  Your loadouts cannot be saved.");
			a.showAndWait();
		}
	}

	private static <T> List<T> jsonArrayToList(JSONArray a, Class<T> clazz)
		throws ClassCastException
	{
		List<T> r = new ArrayList<>();
		int limit = a.size();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}
}

