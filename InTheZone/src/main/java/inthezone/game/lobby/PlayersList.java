package inthezone.game.lobby;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import inthezone.game.guiComponents.RollerScrollPane;

public class PlayersList extends StackPane {
	private final VBox inner = new VBox();
	private final Label title = new Label("Players");
	private final TextField searchBox = new TextField();
	private final RollerScrollPane scrollPlayers;
	private final Label noPlayers = new Label();
	private final ListView<String> players = new ListView<>();
	private final ObservableList<String> allPlayers =
		FXCollections.observableArrayList();
	private final ObservableList<String> playersModel =
		FXCollections.observableArrayList();
	private final Set<String> visiblePlayers = new HashSet<>();
	private final Button challenge = new Button("Challenge");

	private final static String noPlayersOnServer = "No players on server";
	private final static String noPlayersFound = "No players found";
	
	public PlayersList(final Consumer<String> onChallenge) {
		this.setMaxWidth(380d);
		this.setMinWidth(380d);
		this.setPrefWidth(380d);

		setAlignment(Pos.CENTER);
		players.setItems(playersModel);

		final StackPane searchWrapper = new StackPane();
		final StackPane challengeWrapper = new StackPane();
		searchWrapper.getChildren().add(searchBox);
		challengeWrapper.getChildren().add(challenge);

		players.setPlaceholder(noPlayers);
		noPlayers.setText(noPlayersOnServer);

		this.setId("players-list");
		inner.setId("players-list-inner");
		title.getStyleClass().add("title");
		noPlayers.setId("no-players");
		searchBox.getStyleClass().add("gui-textfield");
		players.getStyleClass().add("gui-list");
		challenge.getStyleClass().addAll("gui-button", "gui-green-button");
		searchWrapper.getStyleClass().add("control-wrapper");
		challengeWrapper.getStyleClass().add("control-wrapper");

		this.scrollPlayers = new RollerScrollPane(players, false);
		this.scrollPlayers.setScrollWheelEnable(true);

		VBox.setVgrow(scrollPlayers, Priority.ALWAYS);
		scrollPlayers.setMaxHeight(Double.MAX_VALUE);

		title.setMaxWidth(Double.MAX_VALUE);

		inner.getChildren().addAll(
			title, searchWrapper, scrollPlayers, challengeWrapper);
		this.getChildren().add(inner);

		searchBox.setPromptText("Search player");
		searchBox.textProperty().addListener((o, v0, v1) -> doSearch(v1));

		challenge.disableProperty().bind(
			players.getSelectionModel().selectedItemProperty().isNull());

		challenge.setOnAction(event -> {
			final String otherPlayer = players.getSelectionModel().getSelectedItem();
			if (otherPlayer != null) onChallenge.accept(otherPlayer);
		});

		this.setOnMouseClicked(event ->
			players.getSelectionModel().clearSelection());
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

		if (allPlayers.isEmpty()) {
			noPlayers.setText(noPlayersOnServer);
		} else {
			noPlayers.setText(noPlayersFound);
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
		playersModel.remove(playername);
		doSearch(searchBox.getText());
	}
}

