package inthezone.game;

import inthezone.battle.BattleOutcome;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.LobbyListener;
import inthezone.comptroller.Network;
import inthezone.comptroller.NetworkCommandGenerator;
import inthezone.game.battle.BattleView;
import inthezone.game.lobby.LobbyView;
import isogame.engine.CorruptDataException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

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

	private Optional<BattleView> currentBattle = Optional.empty();

	// remember the last challenge until such a time as we can present it to the
	// player.
	private Optional<String> challengePlayer = Optional.empty();
	private Optional<StartBattleCommandRequest> challenge = Optional.empty();

	public ContentPane(
		final ClientConfig config,
		final GameDataFactory gameData,
		final String server,
		final int port,
		final Optional<String> playername
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
	public <T> void showScreen(
		final DialogScreen<T> screen, final Consumer<Optional<T>> k
	) {
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

		if (screens.isEmpty()) {
			challengePlayer.ifPresent(player ->
				challenge.ifPresent(cmd -> {
					challengePlayer = Optional.empty();
					challenge = Optional.empty();
					lobbyView.challengeFrom(player, this.fromQueue, cmd);
				}));
		}
	}

	private void switchPane(final Pane target) {
		currentPane.setVisible(false);
		target.setVisible(true);
		currentPane = target;
	}

	@Override
	public void connectedToServer(
		final String playerName, final Collection<String> players
	) {
		Platform.runLater(() -> {
			isConnected = true;
			disconnected.endConnecting();
			lobbyView.joinLobby(playerName, players);
			if (screens.isEmpty()) switchPane(lobbyView);
		});
	}

	@Override
	public void errorConnectingToServer(final Exception e) {
		Platform.runLater(() -> {
			isConnected = false;
			final Alert a = new Alert(Alert.AlertType.ERROR,
				e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error connecting to server");
			a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			a.showAndWait();
			disconnected.endConnecting();
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}

	@Override
	public void serverError(final Exception e) {
		Platform.runLater(() -> {
			e.printStackTrace();
			isConnected = false;
			final Alert a = new Alert(Alert.AlertType.ERROR,
				e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Server error");
			a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			a.showAndWait();
			currentBattle.ifPresent(b -> b.handleEndBattle(Optional.empty()));
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}


	@Override
	public void serverNotification(final String e) {
		Platform.runLater(() -> {
			final Alert a = new Alert(Alert.AlertType.ERROR, e, ButtonType.CLOSE);
			a.setHeaderText("Message from the server");
			a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			a.showAndWait();
		});
	}

	@Override
	public void connectionDropped() {
		Platform.runLater(() -> {
			isConnected = false;
			final Alert a = new Alert(Alert.AlertType.ERROR,
				"Lost connection to the server", ButtonType.CLOSE);
			a.setHeaderText("Server disconnected");
			a.showAndWait();
			currentBattle.ifPresent(b -> b.handleEndBattle(Optional.empty()));
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}

	@Override
	public void loggedOff() {
		Platform.runLater(() -> {
			isConnected = false;
			disconnected.endConnecting();
			currentBattle.ifPresent(b -> b.handleEndBattle(Optional.empty()));
			if (screens.isEmpty()) switchPane(disconnected);
		});
	}

	@Override
	public void playerHasLoggedIn(final String player) {
		Platform.runLater(() -> {
			lobbyView.playerJoins(player);
		});
	}

	@Override
	public void playerHasLoggedOff(final String player) {
		Platform.runLater(() -> {
			lobbyView.playerLeaves(player);
		});
	}

	@Override
	public void playerHasEnteredBattle(final String player) {
		Platform.runLater(() -> {
			lobbyView.playerEntersGame(player);
		});
	}

	@Override
	public void playerRefusesChallenge(
		final String player, final boolean notReady
	) {
		Platform.runLater(() -> {
			lobbyView.challengeRejected(player, notReady);
		});
	}

	@Override
	public void challengeIssued(final String player) {
		Platform.runLater(() -> {
			lobbyView.issuedChallenge(player);
		});
	}

	private boolean fromQueue = false;

	@Override
	public void challengeFrom(
		final String player,
		final boolean fromQueue,
		final StartBattleCommandRequest cmd
	) {
		Platform.runLater(() -> {
			if (screens.isEmpty()) {
				lobbyView.challengeFrom(player, fromQueue, cmd);
			} else {
				challengePlayer = Optional.of(player);
				challenge = Optional.of(cmd);
				this.fromQueue = fromQueue;
			}
		});
	}

	@Override
	public void otherClientDisconnects(final boolean logoff) {
		Platform.runLater(() -> {
			currentBattle.ifPresent(bv -> {
				if (logoff) {
					bv.endBattle(BattleOutcome.OTHER_LOGGED_OUT);
				} else {
					bv.waitForOtherClientToReconnect();
				}
			});
		});
	}

	@Override
	public void otherClientReconnects() {
		Platform.runLater(() -> {
			currentBattle.ifPresent(bv -> {
				bv.otherClientReconnects();
			});
		});
	}

	private boolean cancelQueue = true;
	private final Set<String> cancelledChallenges = new HashSet<>();

	@Override public void challengeCancelled(final String player) {
		Platform.runLater(() -> {
			lobbyView.cancellationFrom(player);
		});
	}

	@Override public void queueCancelled() {
		Platform.runLater(() -> {
			lobbyView.queueCancellation();
		});
	}

	public void enterQueue(final List<String> vetoMaps) {
		network.enterQueue(vetoMaps);
		cancelQueue = false;
	}

	public void cancelQueue() {
		network.cancelChallenge();
		cancelQueue = true;
	}

	/**
	 * Cancel an outstanding challenge.
	 * */
	public void cancelChallenge(final String player) {
		cancelledChallenges.add(player);
		network.cancelChallenge();
	}

	@Override
	public void startBattle(
		final StartBattleCommand ready,
		final Player player,
		final String otherPlayer,
		final boolean isFromQueue
	) {
		Platform.runLater(() -> {
			try {
				if (cancelledChallenges.contains(otherPlayer)) {
					cancelledChallenges.remove(otherPlayer);
					// last line of defence in case of race conditions
					network.sendCommand(new ResignCommand(player));
					return;
				}

				if (isFromQueue && cancelQueue) {
					// last line of defence in case of race conditions
					network.sendCommand(new ResignCommand(player));
					return;
				}

				final BattleView newBattle = new BattleView(ready, player,
					new NetworkCommandGenerator(network.readCommandQueue),
					Optional.of(network), gameData, config);
				currentBattle = Optional.of(newBattle);
				lobbyView.challengeAccepted(otherPlayer);
				showScreen(newBattle, oWinCond -> {
					currentBattle = Optional.empty();
				});

			} catch (final CorruptDataException e) {
				lobbyView.challengeError(otherPlayer);
				network.sendCommand(new ResignCommand(player, true));
				final Alert a = new Alert(Alert.AlertType.ERROR,
					e.getMessage(), ButtonType.CLOSE);
				a.setHeaderText("Error starting battle");
				a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
				a.showAndWait();
			}
		});
	}
}

