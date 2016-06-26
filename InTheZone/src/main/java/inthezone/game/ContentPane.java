package inthezone.game;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.LobbyListener;
import inthezone.comptroller.Network;
import inthezone.game.loadoutEditor.LoadoutView;
import inthezone.game.lobby.LobbyView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.Collection;
import java.util.Optional;

public class ContentPane extends StackPane implements LobbyListener {
	private final DisconnectedView disconnected;
	private final LoadoutView loadout;
	private final LobbyView lobbyView;

	public final Network network;
	private Thread networkThread;

	private Pane currentPane;

	private boolean isConnected = false;


	public ContentPane(
		ClientConfig config,
		GameDataFactory gameData,
		String server,
		int port,
		Optional<String> playername
	) {
		super();

		network = new Network(gameData, this);
		loadout = new LoadoutView(config, gameData, this::fallbackPane);
		disconnected = new DisconnectedView(
			() -> switchPane(loadout),
			network, server, port, playername);
		lobbyView = new LobbyView(network);
		networkThread = new Thread(network);
		networkThread.start();
		currentPane = disconnected;
		loadout.setVisible(false);
		lobbyView.setVisible(false);
		this.getChildren().addAll(disconnected, loadout, lobbyView);
	}

	private void switchPane(Pane target) {
		currentPane.setVisible(false);
		target.setVisible(true);
		currentPane = target;
	}

	/**
	 * Fall back to the most appropriate pane.
	 * */
	private void fallbackPane() {
		if (!isConnected) switchPane(disconnected);
		else switchPane(lobbyView);
	}

	@Override
	public void connectedToServer(Collection<String> players) {
		Platform.runLater(() -> {
			disconnected.endConnecting();
			lobbyView.setPlayers(players);
			switchPane(lobbyView);
		});
	}

	@Override
	public Optional<String> tryDifferentPlayerName(String name) {
		return Optional.empty();
	}

	@Override
	public void errorConnectingToServer(Exception e) {
		Platform.runLater(() -> {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error connecting to server");
			a.showAndWait();
			disconnected.endConnecting();
			switchPane(disconnected);
		});
	}

	@Override
	public void serverError(Exception e) {
		Platform.runLater(() -> {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Server error");
			a.showAndWait();
			disconnected.endConnecting();
			switchPane(disconnected);
		});
	}

	@Override
	public void connectionDropped() {
	}

	@Override
	public void loggedOff() {
		Platform.runLater(() -> {
			disconnected.endConnecting();
			switchPane(disconnected);
		});
	}

	@Override
	public void playerHasLoggedIn(String player) {
		Platform.runLater(() -> {
			lobbyView.playerJoins(player);
		});
	}

	@Override
	public void playerHasLoggedOff(String player) {
		Platform.runLater(() -> {
			lobbyView.playerLeaves(player);
		});
	}

	@Override
	public void playerHasEnteredBattle(String player) {
		Platform.runLater(() -> {
			lobbyView.playerEntersGame(player);
		});
	}

	@Override
	public void playerRefusesChallenge(String player) {
	}

	@Override
	public void challengeFrom(String player, StartBattleCommandRequest cmd) {
	}

	@Override
	public void startBattle(BattleInProgress battle, String player) {
	}
}

