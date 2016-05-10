package inthezone.battle.data;

import isogame.resource.ResourceLocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CompiledResourceLocator implements ResourceLocator {
	private final File gameDataCacheDir;

	public CompiledResourceLocator(File gameDataCacheDir) {
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
		File gameDataFile = new File(gameDataFilename());

		if (!gameDataFile.exists()) {
			// copy the compiled-in version to make a new cached version
			if (!gameDataCacheDir.exists()) gameDataCacheDir.mkdir();
			OutputStream fout = new FileOutputStream(gameDataFile);
			InputStream fin = internalGameData();
			int b;
			while ((b = fin.read()) != -1) fout.write(b);
			fout.close();
			fin.close();
		}

		return new FileInputStream(gameDataFile);
	}

	public InputStream internalGameData() throws IOException {
		InputStream r = this.getClass().getResourceAsStream(gameDataFilename());
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
		return (new File(gameDataCacheDir, "game_data.json")).toString();
	}

	@Override
	public String globalLibraryFilename() {
		return "/gamedata/global_library.json";
	}
}

