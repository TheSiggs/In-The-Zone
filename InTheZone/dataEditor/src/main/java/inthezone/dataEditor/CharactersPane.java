package inthezone.dataEditor;

import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Stats;
import isogame.engine.CorruptDataException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CharactersPane extends VBox {
	private final HBox tools = new HBox();
	private final Button save = new Button("Save");
	private final Button add = new Button("New");
	private final Button remove = new Button("Remove");
	private final Accordion characters;

	private final BooleanProperty changed;
	private final File dataDir;
	private final GameDataFactory gameData;

	private List<File> getStages(final File baseDir) {
		final List<File> r = Arrays.stream(baseDir.listFiles())
			.filter(f -> f.getName().endsWith(".map"))
			.collect(Collectors.toList());
		r.addAll(Arrays.stream(baseDir.listFiles())
			.filter(f -> f.isDirectory())
			.flatMap(f -> getStages(f).stream())
			.collect(Collectors.toList()));
		return r;
	}

	public CharactersPane(
		final GameDataFactory gameData,
		final File dataDir,
		final BooleanProperty changed,
		final AbilitiesPane abilities
	) {
		super();

		this.gameData = gameData;
		this.dataDir = dataDir;
		this.changed = changed;

		this.tools.getChildren().addAll(save, add, remove);

		save.disableProperty().bind(changed.not());
		save.textProperty().bind(Bindings.concat("Save",
			Bindings.when(changed).then("").otherwise("d")));

		this.characters = new Accordion(
			gameData.getCharacters().stream()
				.map(c -> new CharacterPane(dataDir, c, gameData, changed, abilities))
				.collect(Collectors.toList()).toArray(new CharacterPane[0]));

		this.getChildren().addAll(tools, characters);

		save.setOnAction(event -> saveGameData());

		add.setOnAction(event -> {
			final Stats s = new Stats(3, 3, 1, 1, 1, 1);

			Image portrait;
			try {
				portrait = new Image(new FileInputStream(
					new File(dataDir, "gfx/portrait/blank.png")));
			} catch (final IOException e) {
				portrait = null;
			}

			Image bigPortrait;
			try {
				bigPortrait = new Image(new FileInputStream(
					new File(dataDir, "gfx/portrait/generic.png")));
			} catch (final IOException e) {
				bigPortrait = null;
			}

			final CharacterInfo c = new CharacterInfo(
				"New character", "", null, null,
				portrait, "portrait/blank.png",
				bigPortrait, "portrait/generic.png",
				s, new LinkedList<>(), true,
				new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
			final CharacterPane pane =
				new CharacterPane(dataDir, c, gameData, changed, abilities);
			characters.getPanes().add(pane);
			characters.setExpandedPane(pane);
			changed.setValue(true);
		});

		remove.setOnAction(event -> {
			final CharacterPane r = (CharacterPane) characters.getExpandedPane();
			if (r != null) {
				Alert m = new Alert(Alert.AlertType.CONFIRMATION, null,
					ButtonType.NO, ButtonType.YES);
				m.setHeaderText("Really delete " + r.getName() + "?");
				m.showAndWait()
					.filter(response -> response == ButtonType.YES)
					.ifPresent(response -> {
						abilities.clearAbilities();
						characters.getPanes().remove(r);
					});
			}
			changed.setValue(true);
		});
	}

	public boolean gameDataSaved() {
		return !changed.getValue();
	}

	/**
	 * Best effort save game data.
	 * */
	public void saveGameData() {
		try {
			final List<File> stages = getStages(dataDir);

			final List<CharacterInfo> cdata = new ArrayList<>();
			for (final TitledPane x : characters.getPanes()) {
				cdata.add(((CharacterPane) x).getCharacter());
			}

			gameData.writeToStream(
				new FileOutputStream(new File(dataDir, GameDataFactory.gameDataName)),
				stages, cdata
			);
			changed.setValue(false);
		} catch (final IOException e) {
			final Alert error = new Alert(Alert.AlertType.ERROR);
			error.setTitle("Error saving game data");
			error.setHeaderText(e.toString());
			error.showAndWait();
		} catch (final Exception e) {
			if (e.getCause() instanceof CorruptDataException) {  
				final Alert error = new Alert(Alert.AlertType.ERROR);
				error.setTitle("Error in data");
				error.setHeaderText(e.toString());
				error.showAndWait();
			} else {
				final Alert error = new Alert(Alert.AlertType.ERROR);
				error.setTitle("Unexpected error");
				error.setHeaderText(e.toString());
				error.showAndWait();
			}
		}
	}
}

