package inthezone.battle.data;

import isogame.resource.ResourceLocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public class CompiledResourceLocator implements ResourceLocator {
	private final Optional<File> gameDataCacheDir;

	public CompiledResourceLocator(Optional<File> gameDataCacheDir) {
		this.gameDataCacheDir = gameDataCacheDir;
	}

	@Override
	public InputStream gfx(String file) throws IOException {
		InputStream r = this.getClass().getResourceAsStream("/gamedata/gfx/" + file);
		if (r != null) return r; else
			throw new FileNotFoundException("Missing gfx resource " + file);
	}

	@Override
	public InputStream sfx(String file) throws IOException {
		InputStream r = this.getClass().getResourceAsStream("/gamedata/sfx/" + file);
		if (r != null) return r; else
			throw new FileNotFoundException("Missing sfx resource " + file);
	}

	@Override
	public InputStream gameData() throws IOException {
		if (!gameDataCacheDir.isPresent()) {
			return internalGameData();
		} else {
			File gameDataFile = new File(gameDataFilename());

			if (!gameDataFile.exists()) {
				File cache = gameDataCacheDir.get();
				// copy the compiled-in version to make a new cached version
				if (!cache.exists()) cache.mkdir();
				OutputStream fout = new FileOutputStream(gameDataFile);
				InputStream fin = internalGameData();
				int b;
				while ((b = fin.read()) != -1) fout.write(b);
				fout.close();
				fin.close();
			}

			return new FileInputStream(gameDataFile);
		}
	}

	public InputStream internalGameData() throws IOException {
		InputStream r = this.getClass().getResourceAsStream("/gamedata/game_data.json");
		if (r != null) return r; else
			throw new FileNotFoundException("Missing internal game data");
	}

	@Override
	public InputStream globalLibrary() throws IOException {
		InputStream r = this.getClass().getResourceAsStream(globalLibraryFilename());
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

