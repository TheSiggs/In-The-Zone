package inthezone.server;

import isogame.engine.CorruptDataException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import inthezone.battle.data.GameDataFactory;
import inthezone.util.Log;

/**
 * The game server
 * */
public class Server {
	public static final int DEFAULT_BACKLOG = 10;
	public static final int DEFAULT_MAXCLIENTS = 1000;
	public static final int DEFAULT_PORT = 8000; // for now!
	public static final File DEFAULT_COMMAND_PIPE =
		new File("server_command.pipe");

	public static final File GAMES_DIR = new File("games");

	private static boolean inGlobalExceptionHandler = false;
	private static void globalExceptionHandler(
		final Thread t, final Throwable e
	) {
		if (inGlobalExceptionHandler) {
			/* Double fault, hard terminate */
			System.exit(100);
		}

		inGlobalExceptionHandler = true;
		Log.fatal("Unexpected exception", e);
		System.exit(99);
		inGlobalExceptionHandler = false;
	}

	public static void main(final String args[]) {
		final Map<String, String> parsedArgs = new HashMap<>();
		for (final String arg : args) {
			final String[] p = arg.split("=");
			if (p.length != 2) {
				System.out.println("Invalid command line option " + arg);
				commandLineError();
			}
			parsedArgs.put(p[0], p[1]);
		}

		final Optional<File> baseDir = 
			Optional.ofNullable(parsedArgs.get("--basedir"))
			.map(x -> (new File(x)).getAbsoluteFile());

		try {
			if (!GAMES_DIR.exists()) GAMES_DIR.mkdirs();
			if (!GAMES_DIR.isDirectory()) {
				Log.fatal("Cannot create saved games directory ./" + GAMES_DIR, null);
				System.exit(1);
			}

			final int port = Optional.ofNullable(parsedArgs.get("--port"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_PORT);
			final int backlog = Optional.ofNullable(parsedArgs.get("--backlog"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_BACKLOG);
			final String name = Optional.ofNullable(parsedArgs.get("--name"))
				.orElse(InetAddress.getLocalHost().getHostName());
			final int maxClients = Optional.ofNullable(parsedArgs.get("--maxClients"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_MAXCLIENTS);
			final File commandPipe =
				Optional.ofNullable(parsedArgs.get("--command-pipe"))
					.map(s -> new File(s)).orElse(DEFAULT_COMMAND_PIPE)
					.getAbsoluteFile();

			Log.info("Starting server \"" + name + "\" on port " + port, null);
			Log.info("Maximum clients: " + maxClients, null);

			final GameDataFactory dataFactory =
				new GameDataFactory(baseDir, true, true);
			final Multiplexer multiplexer = new Multiplexer(
				name, port, backlog, maxClients, dataFactory);

			final Thread mplexerThread = new Thread(multiplexer);
			final Thread commandThread = new Thread(new CommandThread(commandPipe));

			mplexerThread
				.setUncaughtExceptionHandler(Server::globalExceptionHandler);
			commandThread
				.setUncaughtExceptionHandler(Server::globalExceptionHandler);

			mplexerThread.start();
			commandThread.start();

			boolean again = true;
			while (again) {
				try {
					again = false;
					mplexerThread.join();
					Log.info("Multiplexer thread terminated", null);
				} catch (final InterruptedException e) {
					Log.warn("Main thread interrupted", null);
					again = true;
				}
			}

			Log.info("Server finished, shutting down now", null);

		} catch (final IOException e) {
			Log.fatal("Error initialising server", e);
			System.exit(2);

		} catch (final CorruptDataException e) {
			Log.fatal("Bad game data when initialising server", e);
			System.exit(2);

		} catch (final NumberFormatException e) {
			System.out.println("Invalid command line option, expected number");
			System.out.println(e.getMessage());
			commandLineError();
		}
	}

	private static void commandLineError() {
		System.out.println("Syntax: server (opt=value)*");
		System.out.println("Valid options are: --basedir, --port, --name, --backlog, --maxClients, --command-pipe");
		System.exit(2);
	}
}

