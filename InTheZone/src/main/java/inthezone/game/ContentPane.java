package inthezone.game;

import inthezone.battle.BattleOutcome;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.LobbyListener;
import inthezone.comptroller.Network;
import inthezone.comptroller.NetworkCommandGenerator;
import inthezone.game.battle.BattleView;
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
			isConnected = true;
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
			isConnected = false;
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
			e.printStackTrace();
			isConnected = false;
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Server error");
			a.showAndWait();
			disconnected.endConnecting();
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}


	@Override
	public void serverNotification(String e) {
		Platform.runLater(() -> {
			Alert a = new Alert(Alert.AlertType.ERROR, e, ButtonType.CLOSE);
			a.setHeaderText("Message from the server");
			a.showAndWait();
		});
	}

	@Override
	public void connectionDropped() {
		Platform.runLater(() -> {
			isConnected = false;
		});
	}

	@Override
	public void loggedOff() {
		Platform.runLater(() -> {
			isConnected = false;
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
	public void challengeIssued(String player) {
	}

	@Override
	public void challengeFrom(String player, StartBattleCommandRequest cmd) {
		Platform.runLater(() -> {
			lobbyView.challengeFrom(player, cmd);
		});
	}

	@Override
	public void startBattle(
		StartBattleCommand ready, Player player, String playerName
	) {
		Platform.runLater(() ->
			showScreen(new BattleView(
				ready, player,
				new NetworkCommandGenerator(network.readCommandQueue),
				network, gameData), (Optional<BattleOutcome> oWinCond) -> {
					String message = null;
					if (oWinCond.isPresent()) {
						switch (oWinCond.get()) {
							case WIN: message = "You win!"; break;
							case LOSE: message = "You lose."; break;
							case DRAW: message = "It's a draw"; break;
						}
					}

					Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.CLOSE);
					a.setHeaderText("Battle over");
					a.showAndWait();
				}));
	}
}

