package inthezone.game.lobby;

import inthezone.comptroller.Network;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LobbyView extends VBox {
	private final ObservableList<Player> players =
		FXCollections.observableArrayList();

	private final ObservableList<Challenge> challenges =
		FXCollections.observableArrayList();

	private final Map<String, Player>	playerNames = new HashMap<>();

	public LobbyView(Network network) {
		final FlowPane toolbar = new FlowPane();
		final Button logout = new Button("Logout");
		final Button challenge = new Button("Challenge");
		toolbar.getChildren().add(logout);

		logout.setOnAction(event -> {
			network.logout();
		});

		challenge.setOnAction(event -> {
		});

		final VBox leftPane = new VBox();
		final VBox rightPane = new VBox(); 

		final ListView<Player> playerList = new ListView<>(players);
		final ListView<Challenge> challengeList = new ListView<>(challenges);
		leftPane.getChildren().addAll(new Label("Players on server"), playerList);
		rightPane.getChildren().addAll(new Label("Challenges"), challengeList);

		final HBox mainPane = new HBox();
		mainPane.getChildren().addAll(leftPane, rightPane);

		this.getChildren().addAll(toolbar, mainPane);
	}

	public void setPlayers(Collection<String> players) {
		this.players.clear();
		players.stream()
			.map(x -> new Player(x, false))
			.forEach(x -> this.players.add(x));
	}

	public void playerJoins(String player) {
		Player p = playerNames.get(player);
		if (p != null) {
			p.reset();
		} else {
			p = new Player(player, false);
			playerNames.put(player, p);
			players.add(p);
		}
	}

	public void playerLeaves(String player) {
		Player p = playerNames.get(player);
		if (p != null) players.remove(p);
	}

	public void playerEntersGame(String player) {
		Player p = playerNames.get(player);
		if (p != null) p.setInGame();
	}
}

class Player {
	public final String name;

	private boolean inGame;

	public Player(String name, boolean inGame) {
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

class Challenge {
	public final String name;

	public Challenge(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}

