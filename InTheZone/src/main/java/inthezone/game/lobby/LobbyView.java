package inthezone.game.lobby;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.Network;
import inthezone.game.ClientConfig;
import inthezone.game.ContentPane;
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

	private final Map<String, ServerPlayer>	playerNames = new HashMap<>();

	public LobbyView(
		ContentPane parent,
		GameDataFactory gameData,
		ClientConfig config
	) {
		super();

		final FlowPane toolbar = new FlowPane();
		final Button logout = new Button("Logout");
		final Button challenge = new Button("Challenge");
		toolbar.getChildren().addAll(challenge, logout);

		final VBox mainPane = new VBox();

		final ListView<ServerPlayer> playerList = new ListView<>(players);
		mainPane.getChildren().addAll(new Label("Players on server"), playerList);

		challenge.disableProperty().bind(
			playerList.getSelectionModel().selectedItemProperty().isNull());

		this.getChildren().addAll(toolbar, mainPane);

		logout.setOnAction(event -> {
			parent.network.logout();
		});

		challenge.setOnAction(event -> {
			ServerPlayer s = playerList.getSelectionModel().getSelectedItem();
			if (s != null) {
				try {
					parent.showScreen(
						new ChallengePane(gameData, config,
							Optional.empty(), Player.randomPlayer()), oCmdReq ->
								oCmdReq.ifPresent(cmdReq -> {
									parent.network.challengePlayer(cmdReq, s.name);
								}));
				} catch (CorruptDataException e) {
					Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
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
			.forEach(x -> this.players.add(x));
	}

	public void playerJoins(String player) {
		if (player == playerName) return;

		ServerPlayer p = playerNames.get(player);
		if (p != null) {
			p.reset();
		} else {
			p = new ServerPlayer(player, false);
			playerNames.put(player, p);
			players.add(p);
		}
	}

	public void playerLeaves(String player) {
		ServerPlayer p = playerNames.get(player);
		if (p != null) players.remove(p);
	}

	public void playerEntersGame(String player) {
		ServerPlayer p = playerNames.get(player);
		if (p != null) p.setInGame();
	}
}

class ServerPlayer {
	public final String name;

	private boolean inGame;

	public ServerPlayer(String name, boolean inGame) {
		this.name = name;
		this.inGame = inGame;
	}

	public void setInGame() {
		inGame = true;
	}

	public void reset() {
		inGame = false;
	}

	@Override
	public String toString() {
		return name + (inGame? " (unavailable)" : "");
	}
}

