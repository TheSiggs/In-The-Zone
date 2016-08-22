package inthezone.game;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.LobbyListener;
import inthezone.comptroller.Network;
import inthezone.game.lobby.LobbyView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.Optional;
import java.util.Stack;

public class ContentPane extends StackPane implements LobbyListener {
	private final DisconnectedView disconnected;
	private final LobbyView lobbyView;

	@SuppressWarnings("unchecked")
	private final Stack<DialogScreen> screens = new Stack();

	public final Network network;
	public final ClientConfig config;
	public final GameDataFactory gameData;

	public final Thread networkThread;

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

		this.config = config;
		this.gameData = gameData;

		network = new Network(gameData, this);
		disconnected = new DisconnectedView(
			this, gameData, config, server, port, playername);
		lobbyView = new LobbyView(this, gameData, config);
		networkThread = new Thread(network);
		networkThread.start();
		currentPane = disconnected;
		lobbyView.setVisible(false);
		this.getChildren().addAll(disconnected, lobbyView);
	}

	/**
	 * Show a dialog screen.
	 * @param screen The screen to show
	 * @param k The continuation to execute when the screen finishes
	 * */
	public <T> void showScreen(DialogScreen<T> screen, Consumer<Optional<T>> k) {
		this.getChildren().add(screen);
		screens.push(screen);
		switchPane(screen);
		screen.doOnDone(v -> {
			this.getChildren().remove(screen);
			closeScreen();
			k.accept(v);
		});
	}

	private void closeScreen() {
		screens.pop();
		if (screens.isEmpty()) {
			if (isConnected) switchPane(lobbyView);
			else switchPane(disconnected);
		} else {
			switchPane(screens.peek());
		}
	}

	private void switchPane(Pane target) {
		currentPane.setVisible(false);
		target.setVisible(true);
		currentPane = target;
	}

	@Override
	public void connectedToServer(String playerName, Collection<String> players) {
		Platform.runLater(() -> {
			disconnected.endConnecting();
			lobbyView.joinLobby(playerName, players);
			if (screens.isEmpty()) switchPane(lobbyView);
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
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}

	@Override
	public void serverError(Exception e) {
		Platform.runLater(() -> {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Server error");
			a.showAndWait();
			disconnected.endConnecting();
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}

	@Override
	public void connectionDropped() {
	}

	@Override
	public void loggedOff() {
		Platform.runLater(() -> {
			disconnected.endConnecting();
			if (screens.isEmpty()) switchPane(disconnected);
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

