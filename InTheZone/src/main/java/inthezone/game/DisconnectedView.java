package inthezone.game;

import inthezone.ai.SimpleAI;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.comptroller.Network;
import inthezone.game.battle.BattleView;
import inthezone.game.loadoutEditor.LoadoutOverview;
import inthezone.game.lobby.ChallengePane;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DisconnectedView extends FlowPane {
	private final Network network;

	private final Button login = new Button("Connect to server");
	private final Button setServer = new Button("Set server");
	private final Button loadout = new Button("Edit loadouts offline");
	private final Button sandpit = new Button("Sandpit mode");

	private final GameDataFactory gameData;
	private final ContentPane parent;

	private String server;
	private int port;

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

		sandpit.setOnAction(event -> {
			if (config.loadouts.size() < 1) {
				final Alert a = new Alert(
					Alert.AlertType.INFORMATION, null, ButtonType.OK);
				a.setHeaderText(
					"You must create at least one loadout before starting a game");
				a.showAndWait();
				return;
			}

			try {
				parent.showScreen(
					new ChallengePane(gameData, config, Optional.empty(),
						Player.PLAYER_A, "You", "AI"), getStartSandpitCont());
			} catch (CorruptDataException e) {
				final Alert a = new Alert(Alert.AlertType.ERROR,
					e.getMessage(), ButtonType.CLOSE);
				a.setHeaderText("Game data corrupt");
				a.showAndWait();
				System.exit(1);
			}
		});

		this.getChildren().addAll(login, setServer, loadout, sandpit);
	}

	private Consumer<Optional<StartBattleCommandRequest>> getStartSandpitCont() {
		return ostart -> {
			ostart.ifPresent(start -> {
				try {
					// prepare the AI position
					final Player op = start.getOtherPlayer();
					final Stage si = gameData.getStage(start.stage);
					Collection<MapPoint> startTiles = op == Player.PLAYER_A ?
						si.terrain.getPlayerStartTiles() :
						si.terrain.getAIStartTiles();
					final Loadout l = makeSandpitLoadout(start, startTiles, gameData);

					// prepare the battle
					final StartBattleCommand ready =
						(new StartBattleCommandRequest(start.stage, op, "AI", l,
							startTiles.stream().collect(Collectors.toList())))
							.makeCommand(start, gameData);

					// start the battle
					parent.showScreen(new BattleView(
						ready, Player.PLAYER_A, "AI", new SimpleAI(), Optional.empty(), gameData),
						winCond -> System.err.println("Battle over: " + winCond));
				} catch (CorruptDataException e) {
					final Alert a = new Alert(Alert.AlertType.ERROR,
						e.getMessage(), ButtonType.OK);
					a.setHeaderText("Error starting game");
					a.showAndWait();
				}
			});
		};
	}
	
	private static Loadout makeSandpitLoadout(
		final StartBattleCommandRequest start,
		final Collection<MapPoint> startTiles,
		final GameDataFactory gameData
	) throws CorruptDataException {
		final List<CharacterProfile> characters = new ArrayList<>();
		for (int i = 0; i < startTiles.size(); i++)
			characters.add(new CharacterProfile(gameData.getCharacter("Robot")));

		return new Loadout("Sandpit", characters, new ArrayList<>());
	}

	public void startConnecting() {
		// TODO: show an animation of some sort to indicate that we are connecting
	}
	
	public void endConnecting() {
	}
}

