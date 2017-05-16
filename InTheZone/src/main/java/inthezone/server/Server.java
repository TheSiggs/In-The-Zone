package inthezone.server;

import inthezone.battle.data.GameDataFactory;
import isogame.engine.CorruptDataException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Server {
	public static final int DEFAULT_BACKLOG = 10;
	public static final int DEFAULT_MAXCLIENTS = 1000;
	public static final int DEFAULT_PORT = 8000; // for now!

	public static void main(String args[]) {
		Map<String, String> parsedArgs = new HashMap<>();
		for (String arg : args) {
			String[] p = arg.split("=");
			if (p.length != 2) {
				System.out.println("Invalid command line option " + arg);
				commandLineError();
			}
			parsedArgs.put(p[0], p[1]);
		}

		System.err.println(parsedArgs.toString());

		final Optional<File> baseDir = 
			Optional.ofNullable(parsedArgs.get("--basedir"))
			.map(x -> (new File(x)).getAbsoluteFile());

		try {
			final int port = Optional.ofNullable(parsedArgs.get("--port"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_PORT);
			final int backlog = Optional.ofNullable(parsedArgs.get("--backlog"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_BACKLOG);
			final String name = Optional.ofNullable(parsedArgs.get("--name"))
				.orElse(InetAddress.getLocalHost().getHostName());
			final int maxClients = Optional.ofNullable(parsedArgs.get("--maxClients"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_MAXCLIENTS);

			System.err.println("Starting server \"" + name + "\" on port " + port);
			System.err.println("Maximum clients: " + maxClients);

			final GameDataFactory dataFactory = new GameDataFactory(baseDir, true, true);
			final Multiplexer multiplexer = new Multiplexer(
				name, port, backlog, maxClients, dataFactory);

			final Thread mplexerThread = new Thread(multiplexer);
			mplexerThread.start();

			boolean again = true;
			while (again) {
				try {
					again = false;
					mplexerThread.join();
				} catch (InterruptedException e) {
					again = true;
				}
			}

		} catch (IOException e) {
			System.err.println("Error initialising server");
			e.printStackTrace();
			System.exit(2);
		} catch (CorruptDataException e) {
			System.err.println("Bad game data when initialising server");
			e.printStackTrace();
			System.exit(2);
		} catch (NumberFormatException e) {
			System.err.println("Invalid command line option, expected number");
			System.err.println(e.getMessage());
			commandLineError();
		}
	}

	private static void commandLineError() {
		System.out.println("Syntax: server (opt=value)*");
		System.out.println("Valid options are: basedir, port, name, backlog, maxClients");
		System.exit(2);
	}
}

