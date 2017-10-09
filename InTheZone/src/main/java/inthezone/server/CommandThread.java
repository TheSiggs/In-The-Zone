package inthezone.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import inthezone.Log;

/**
 * A thread which monitors a pipe listening for management commands.
 * */
public class CommandThread implements Runnable {
	private final File commandPipe;

	public CommandThread(
		final File commandPipe
	) {
		this.commandPipe = commandPipe;
	}

	@Override public void run() {
		try (
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(commandPipe), "UTF-8"))
		) {
			for (String ln = in.readLine(); ln != null; ln = in.readLine()) {
				final String line = ln.trim().toLowerCase();
				if (line.equals("terminate")) {
					// The terminate command
					Log.info("Terminate message received, shutting down now", null);
					System.exit(3);
				} else {
					Log.warn("Unexpected command \"" + ln + "\"", null);
				}
			}

		} catch (final IOException e) {
			Log.error("Error reading command pipe", e);
		}
	}
}

