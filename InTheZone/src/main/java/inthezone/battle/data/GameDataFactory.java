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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameDataFactory implements HasJSONRepresentation {
	private final Library globalLibrary;
	private final StandardSprites standardSprites;
	public final ResourceLocator loc;
	public static final String globalLibraryName = "global_library.json";
	public static final String gameDataName = "game_data.json";
	public static final File gameDataCacheDir =
		new File(System.getProperty("user.home"), ".inthezone");
	
	private final boolean updateCache;

	private JSONObject json = null;

	public GameDataFactory(Optional<File> baseDir, boolean useInternal)
		throws IOException, CorruptDataException
	{
		this(baseDir, useInternal, false);
	}

	public GameDataFactory(
		Optional<File> baseDir, boolean useInternal, boolean nofx
	)
		throws IOException, CorruptDataException
	{
		if (baseDir.isPresent()) {
			this.loc = new DevelopmentResourceLocator(baseDir.get());
			this.updateCache = false;
		} else {
			this.loc = new CompiledResourceLocator(
				Optional.ofNullable(useInternal? null : gameDataCacheDir));
			this.updateCache = true;
		}

		this.globalLibrary = Library.fromFile(
			loc.globalLibrary(), loc.globalLibraryFilename(), loc, null, nofx);

		this.standardSprites = new StandardSprites(globalLibrary, loc);

		// load the game data
		try (BufferedReader in =
			new BufferedReader(new InputStreamReader(loc.gameData(), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + loc.gameDataFilename().toString());
			System.err.println(loc.gameDataFilename().toString());

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			loadGameData(new JSONObject(raw.toString()));

		} catch (JSONException e) {
			throw new CorruptDataException("game data is corrupted", e);
		}
	}

	@Override
	public JSONObject getJSON() {return json;}

	public void update(JSONObject json) throws CorruptDataException {
		stages.clear();
		characters.clear();
		loadGameData(json);

		if (updateCache) {
			try (PrintWriter out =
				new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					loc.gameDataFilename()), "UTF-8"));
			) {
				out.print(json);
			} catch (IOException e) {
				throw new CorruptDataException("IO error writing back to cache", e);
			}
		}
	}

	private void loadGameData(JSONObject json) throws CorruptDataException {
		try {
			this.json = json;
			this.version = UUID.fromString(json.getString("version"));
			this.versionNumber = json.optInt("versionNumber", 0);
			final JSONArray aStages = json.getJSONArray("stages");
			final JSONArray aCharacters = json.getJSONArray("characters");

			for (Object x : aStages) {
				Stage i = Stage.fromJSON(
					(JSONObject) x, loc, globalLibrary);
				stages.put(i.name, i);
			}

			for (Object x : aCharacters) {
				CharacterInfo i =
					CharacterInfo.fromJSON((JSONObject) x, loc, globalLibrary);
				characters.put(i.name, i);
			}

		} catch (JSONException e) {
			throw new CorruptDataException("Error in game data: " + e.getMessage(), e);

		} catch (ClassCastException|IllegalArgumentException e) {
			throw new CorruptDataException("Type error in game data: " + e.getMessage(), e);
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
		Stage r = stages.get(name);
		return r == null? null : r.clone();
	}

	public Collection<Stage> getStages() {
		return stages.values();
	}

	public int getPriorityLevel(String l) {
		return globalLibrary.priorities.indexOf(l);
	}

	/**
	 * Get all the sprites in the global library.
	 * */
	public Collection<SpriteInfo> getGlobalSprites() {
		return globalLibrary.allSprites();
	}

	/**
	 * Get the standard sprites.
	 * */
	public StandardSprites getStandardSprites() {
		return standardSprites;
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
	public void writeToStream(
		OutputStream outStream,
		Collection<File> stages,
		Collection<CharacterInfo> characters
	) throws IOException {
		try (PrintWriter out =
			new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"));
		) {
			final JSONObject o = new JSONObject();
			final JSONArray s = new JSONArray();
			final JSONArray c = new JSONArray();
			final JSONArray w = new JSONArray();

			for (File stage : stages) s.put(parseStage(stage));
			for (CharacterInfo character : characters) c.put(character.getJSON());

			o.put("version", UUID.randomUUID().toString());
			o.put("versionNumber", ++versionNumber);
			o.put("stages", s);
			o.put("characters", c);

			out.print(o.toString(2));
		}
	}

	private JSONObject parseStage(File stage) throws IOException {
		try (BufferedReader in =
			new BufferedReader(new InputStreamReader(new FileInputStream(stage), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + stage.toString());

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			return new JSONObject(raw.toString());

		} catch (JSONException e) {
			throw new IOException("stage file \"" + stage + "\" is corrupted");
		}
	}
}

