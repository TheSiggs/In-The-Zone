package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.engine.Stage;
import isogame.resource.DevelopmentResourceLocator;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ssjsjs.JSONdecodeException;
import ssjsjs.JSONencodeException;
import ssjsjs.SSJSJS;

/**
 * Constructs game data objects.
 * */
public class GameDataFactory {
	private final Library globalLibrary;
	private final StandardSprites standardSprites;
	public final ResourceLocator loc;
	public static final String globalLibraryName = "global_library.json";
	public static final String gameDataName = "game_data.json";
	public static final File gameDataCacheDir =
		new File(System.getProperty("user.home"), ".inthezone");
	
	private final boolean updateCache;

	private JSONObject json = null;
	public JSONObject getJSON() {return json;}

	private final Set<Runnable> updateWatchers = new HashSet<>();

	public void addUpdateWatcher(final Runnable update) {
		updateWatchers.add(update);
	}

	public void removeUpdateWatcher(final Runnable update) {
		updateWatchers.remove(update);
	}

	public GameDataFactory(final Optional<File> baseDir, boolean useInternal)
		throws IOException, CorruptDataException
	{
		this(baseDir, useInternal, false);
	}

	public GameDataFactory(
		final Optional<File> baseDir,
		final boolean useInternal,
		final boolean nofx
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
		try (final BufferedReader in =
			new BufferedReader(new InputStreamReader(loc.gameData(), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + loc.gameDataFilename().toString());
			System.err.println(loc.gameDataFilename().toString());

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			loadGameData(new JSONObject(raw.toString()));

		} catch (final JSONException e) {
			throw new CorruptDataException("game data is corrupted", e);
		}
	}

	public void update(final JSONObject json) throws CorruptDataException {
		stages.clear();
		characters.clear();
		loadGameData(json);

		if (updateCache) {
			try (final PrintWriter out =
				new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					loc.gameDataFilename()), "UTF-8"));
			) {
				out.print(json);
			} catch (final IOException e) {
				throw new CorruptDataException("IO error writing back to cache", e);
			}
		}

		for (final Runnable r : updateWatchers) r.run();
	}

	private void loadGameData(final JSONObject json) throws CorruptDataException {
		final Map<String, Object> env = new HashMap<>();
		env.put("locator", loc);
		env.put("library", globalLibrary);

		try {
			this.json = json;
			this.version = UUID.fromString(json.getString("version"));
			this.versionNumber = json.optInt("versionNumber", 0);
			final JSONArray aStages = json.getJSONArray("stages");
			final JSONArray aCharacters = json.getJSONArray("characters");

			for (final Object x : aStages) {
				final String name = ((JSONObject) x).optString("name");
				final Stage i = Stage.fromJSON(name, (JSONObject) x, loc, globalLibrary);
				stages.put(i.name, i);
			}

			for (final Object x : aCharacters) {
				final CharacterInfo i = SSJSJS.decode((JSONObject) x, CharacterInfo.class, env);
				characters.put(i.name, i);
			}

		} catch (final JSONException|JSONdecodeException e) {
			throw new CorruptDataException("Error in game data: " + e.getMessage(), e);

		} catch (final ClassCastException|IllegalArgumentException e) {
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
	public Stage getStage(final String name) {
		final Stage r = stages.get(name);
		return r == null? null : r.clone();
	}

	/**
	 * Get the thumbnail image for a stage.  May return null.
	 * */
	public Image getThumbnail(final Stage stage) {
		try {
			return new Image(loc.gfx("mapThumbs/" + stage.name + ".png"));
		} catch (final IOException e) {
			System.err.println("No thumbnail for " + stage.name);
			e.printStackTrace();
			return null;
		}
	}

	public Collection<Stage> getStages() {
		return stages.values();
	}

	public int getPriorityLevel(final String l) {
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
	public CharacterInfo getCharacter(final String name) {
		return characters.get(name);
	}

	public Collection<CharacterInfo> getCharacters() {
		return characters.values();
	}

	/**
	 * Write game data out to a file
	 * */
	public void writeToStream(
		final OutputStream outStream,
		final Collection<File> stages,
		final Collection<CharacterInfo> characters
	) throws IOException, CorruptDataException {
		try (final PrintWriter out =
			new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"));
		) {
			final JSONObject o = new JSONObject();
			final JSONArray s = new JSONArray();
			final JSONArray c = new JSONArray();

			for (final File stage : stages)
				s.put(parseStage(stage));

			for (final CharacterInfo character : characters)
				c.put(SSJSJS.encode(character));

			o.put("version", UUID.randomUUID().toString());
			o.put("versionNumber", ++versionNumber);
			o.put("stages", s);
			o.put("characters", c);

			out.print(o.toString(2));

		} catch (final JSONencodeException e) {
			throw new CorruptDataException(
				"Cannot serialize game data: " + e.getMessage(), e);
		}
	}

	private JSONObject parseStage(final File stage) throws IOException {
		try (final BufferedReader in =
			new BufferedReader(new InputStreamReader(new FileInputStream(stage), "UTF-8"))
		) {
			if (in == null) throw new FileNotFoundException(
				"File not found " + stage.toString());

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			return new JSONObject(raw.toString());

		} catch (final JSONException e) {
			throw new IOException("stage file \"" + stage + "\" is corrupted");
		}
	}
}

