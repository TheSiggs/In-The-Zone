package inthezone.game.lobby;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PlayersList extends VBox {
	private final TextField searchBox = new TextField();
	private final ListView<String> players = new ListView<>();
	private final ObservableList<String> allPlayers =
		FXCollections.observableArrayList();
	private final ObservableList<String> playersModel =
		FXCollections.observableArrayList();
	private final Set<String> visiblePlayers = new HashSet<>();
	
	private final Button challenge = new Button("Challenge");
	
	public PlayersList(final Consumer<String> onChallenge) {
		setAlignment(Pos.CENTER);
		players.setItems(playersModel);
		this.getChildren().addAll(
			new Label("Players"), searchBox, players, challenge);

		searchBox.setPromptText("Search player");
		searchBox.textProperty().addListener((o, v0, v1) -> doSearch(v1));

		challenge.disableProperty().bind(
			players.getSelectionModel().selectedItemProperty().isNull());

		challenge.setOnAction(event -> {
			final String otherPlayer = players.getSelectionModel().getSelectedItem();
			if (otherPlayer != null) onChallenge.accept(otherPlayer);
		});

	}

	public void doSearch(final String pattern0) {
		final String pattern = normalize(pattern0);

		for (final String s : allPlayers) {
			if (normalize(s).startsWith(pattern)) {
				if (!visiblePlayers.contains(s)) {
					playersModel.add(s);
					playersModel.sort(Comparator.naturalOrder());
				}
				visiblePlayers.add(s);
			} else {
				if (visiblePlayers.contains(s)) playersModel.remove(s);
				visiblePlayers.remove(s);
			}
		}
	}

	private static String normalize(final String string) {
		return Normalizer
			.normalize(string.toLowerCase().trim(), Normalizer.Form.NFKD)
			.replaceAll("\\p{M}", "");
	}

	public void addPlayer(final String playername) {
		allPlayers.add(playername);
		doSearch(searchBox.getText());
	}

	public void removePlayer(final String playername) {
		allPlayers.remove(playername);
		doSearch(searchBox.getText());
	}
}

