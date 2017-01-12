package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.ArrayList;
import java.util.List;

public class LoadoutView extends DialogScreen<Void> {
	private final ObservableList<LoadoutModel> loadoutsModel =
		FXCollections.observableArrayList();

	private final ObservableList<CharacterInfo> charactersModel =
		FXCollections.observableArrayList();
	
	private final CharacterProfilePane cp = new CharacterProfilePane();
	private final HBox content = new HBox();
	private final GridPane rightPane = new GridPane();

	private final ComboBox<LoadoutModel> loadout = new ComboBox<>(loadoutsModel);
	private final Button newLoadout = new Button("+");
	private final TextField loadoutName = new TextField("<loadout name>");
	private final ListView<CharacterProfileModel> profiles = new ListView<>();
	private final Button addCharacter = new Button("Add Character");
	private final Button removeCharacter = new Button("Remove Character");
	private final ListView<CharacterInfo> characters = new ListView<>(charactersModel);
	private final Button done = new Button("Done");
	private final Button delete = new Button("Delete this loadout");

	private final IntegerProperty totalCost = new SimpleIntegerProperty(0);
	private final BooleanProperty isLegitimate = new SimpleBooleanProperty(true);
	private final Label costLabel = new Label("Cost: ");

	private final Paint goodCostColor = Color.GREEN;
	private final Paint badCostColor = Color.RED;

	private final ClientConfig config;

	private void rebindTotalCost(LoadoutModel l) {
		if (l != null) totalCost.bind(l.profiles.stream()
			.map(pr -> (NumberExpression) pr.costProperty())
			.reduce(new SimpleIntegerProperty(0), (x, y) -> x.add(y)));
	}

	public LoadoutView(ClientConfig config, GameDataFactory gameData) {
		this.config = config;

		loadout.setCellFactory(LoadoutCell.forListView());
		loadout.setButtonCell(new LoadoutCell());

		for (Loadout l : config.loadouts) loadoutsModel.add(new LoadoutModel(l));

		if (loadoutsModel.isEmpty())
			loadoutsModel.add(emptyLoadout(config, gameData));

		for (CharacterInfo c : gameData.getCharacters()) {
			if (c.playable) charactersModel.add(c);
		}

		costLabel.setTextFill(goodCostColor);
		isLegitimate.addListener((o, v0, v1) -> {
			if (v1) costLabel.setTextFill(goodCostColor);
			else costLabel.setTextFill(badCostColor);
		});

		loadout.getSelectionModel().selectedItemProperty()
			.addListener((p, s0, s1) -> {
				if (s0 != null) {
					s0.name.unbind();
				}

				if (s1 != null) {
					loadoutName.setText(s1.name.getValue());
					s1.name.bind(loadoutName.textProperty());
					profiles.setItems(s1.profiles);
					profiles.getSelectionModel().select(0);

					rebindTotalCost(s1);

					isLegitimate.setValue(s1.encodeLoadout().isLegitimate());
				}
			});

		loadout.getSelectionModel().select(0);

		newLoadout.setOnAction(event -> {
			loadoutsModel.add(emptyLoadout(config, gameData));
			loadout.getSelectionModel().select(loadoutsModel.size() - 1);
		});

		HBox loadoutSelection = new HBox();
		loadoutSelection.getChildren()
			.addAll(new Label("Loadout"), loadout, newLoadout);
		FlowPane toolbar = new FlowPane();
		toolbar.getChildren().addAll(done, delete);

		rightPane.add(loadoutSelection, 0, 0, 3, 1);
		rightPane.add(new Label("Name: "), 0, 1);
		rightPane.add(loadoutName, 1, 1);
		rightPane.add(costLabel, 2, 1);
		rightPane.add(profiles, 0, 2, 3, 1);
		rightPane.add(addCharacter, 0, 3, 1, 1);
		rightPane.add(removeCharacter, 1, 3, 1, 1);
		rightPane.add(characters, 0, 4, 3, 1);
		rightPane.add(toolbar, 0, 5, 3, 1);

		costLabel.textProperty().bind(
			(new SimpleStringProperty(" Total cost: "))
				.concat(totalCost).concat(" / " + Loadout.maxPP));

		totalCost.addListener(o -> {
			LoadoutModel m = loadout.getSelectionModel().getSelectedItem();
			if (m != null) isLegitimate.setValue(m.encodeLoadout().isLegitimate());
		});

		characters.setPlaceholder(new Label("All characters chosen"));
		profiles.setPlaceholder(new Label("You must add at least one character"));

		profiles.setCellFactory(CharacterIndicatorCell.forListView());

		profiles.getSelectionModel().selectedItemProperty()
			.addListener((p, s0, s1) -> {
				cp.setCharacterProfile(s1);
			});

		profiles.getSelectionModel().select(null);
		profiles.getSelectionModel().select(0);

		addCharacter.disableProperty().bind(characters.getSelectionModel()
			.selectedItemProperty().isNull());
		removeCharacter.disableProperty().bind(profiles.getSelectionModel()
			.selectedItemProperty().isNull());

		characters.setOnMouseClicked(event -> {
			if (event.getClickCount() > 1) addCharacter();
		});
		addCharacter.setOnAction(event -> addCharacter());
		removeCharacter.setOnAction(event -> removeCharacter());

		// The delete button
		delete.setOnAction(event -> {
			LoadoutModel s = loadout.getSelectionModel().getSelectedItem();
			if (s == null) return;

			Alert a = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.NO, ButtonType.YES);
			a.setHeaderText("Really delete " + s.name.getValue() + "?");
			if (a.showAndWait().map(b -> b.equals(ButtonType.NO)).orElse(true)) {
				return;
			}

			if (loadoutsModel.size() == 1){
				loadoutsModel.add(emptyLoadout(config, gameData));
			}
			SingleSelectionModel sel = loadout.getSelectionModel();
			if (sel.getSelectedIndex() == 0) sel.selectNext(); else sel.selectPrevious();
			loadoutsModel.remove(s);
			saveLoadouts();
		});

		// The done button
		done.setOnAction(event -> {
			saveLoadouts();
			onDone.accept(null);
		});

		content.getChildren().addAll(cp, rightPane);
		this.getChildren().add(content);
	}

	private void addCharacter() {
		CharacterInfo c = characters.getSelectionModel().getSelectedItem();
		if (c == null) return;
		try {
			if (!profiles.getItems().stream().anyMatch(p -> p.rootCharacter.name.equals(c.name))) {
				profiles.getItems().add(new CharacterProfileModel(new CharacterProfile(c)));
				rebindTotalCost(loadout.getSelectionModel().getSelectedItem());
			}

		} catch (CorruptDataException e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Error in game data");
			a.showAndWait();
			config.writeConfig();

			System.exit(1);
		}
	}

	private void removeCharacter() {
		CharacterProfileModel profile = profiles.getSelectionModel().getSelectedItem();
		if (profile == null) return;

		profiles.getItems().remove(profile);
		rebindTotalCost(loadout.getSelectionModel().getSelectedItem());
	}

	private void saveLoadouts() {
		config.loadouts.clear();
		for (LoadoutModel m : loadoutsModel)
			config.loadouts.add(m.encodeLoadout());
		config.writeConfig();
	}

	private static LoadoutModel emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		List<CharacterProfile> profiles = new ArrayList<>();
		return new LoadoutModel(new Loadout("<new loadout>", profiles));
	}
}

