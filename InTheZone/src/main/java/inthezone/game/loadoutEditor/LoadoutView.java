package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class LoadoutView extends DialogScreen<Void> {
	private final ObservableList<LoadoutModel> loadoutsModel =
		FXCollections.observableArrayList();

	private final CharacterProfilePane cp = new CharacterProfilePane();
	private final HBox content = new HBox();
	private final VBox rightPane = new VBox();

	private final ComboBox<LoadoutModel> loadout = new ComboBox<>(loadoutsModel);
	private final Button newLoadout = new Button("+");
	private final TextField loadoutName = new TextField("<loadout name>");
	private final ListView<CharacterProfileModel> profiles = new ListView<>();
	private final Button done = new Button("Done");
	private final Button delete = new Button("Delete this loadout");

	private final ClientConfig config;

	public LoadoutView(ClientConfig config, GameDataFactory gameData) {
		this.config = config;

		loadout.setCellFactory(LoadoutCell.forListView());
		loadout.setButtonCell(new LoadoutCell());

		for (Loadout l : config.loadouts) loadoutsModel.add(new LoadoutModel(l));

		if (loadoutsModel.isEmpty())
			loadoutsModel.add(emptyLoadout(config, gameData));

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
		rightPane.getChildren().addAll(loadoutSelection, loadoutName, profiles, toolbar);

		profiles.setCellFactory(CharacterIndicatorCell.forListView());

		profiles.getSelectionModel().selectedItemProperty()
			.addListener((p, s0, s1) -> {
				if (s1 != null) cp.setCharacterProfile(s1);
			});

		profiles.getSelectionModel().select(null);
		profiles.getSelectionModel().select(0);

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

	private void saveLoadouts() {
		config.loadouts.clear();
		for (LoadoutModel m : loadoutsModel)
			config.loadouts.add(m.encodeLoadout());
		config.writeConfig();
	}

	private static LoadoutModel emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		try {
			List<CharacterProfile> profiles = new ArrayList<>();
			for (CharacterInfo c : gameData.getCharacters()) {
				if (c.playable) profiles.add(new CharacterProfile(c));
			}
			return new LoadoutModel(new Loadout("<new loadout>", profiles));
		} catch (CorruptDataException e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Game data corrupt");
			a.showAndWait();
			config.writeConfig();

			System.exit(1);
			return null;
		}
	}
}

