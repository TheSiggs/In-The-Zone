package inthezone.game;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.LobbyListener;
import inthezone.comptroller.Network;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.Collection;
import java.util.Optional;

public class ContentPane extends StackPane implements LobbyListener {
	private final DisconnectedView disconnected;
	private final LoadoutView loadout;
	private final LobbyView lobbyView;

	private final Network network;
	private Thread networkThread;

	private Pane currentPane;

	public ContentPane(
		GameDataFactory gameData, String server, int port, Optional<String> playername
	) {
		super();

		network = new Network(gameData, this);
		disconnected = new DisconnectedView(network, server, port, playername);
		loadout = new LoadoutView();
		lobbyView = new LobbyView();
		networkThread = new Thread(network);
		networkThread.start();
		currentPane = disconnected;
		loadout.setVisible(false);
		lobbyView.setVisible(false);
		this.getChildren().addAll(disconnected, loadout, lobbyView);
	}

	@Override
	public void connectedToServer(Collection<String> players) {
		Platform.runLater(() -> {
			disconnected.endConnecting();
			currentPane.setVisible(false);
			lobbyView.setPlayers(players);
			lobbyView.setVisible(true);
		});
	}

	@Override
	public Optional<String> tryDifferentPlayerName(String name) {
		return Optional.empty();
	}

	@Override
	public void errorConnectingToServer(Exception e) {
		disconnected.endConnecting();
	}

	@Override
	public void serverError(Exception e) {
		disconnected.endConnecting();
	}

	@Override
	public void connectionDropped() {
	}

	@Override
	public void playerHasLoggedIn(String player) {
	}

	@Override
	public void playerHasLoggedOff(String player) {
	}

	@Override
	public void playerHasEnteredBattle(String player) {
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

