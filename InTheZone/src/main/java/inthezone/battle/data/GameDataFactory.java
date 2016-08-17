package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.engine.Stage;
import isogame.resource.DevelopmentResourceLocator;
import isogame.resource.ResourceLocator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameDataFactory implements HasJSONRepresentation {
	private final Library globalLibrary;
	private final ResourceLocator loc;
	public static final String globalLibraryName = "global_library.json";
	public static final String gameDataName = "game_data.json";
	public static final File gameDataCacheDir =
		new File(System.getProperty("user.home"), ".inthezone");

	private JSONObject json = null;

	public GameDataFactory(Optional<File> baseDir)
		throws IOException, CorruptDataException
	{
		InputStream gameData;
		File gameDataFile;

		if (baseDir.isPresent()) {
			this.loc = new DevelopmentResourceLocator(baseDir.get());
		} else {
			this.loc = new CompiledResourceLocator(gameDataCacheDir);
		}

		this.globalLibrary = Library.fromFile(
			loc.globalLibrary(), loc.globalLibraryFilename(), loc, null);

		gameData = loc.gameData();

		// load the game data
		try (BufferedReader in =
			new BufferedReader(new InputStreamReader(gameData, "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + loc.gameDataFilename().toString());
			System.err.println(loc.gameDataFilename().toString());
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(in);
			loadGameData(json);
		} catch (ParseException e) {
			throw new CorruptDataException("game data is corrupted");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		return json;
	}

	public void update(JSONObject json) throws CorruptDataException {
		stages.clear();
		characters.clear();
		loadGameData(json);
	}

	/**
	 * Update the cached game data to this version of the game data.
	 * */
	public void update() {
		// TODO: implement this
	}

	private void loadGameData(JSONObject json) throws CorruptDataException {
		this.json = json;
		Object oVersion = json.get("version");
		Object oVersionNumber = json.get("versionNumber");
		Object oStages = json.get("stages");
		Object oCharacters = json.get("characters");

		if (oVersion == null) throw new CorruptDataException("No version in game data");
		if (oStages == null) throw new CorruptDataException("No stages in game data");
		if (oCharacters == null) throw new CorruptDataException("No characters in game data");

		try {
			this.version = UUID.fromString((String) oVersion);
			this.versionNumber = oVersionNumber == null?
				0 : ((Number) oVersionNumber).intValue();

			JSONArray aStages = (JSONArray) oStages;
			JSONArray aCharacters = (JSONArray) oCharacters;

			for (Object x : aStages) {
				Stage i = Stage.fromJSON(
					(JSONObject) x, loc, globalLibrary);
				stages.put(i.name, i);
			}

			for (Object x : aCharacters) {
				CharacterInfo i = CharacterInfo.fromJSON((JSONObject) x, globalLibrary);
				characters.put(i.name, i);
			}

		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in game data: ", e);
		} catch (IllegalArgumentException e) {
			throw new CorruptDataException("Type error in game data: ", e);
		}
	}

	private UUID version;
	private int versionNumber;
	private Map<String, Stage> stages = new HashMap<>();
	private Map<String, CharacterInfo> characters = new HashMap<>();

	public UUID getVersion() {
		return version;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	/**
	 * May return null.
	 * */
	public Stage getStage(String name) {
		return stages.get(name);
	}

	public Collection<Stage> getStages() {
		return stages.values();
	}

	/**
	 * Get all the sprites in the global library.
	 * */
	public Collection<SpriteInfo> getGlobalSprites() {
		return globalLibrary.allSprites();
	}

	/**
	 * May return null
	 * */
	public CharacterInfo getCharacter(String name) {
		return characters.get(name);
	}

	public Collection<CharacterInfo> getCharacters() {
		return characters.values();
	}

	/**
	 * Write game data out to a file
	 * */
	@SuppressWarnings("unchecked")
	public void writeToStream(
		OutputStream outStream,
		Collection<File> stages,
		Collection<CharacterInfo> characters
	) throws IOException {
		try (PrintWriter out =
			new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"));
		) {
			JSONObject o = new JSONObject();
			JSONArray s = new JSONArray();
			JSONArray c = new JSONArray();
			JSONArray w = new JSONArray();
			for (File stage : stages) {
				s.add(parseStage(stage));
			}
			for (CharacterInfo character : characters) {
				c.add(character.getJSON());
			}
			o.put("version", UUID.randomUUID().toString());
			o.put("versionNumber", ++versionNumber);
			o.put("stages", s);
			o.put("characters", c);
			out.print(o);
		}
	}

	private JSONObject parseStage(File stage) throws IOException {
		try (BufferedReader in =
			new BufferedReader(new InputStreamReader(new FileInputStream(stage), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + stage.toString());
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(in);
			return json;
		} catch (ParseException e) {
			throw new IOException("stage file \"" + stage + "\" is corrupted");
		}
	}
}

