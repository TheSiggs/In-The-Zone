package inthezone.server;

import inthezone.battle.data.GameDataFactory;
import isogame.engine.CorruptDataException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Server {
	public static final int DEFAULT_BACKLOG = 10;
	public static final int DEFAULT_PORT = 80;

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

		final Optional<File> baseDir = 
			Optional.ofNullable(parsedArgs.get("basedir")).map(x -> new File(x));

		try {
			final int port = Optional.ofNullable(parsedArgs.get("port"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_PORT);
			final int backlog = Optional.ofNullable(parsedArgs.get("backlog"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_BACKLOG);

			GameDataFactory dataFactory = new GameDataFactory(baseDir);
			Multiplexer multiplexer = new Multiplexer(dataFactory);
			Connector connector = new Connector(port, backlog, multiplexer);

			Thread mplexerThread = new Thread(multiplexer);
			Thread connectorThread = new Thread(connector);
			mplexerThread.start();
			connectorThread.start();

			boolean again = true;
			while (again) {
				try {
					again = false;
					connectorThread.join();
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
		System.out.println("Valid options are: basedir, port, backlog");
		System.exit(2);
	}
}

