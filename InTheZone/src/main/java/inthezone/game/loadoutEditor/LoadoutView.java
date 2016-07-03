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
import javafx.scene.control.TextField;
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
	private final ListView<CharacterProfile> profiles = new ListView<>();
	private final Button done = new Button("Done");

	public LoadoutView(ClientConfig config, GameDataFactory gameData) {
		super();

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
		rightPane.getChildren().addAll(loadoutSelection, loadoutName, profiles, done);

		profiles.setCellFactory(CharacterIndicatorCell.forListView());

		ChangeListener<CharacterProfile> profileListener =
			(p, s0, s1) -> {
				int i = profiles.getSelectionModel().getSelectedIndex();
				loadout.getSelectionModel().getSelectedItem().profiles.set(i, s1);
			};

		profiles.getSelectionModel().selectedItemProperty()
			.addListener((p, s0, s1) -> {
				try {
					cp.profileProperty().removeListener(profileListener);
				} catch (NullPointerException e) {
					/* Doesn't matter */
				}
				if (s1 != null) {
					cp.setCharacterProfile(s1);
					cp.profileProperty().addListener(profileListener);
				}
			});

		profiles.getSelectionModel().select(null);
		profiles.getSelectionModel().select(0);

		done.setOnAction(event -> {
			config.loadouts.clear();
			for (LoadoutModel m : loadoutsModel)
				config.loadouts.add(m.encodeLoadout());
			config.writeConfig();
			onDone.accept(null);
		});

		content.getChildren().addAll(cp, rightPane);
		this.getChildren().add(content);
	}

	private static LoadoutModel emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		try {
			List<CharacterProfile> profiles = new ArrayList<>();
			for (CharacterInfo c : gameData.getCharacters())
				profiles.add(new CharacterProfile(c));
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

