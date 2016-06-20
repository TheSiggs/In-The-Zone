package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.Network;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.util.Map;
import java.util.Optional;

public class Game extends Application {
	/* TODO: find a way to set these from the build system */
	private final static boolean IS_DEVEL_BUILD = true;
	private final static String DEFAULT_SERVER = "127.0.0.1"; // TODO: update with the ip of the real sever
	private final static int DEFAULT_PORT = 8000; // TODO: update this with the real default port

	private final ClientConfig config = new ClientConfig();

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}

	private Network network = null;

	@Override
	public void start(Stage primaryStage) {
		Map<String, String> args = getParameters().getNamed();
		System.err.println("args: " + args.toString());

		Optional<String> basedir;
		String server;
		int port;
		if (IS_DEVEL_BUILD) {
			basedir = Optional.ofNullable(args.get("basedir"));
			server = args.getOrDefault("server", "127.0.0.1");
			try {
				port = Optional.ofNullable(args.get("port"))
					.map(x -> Integer.parseInt(x)).orElse(DEFAULT_PORT);
			} catch (NumberFormatException e) {
				port = 0; // need to set this to prevent compiler error
				System.err.println("Port parameter must be a number");
				System.exit(2);
			}
		} else {
			basedir = Optional.empty();
			server = DEFAULT_SERVER;
			port = DEFAULT_PORT;
		}

		FlowPane root = new FlowPane();
		Scene scene = new Scene(root, 960, 540);

		try {
			final GameDataFactory gameData =
				new GameDataFactory(basedir.map(x -> (new File(x)).getAbsoluteFile()));
			final ContentPane contentPane = new ContentPane(
				gameData, server, port, config.getDefaultPlayerName());
			root.getChildren().add(contentPane);

			this.network = contentPane.network;

			primaryStage.setTitle("In the Zone!");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error starting game client");
			a.showAndWait();
		}
	}

	@Override
	public void stop() {
		if (network != null) {
			//network.shutdown();
		}
	}
}

