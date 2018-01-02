package inthezone.battle.data;

import isogame.resource.ResourceLocator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A locator for resources compiled into the client jar files
 * */
public class CompiledResourceLocator implements ResourceLocator {
	private final Optional<File> gameDataCacheDir;

	public CompiledResourceLocator(final Optional<File> gameDataCacheDir) {
		this.gameDataCacheDir = gameDataCacheDir;
	}

	@Override
	public InputStream gfx(final String file) throws IOException {
		final InputStream r =
			this.getClass().getResourceAsStream("/gamedata/gfx/" + file);
		if (r != null) return r; else
			throw new FileNotFoundException("Missing gfx resource " + file);
	}

	@Override
	public InputStream sfx(final String file) throws IOException {
		final InputStream r =
			this.getClass().getResourceAsStream("/gamedata/sfx/" + file);
		if (r != null) return r; else
			throw new FileNotFoundException("Missing sfx resource " + file);
	}

	private int getStreamVersionNumber(final InputStream s) {
		try (final BufferedReader in =
			new BufferedReader(new InputStreamReader(s, "UTF-8"))
		) {
			if (in == null) return 0;

			final StringBuilder raw = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) raw.append(line);

			return (new JSONObject(raw.toString())).optInt("versionNumber", 0);

		} catch (final JSONException|IOException e) {
			return 0;
		}
	}

	private void copyGameData(final File gameDataFile) throws IOException {
		final File cache = gameDataCacheDir.get();
		// copy the compiled-in version to make a new cached version
		if (!cache.exists()) cache.mkdir();
		final OutputStream fout = new FileOutputStream(gameDataFile);
		final InputStream fin = internalGameData();
		int b;
		while ((b = fin.read()) != -1) fout.write(b);
		fout.close();
		fin.close();
	}

	@Override
	public InputStream gameData() throws IOException {
		if (!gameDataCacheDir.isPresent()) {
			return internalGameData();
		} else {
			final File gameDataFile = new File(gameDataFilename());

			if (!gameDataFile.exists()) {
				copyGameData(gameDataFile);
			} else {
				final int internalVN = getStreamVersionNumber(internalGameData());
				final int cachedVN = getStreamVersionNumber(
					new FileInputStream(gameDataFile));
				if (internalVN > cachedVN) {
					copyGameData(gameDataFile);
				}
			}

			return new FileInputStream(gameDataFile);
		}
	}

	public InputStream internalGameData() throws IOException {
		final InputStream r =
			this.getClass().getResourceAsStream("/gamedata/game_data.json");
		if (r != null) return r; else
			throw new FileNotFoundException("Missing internal game data");
	}

	@Override
	public InputStream globalLibrary() throws IOException {
		final InputStream r =
			this.getClass().getResourceAsStream(globalLibraryFilename());
		if (r != null) return r; else
			throw new FileNotFoundException("Missing global library");
	}

	@Override
	public String gameDataFilename() {
		if (gameDataCacheDir.isPresent()) {
			return (new File(gameDataCacheDir.get(), "game_data.json")).toString();
		} else {
			return "/gamedata/game_data.json";
		}
	}

	@Override
	public String globalLibraryFilename() {
		return "/gamedata/global_library.json";
	}
}

