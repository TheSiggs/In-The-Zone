package inthezone.game;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.Network;

public class Game extends Application {
	/* TODO: find a way to set these from the build system */
	private final static boolean IS_DEVEL_BUILD = true;

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}

	private Network network = null;
	private Thread networkThread = null;
	private static final double MIN_WIDTH = 1280.0;
	private static final double MIN_HEIGHT = 720.0;

	private boolean inGlobalExceptionHandler = false;

	@Override
	public void start(final Stage primaryStage) {
		Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> {
			if (inGlobalExceptionHandler) {
				/* Double fault, hard terminate */
				System.exit(100);
			}

			inGlobalExceptionHandler = true;
			try {
				e.printStackTrace();
				final Alert a = new Alert(Alert.AlertType.ERROR,
					e.getMessage(), ButtonType.CLOSE);
				a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
				a.setHeaderText("Unexpected error");
				a.showAndWait();
			} catch (final Exception e2) {
				/* do nothing because there's nothing we can do at this point */
			} finally {
				inGlobalExceptionHandler = false;
				Platform.exit();
			}
		});
		

		final Map<String, String> args = getParameters().getNamed();
		System.err.println("args: " + args.toString());

		try {

			Optional<String> basedir;
			Optional<String> serverOverride = Optional.empty();
			Optional<Integer> portOverride = Optional.empty();

			if (IS_DEVEL_BUILD) {
				basedir = Optional.ofNullable(args.get("basedir"));
				if (!basedir.isPresent() && args.get("dev") != null) {
					basedir = Optional.ofNullable(getDataDir(primaryStage));
				}
				serverOverride = Optional.ofNullable(args.get("server"));
				try {
					portOverride = Optional.ofNullable(args.get("port"))
						.map(x -> Integer.parseInt(x));
				} catch (NumberFormatException e) {
					System.err.println("Port parameter must be a number");
					System.exit(2);
				}
			} else {
				basedir = Optional.empty();
			}

			final GameDataFactory gameData = new GameDataFactory(
				basedir.map(x -> (new File(x)).getAbsoluteFile()), false);
			final ClientConfig config = new ClientConfig(gameData);

			final String server = serverOverride.orElse(config.server);
			final int port = portOverride.orElse(config.port);

			final ContentPane contentPane = new ContentPane(
				config, gameData, server, port, config.defaultPlayerName);

			this.network = contentPane.network;
			this.networkThread = contentPane.networkThread;

			Scene scene = new Scene(contentPane, MIN_WIDTH, MIN_HEIGHT);
			scene.getStylesheets().add("GUI.css");

			primaryStage.setTitle("In the Zone!");
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(MIN_WIDTH);
			primaryStage.setMinHeight(MIN_HEIGHT);
			primaryStage.show();

			/* This works around a race condition in javaFX that would otherwise lead
			 * to incorrect mapping from local to screen coordinates for popup windows.
			 * */
			primaryStage.setX(primaryStage.getX() + 1);
			primaryStage.setY(primaryStage.getY() + 1);
			primaryStage.setX(primaryStage.getX() - 1);
			primaryStage.setY(primaryStage.getY() - 1);

		} catch (Exception e) {
			final Alert a = new Alert(Alert.AlertType.ERROR,
				e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error starting game client");
			a.showAndWait();
		}
	}

	public String getDataDir(final Stage primaryStage) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select data directory...");
		File dataDir = directoryChooser.showDialog(primaryStage);
		return dataDir.toString();
	}

	@Override
	public void stop() {
		if (network != null) network.shutdown();

		System.exit(0);
	}
}

