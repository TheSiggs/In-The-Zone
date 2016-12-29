package inthezone.server;

import inthezone.battle.data.GameDataFactory;
import isogame.engine.CorruptDataException;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Server extends Application {
	public static final int DEFAULT_BACKLOG = 10;
	public static final int DEFAULT_PORT = 8000; // for now!

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}

	@Override
	public void start(Stage primaryStage) {
		Map<String, String> parsedArgs = getParameters().getNamed();
		System.err.println(parsedArgs.toString());

		final Optional<File> baseDir = 
			Optional.ofNullable(parsedArgs.get("basedir"))
			.map(x -> (new File(x)).getAbsoluteFile());

		try {
			final int port = Optional.ofNullable(parsedArgs.get("port"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_PORT);
			final int backlog = Optional.ofNullable(parsedArgs.get("backlog"))
				.map(x -> Integer.parseInt(x)).orElse(DEFAULT_BACKLOG);

			GameDataFactory dataFactory = new GameDataFactory(baseDir, true);
			Multiplexer multiplexer = new Multiplexer(port, backlog, dataFactory);

			Thread mplexerThread = new Thread(multiplexer);
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
		System.out.println("Valid options are: --basedir, --port, --backlog");
		System.exit(2);
	}
}

