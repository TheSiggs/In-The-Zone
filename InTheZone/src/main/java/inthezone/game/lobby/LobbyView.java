package inthezone.game.lobby;

import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.Network;
import inthezone.game.ClientConfig;
import inthezone.game.ContentPane;
import inthezone.game.loadoutEditor.LoadoutOverview;
import isogame.engine.CorruptDataException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LobbyView extends VBox {
	private final ObservableList<ServerPlayer> players =
		FXCollections.observableArrayList();
	final ListView<ServerPlayer> playerList = new ListView<>(players);
	
	private final Map<String, ServerPlayer>	playerNames = new HashMap<>();

	private final ContentPane parent;
	private final GameDataFactory gameData;
	private final ClientConfig config;

	public LobbyView(
		ContentPane parent,
		GameDataFactory gameData,
		ClientConfig config
	) {
		super();

		this.parent = parent;
		this.gameData = gameData;
		this.config = config;

		final FlowPane toolbar = new FlowPane();
		final Button logout = new Button("Logout");
		final Button loadouts = new Button("Edit loadouts");
		final Button challenge = new Button("Challenge");
		toolbar.getChildren().addAll(challenge, loadouts, logout);

		final VBox mainPane = new VBox();

		mainPane.getChildren().addAll(new Label("Players on server"), playerList);

		challenge.disableProperty().bind(
			playerList.getSelectionModel().selectedItemProperty().isNull());

		this.getChildren().addAll(toolbar, mainPane);

		logout.setOnAction(event -> {
			parent.network.logout();
		});

		loadouts.setOnAction(event -> {
			parent.showScreen(new LoadoutOverview(parent), v -> {});
		});

		challenge.setOnAction(event -> {
			if (config.loadouts.isEmpty()) {
				final Alert a = new Alert(
					Alert.AlertType.INFORMATION, null, ButtonType.OK);
				a.setHeaderText(
					"You must create at least one loadout before issuing a challenge");
				a.showAndWait();
				return;
			}

			final ServerPlayer s =
				playerList.getSelectionModel().getSelectedItem();

			if (s != null && s.isInGame()) {
				final Alert a = new Alert(
					Alert.AlertType.INFORMATION, null, ButtonType.OK);
				a.setHeaderText(s.name + " is in a game");
				a.showAndWait();
				
			} else if (s != null) {
				try {
					parent.showScreen(
						new ChallengePane(gameData, config,
							Optional.empty(), Player.randomPlayer(), s.name), oCmdReq ->
								oCmdReq.ifPresent(cmdReq -> {
									parent.network.challengePlayer(cmdReq, s.name);
								}));
				} catch (CorruptDataException e) {
					final Alert a = new Alert(Alert.AlertType.ERROR,
						e.getMessage(), ButtonType.CLOSE);
					a.setHeaderText("Error initialising challenge panel");
					a.showAndWait();
				}
			}
		});
	}

	private String playerName = null;

	public void joinLobby(String playerName, Collection<String> players) {
		this.playerName = playerName;

		this.players.clear();

		players.stream()
			.filter(x -> !x.equals(playerName))
			.map(x -> new ServerPlayer(x, false))
			.forEach(x -> {
				this.players.add(x);
				this.playerNames.put(x.name, x);
			});
	}

	public void playerJoins(String player) {
		if (player == playerName) return;

		ServerPlayer p = playerNames.get(player);
		if (p != null) {
			p.reset();
			playerList.refresh();
		} else {
			p = new ServerPlayer(player, false);
			playerNames.put(player, p);
			players.add(p);
		}
	}

	public void playerLeaves(String player) {
		ServerPlayer p = playerNames.remove(player);
		if (p != null) players.remove(p);
	}

	public void playerEntersGame(String player) {
		ServerPlayer p = playerNames.get(player);
		if (p != null) p.setInGame();
		playerList.refresh();
	}

	public void challengeFrom(String player, StartBattleCommandRequest otherCmd) {
		if (config.loadouts.isEmpty()) {
			// Automatically refuse the challenge
			parent.network.refuseChallenge(player);
		}

		final Alert a = new Alert(
			Alert.AlertType.CONFIRMATION, "Accept this challenge?",
			ButtonType.YES, ButtonType.NO);
		a.setHeaderText(player + " challenges you to battle!");
		a.showAndWait().ifPresent(r -> {
			if (r == ButtonType.YES) {
				try {
					parent.showScreen(
						new ChallengePane(gameData, config,
							Optional.of(otherCmd.stage), otherCmd.player.otherPlayer(), player),
							oCmdReq -> {
								if (!oCmdReq.isPresent()) {
									parent.network.refuseChallenge(player);
								} else {
									try {
										final StartBattleCommandRequest cmdReq = oCmdReq.get();
										final StartBattleCommand ready =
											cmdReq.makeCommand(otherCmd, gameData);
										parent.network.acceptChallenge(
											ready, otherCmd.player.otherPlayer(), player);
									} catch (CorruptDataException e) {
										parent.network.refuseChallenge(player);
										final Alert ae = new Alert(
											Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
										ae.setHeaderText("Error initialising battle");
										ae.showAndWait();
									}
								}
							});
				} catch (CorruptDataException e) {
					parent.network.refuseChallenge(player);
					final Alert ae = new Alert(
						Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
					ae.setHeaderText("Error initialising challenge panel");
					ae.showAndWait();
				}
			} else {
				parent.network.refuseChallenge(player);
			}
		});
	}
}

class ServerPlayer {
	public final String name;

	private boolean inGame;

	public ServerPlayer(String name, boolean inGame) {
		this.name = name;
		this.inGame = inGame;
	}

	public void setInGame() { inGame = true; }

	public boolean isInGame() { return inGame; }

	public void reset() {
		inGame = false;
	}

	@Override
	public String toString() {
		return name + (inGame? " (unavailable)" : "");
	}
}

