package inthezone.game.loadoutEditor;


import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import inthezone.game.battle.ModalDialog;
import inthezone.game.guiComponents.RollerScrollPane;

/**
 * The loadout editor screen.
 * */
public class LoadoutView extends DialogScreen<Loadout> {
	private LoadoutModel model;

	private final AnchorPane root = new AnchorPane();
	private final Label characterName = new Label("");
	private final PPIndicator pp;

	private final TextField loadoutName =
		new TextField("<Loadout name (click to edit)>");
	private final Button done = new Button("Done");
	private final VBox rightPane = new VBox(4);
	private final CharacterProfilePane profilePane = new CharacterProfilePane();
	private final HBox mainPane = new HBox(10);

	private final ObjectProperty<CharacterProfileModel> selectedCharacter =
		new SimpleObjectProperty<>(null);

	private final ClientConfig config;

	private final int loadoutID;

	private final ModalDialog modalDialog = new ModalDialog();

	public LoadoutView(
		final ClientConfig config,
		final GameDataFactory gameData,
		final LoadoutModel model,
		final int loadoutID
	) {
		this.config = config;
		this.model = model;
		this.loadoutID = loadoutID;

		this.getStylesheets().add("/GUI.css");
		this.getStyleClass().add("gui-pane");

		loadoutName.getStyleClass().add("gui-textfield");
		done.getStyleClass().add("gui-button");

		done.setMaxWidth(Double.MAX_VALUE);

		// The done button
		done.setOnAction(event -> saveAndExit());

		rightPane.setMaxWidth(280);
		rightPane.setMinWidth(280);
		rightPane.setFillWidth(true);

		AnchorPane.setLeftAnchor(mainPane, 10d);
		AnchorPane.setRightAnchor(mainPane, 10d);
		AnchorPane.setTopAnchor(mainPane, 10d);
		AnchorPane.setBottomAnchor(mainPane, 10d);

		pp = new PPIndicator(this.model.totalCost);
		AnchorPane.setTopAnchor(pp, 10d);
		AnchorPane.setLeftAnchor(pp, 10d);

		characterName.getStyleClass().add("panel-title");
		characterName.setAlignment(Pos.BASELINE_CENTER);
		AnchorPane.setTopAnchor(characterName, 20d);
		AnchorPane.setLeftAnchor(characterName, 10d);
		AnchorPane.setRightAnchor(characterName, 100d);

		mainPane.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(profilePane, Priority.ALWAYS);
		mainPane.getChildren().addAll(profilePane, rightPane);

		root.getChildren().addAll(characterName, mainPane, pp);

		modalDialog.setOnShow(() -> {
			root.setMouseTransparent(true);
			modalDialog.requestFocus();
		});

		modalDialog.setOnClose(() -> root.setMouseTransparent(false));

		this.getChildren().addAll(root, modalDialog);

		selectedCharacter.addListener((v, o, n) -> {
			if (n != null) {
				characterName.setText(n.profileProperty()
					.get().rootCharacter.name);
				profilePane.setProfile(n);
			}
		});

		this.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ESCAPE) saveAndExit();
		});

		setLoadoutModel(model);

		loadoutName.setTooltip(new Tooltip("Enter a name for this loadout"));
		done.setTooltip(new Tooltip(
			"Save this loadout and return to the loadouts menu"));
	}

	public void saveAndExit() {
		if (this.model.totalCost.get() > Loadout.maxPP) {
			final Text text = new Text(
				"This loadout uses too many power points.\n" +
				"You cannot use this loadout for network games.\n" +
				"Continue anyway?");
			final StackPane textWrapper = new StackPane(text);
			textWrapper.getStyleClass().add("text-container");
			text.setWrappingWidth(400);
			text.getStyleClass().add("text");

			final DialogPane dialog = new DialogPane();
			dialog.getButtonTypes().addAll(ButtonType.YES, ButtonType.CANCEL);
			dialog.getStylesheets().add("dialogs.css");
			dialog.setHeaderText(null);
			dialog.setGraphic(null);
			dialog.setContentText(null);
			dialog.setContent(textWrapper);

			modalDialog.showDialog(dialog, r -> {
				if (r == ButtonType.YES) {
					final Loadout out = this.model.encodeLoadout();
					config.setLoadout(loadoutID, out);
					onDone.accept(Optional.of(out));
				}
			});
		} else {
			final Loadout out = this.model.encodeLoadout();
			config.setLoadout(loadoutID, out);
			onDone.accept(Optional.of(out));
		}
	}

	public void setLoadoutModel(final LoadoutModel model) {
		this.model = model;

		loadoutName.setText(model.name.get());
		model.name.bind(loadoutName.textProperty());

		rightPane.getChildren().clear();
		final Separator spacer1 = new Separator(Orientation.VERTICAL);
		final Separator spacer2 = new Separator(Orientation.VERTICAL);
		spacer1.setMinHeight(0);
		spacer2.setMinHeight(0);
		VBox.setVgrow(spacer1, Priority.ALWAYS);
		VBox.setVgrow(spacer2, Priority.ALWAYS);
		rightPane.getChildren().addAll(spacer1, loadoutName);

		selectedCharacter.set(null);
		final VBox indicatorsPane = new VBox(4);
		indicatorsPane.setMaxWidth(Double.MAX_VALUE);
		indicatorsPane.setFillWidth(true);
		for (int i = 0; i < 4; i++) {
			indicatorsPane.getChildren().add(
				new CharacterIndicatorPane(model,
					model.usedProfiles.get(i), selectedCharacter));
			if (selectedCharacter.get() == null) {
				selectedCharacter.set(model.usedProfiles.get(i));
			}
		}
		final RollerScrollPane scrollPane =
			new RollerScrollPane(indicatorsPane, false);
		scrollPane.setId("character-indicators");
		scrollPane.setMaxHeight(178 * 4 + 48);
		scrollPane.setMaxWidth(Double.MAX_VALUE);
		scrollPane.setMinHeight(100);
		scrollPane.layout();
		scrollPane.setScrollPos(scrollPane.getScrollMin());

		pp.setCostProperty(model.totalCost);
		rightPane.getChildren().addAll(scrollPane, done, spacer2);
	}
}

