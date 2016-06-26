package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import isogame.engine.CorruptDataException;
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

public class LoadoutView extends HBox {
	private final ObservableList<Loadout> loadoutsModel = FXCollections.observableArrayList();
	private final ObservableList<CharacterProfile> profilesModel = FXCollections.observableArrayList();

	private final CharacterProfilePane cp = new CharacterProfilePane();
	private final VBox rightPane = new VBox();

	private final ComboBox<Loadout> loadout = new ComboBox<>(loadoutsModel);
	private final Button newLoadout = new Button("+");
	private final TextField loadoutName = new TextField("<loadout name>");
	private final ListView<CharacterProfile> profiles = new ListView<>(profilesModel);
	private final Button done = new Button("Done");

	public LoadoutView(
		ClientConfig config, GameDataFactory gameData, Runnable onDone
	) {
		super();

		loadoutsModel.addAll(config.loadouts);
		if (loadoutsModel.isEmpty()) {
			loadoutsModel.add(emptyLoadout(config, gameData));
		}

		newLoadout.setOnAction(event -> {
			loadoutsModel.add(emptyLoadout(config, gameData));
			loadout.getSelectionModel().select(loadoutsModel.size() - 1);
		});

		HBox loadoutSelection = new HBox();
		loadoutSelection.getChildren()
			.addAll(new Label("Loadout"), loadout, newLoadout);
		rightPane.getChildren().addAll(loadoutSelection, loadoutName, profiles, done);

		done.setOnAction(event -> {
			config.loadouts.clear();
			config.loadouts.addAll(loadoutsModel);
			config.writeConfig();
			onDone.run();
		});

		this.getChildren().addAll(cp, rightPane);
	}

	private static Loadout emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		try {
			List<CharacterProfile> profiles = new ArrayList<>();
			for (CharacterInfo c : gameData.getCharacters())
				profiles.add(new CharacterProfile(c));
			return new Loadout(profiles);
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

