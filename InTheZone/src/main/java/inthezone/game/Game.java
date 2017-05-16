package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.Network;
import inthezone.server.Server;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Map;
import java.util.Optional;

public class Game extends Application {
	/* TODO: find a way to set these from the build system */
	private final static boolean IS_DEVEL_BUILD = true;

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}

	private Network network = null;
	private Thread networkThread = null;
	private static final double MIN_WIDTH = 300.0;
	private static final double MIN_HEIGHT = 200.0;

	@Override
	public void start(Stage primaryStage) {
		Map<String, String> args = getParameters().getNamed();
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

			final GameDataFactory gameData =
				new GameDataFactory(basedir.map(x -> (new File(x)).getAbsoluteFile()), false);
			final ClientConfig config = new ClientConfig(gameData);

			final String server = serverOverride.orElse(config.server);
			final int port = portOverride.orElse(config.port);

			final ContentPane contentPane = new ContentPane(
				config, gameData, server, port, config.defaultPlayerName);

			this.network = contentPane.network;
			this.networkThread = contentPane.networkThread;

			Scene scene = new Scene(contentPane, 960, 540);
			scene.getStylesheets().add("HUD.css");

			primaryStage.setTitle("In the Zone!");
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(MIN_WIDTH);
			primaryStage.setMinHeight(MIN_HEIGHT);
			primaryStage.show();
		} catch (Exception e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error starting game client");
			a.showAndWait();
		}
	}

	public String getDataDir(Stage primaryStage) {
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

