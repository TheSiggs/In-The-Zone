package inthezone.game;

import static javafx.stage.FileChooser.ExtensionFilter;

import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.Network;
import inthezone.game.battle.BattleView;
import inthezone.game.battle.PlaybackGenerator;
import inthezone.game.loadoutEditor.LoadoutOverview;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

/**
 * The startup splash screen.
 * */
public class DisconnectedView extends FlowPane {
	private final Network network;

	private final Button login = new Button("Connect to server");
	private final Button setServer = new Button("Set server");
	private final Button loadout = new Button("Edit loadouts offline");
	private final Button replay = new Button("Replay recorded game");

	private final GameDataFactory gameData;
	private final ContentPane parent;

	private String server;
	private int port;

	/**
	 * @param parent the parent ContentPane
	 * @param gameData the game data
	 * @param config the client configuration
	 * @param server the domain name of the server to connect to
	 * @param port the port to connect to
	 * @param cachedName the default name for this player
	 * */
	public DisconnectedView(
		final ContentPane parent,
		final GameDataFactory gameData,
		final ClientConfig config,
		final String server,
		final int port,
		final Optional<String> cachedName
	) {
		this.network = parent.network;
		this.gameData = gameData;
		this.parent = parent;

		this.server = server;
		this.port = port;

		login.setOnAction(event -> {
			String playerName = "";
			if (cachedName.isPresent()) {
				playerName = cachedName.get();
			} else {
				while (playerName.equals("")) {
					TextInputDialog ti = new TextInputDialog("<player name>");
					ti.setTitle("Enter player name");
					ti.setHeaderText("Enter player name");
					Optional<String> oPlayerName = ti.showAndWait();
					if (!oPlayerName.isPresent()) return;
					playerName = oPlayerName.orElse("");
					if (playerName.equals("")) {
						Alert a = new Alert(Alert.AlertType.INFORMATION,
							"You must enter a player name", ButtonType.OK, ButtonType.CANCEL);
						a.setHeaderText("");
						Optional<ButtonType> r = a.showAndWait();
						if (r.isPresent() && r.get() == ButtonType.CANCEL) return;
					}
				}
			}
			startConnecting();
			network.connectToServer(this.server, this.port, playerName);
		});

		setServer.setOnAction(event -> {
			final TextInputDialog ti = new TextInputDialog(this.server + ":" + this.port);
			ti.setTitle("Enter server name");
			ti.setHeaderText("Enter server domain name or IP, and port");
			ti.showAndWait().ifPresent(newServer -> {
				final String[] r = newServer.split(":");
				if (r.length == 1) {
					this.server = r[0];
					config.server = this.server;
					config.writeConfig();
				} else if (r.length == 2) {
					this.server = r[0];
					this.port = Integer.parseInt(r[1]);
					config.server = this.server;
					config.port = this.port;
					config.writeConfig();
				} else {
					final Alert a = new Alert(
						Alert.AlertType.INFORMATION, "Enter \"server:port\"");
					a.setHeaderText("Invalid server name");
					a.showAndWait();
				}
			});
		});

		loadout.setOnAction(event -> {
			parent.showScreen(new LoadoutOverview(parent), v -> {});
		});

		replay.setOnAction(event -> {
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose replay file");
			fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Game files", "*.game"),
				new ExtensionFilter("All files", "*.*"));

			final File file =
				fileChooser.showOpenDialog(this.getScene().getWindow());

			if (file != null) {
				try {
					final PlaybackGenerator playback = new PlaybackGenerator();
					final InputStream in = new FileInputStream(file);

					parent.showScreen(new BattleView(playback, in, gameData, config),
						winCond -> System.err.println("Replay complete: " + winCond));

				} catch (final IOException e) {
					final Alert a = new Alert(Alert.AlertType.ERROR,
						e.getMessage(), ButtonType.CLOSE);
					a.setHeaderText("Cannot read saved game file");
					a.showAndWait();

				} catch (final ProtocolException|CorruptDataException e) {
					final Alert a = new Alert(Alert.AlertType.ERROR,
						e.getMessage(), ButtonType.CLOSE);
					a.setHeaderText("Saved game corrupted");
					a.showAndWait();
				}
			}

		});

		this.getChildren().addAll(login, setServer, loadout, replay);
	}

	public void startConnecting() {
		// TODO: show an animation of some sort to indicate that we are connecting
	}
	
	public void endConnecting() {
	}
}

