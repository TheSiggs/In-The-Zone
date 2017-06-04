package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import inthezone.game.RollerScrollPane;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class LoadoutView extends DialogScreen<Void> {
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

	private final IntegerProperty totalCost = new SimpleIntegerProperty(0);
	private final BooleanProperty isLegitimate = new SimpleBooleanProperty(true);
	private final ObjectProperty<CharacterProfileModel> selectedCharacter =
		new SimpleObjectProperty<>(null);

	private final ClientConfig config;

	private void rebindTotalCost(LoadoutModel l) {
		if (l != null) totalCost.bind(l.usedProfiles.stream()
			.map(opr -> (NumberExpression) opr.map(pr ->
				pr.costProperty()).orElse(new SimpleIntegerProperty(0)))
			.reduce(new SimpleIntegerProperty(0), (x, y) -> x.add(y)));
	}

	public LoadoutView(ClientConfig config, GameDataFactory gameData) {
		this(config, gameData, emptyLoadout(config, gameData));
		if (config.loadouts.size() > 0) {
			setLoadoutModel(new LoadoutModel(config.loadouts.iterator().next()));
		}
	}

	public LoadoutView(
		ClientConfig config, GameDataFactory gameData, LoadoutModel model
	) {
		this.config = config;
		this.model = model;

		this.getStylesheets().add("/GUI.css");
		this.getStyleClass().add("gui-pane");

		loadoutName.getStyleClass().add("gui-textfield");
		done.getStyleClass().add("gui-button");

		done.setMaxWidth(Double.MAX_VALUE);

		// The done button
		done.setOnAction(event -> {
			onDone.accept(null);
		});

		rightPane.setMaxWidth(280);
		rightPane.setMinWidth(280);
		rightPane.setFillWidth(true);

		AnchorPane.setLeftAnchor(mainPane, 10d);
		AnchorPane.setRightAnchor(mainPane, 10d);
		AnchorPane.setTopAnchor(mainPane, 10d);
		AnchorPane.setBottomAnchor(mainPane, 10d);

		pp = new PPIndicator(totalCost);
		AnchorPane.setTopAnchor(pp, 10d);
		AnchorPane.setLeftAnchor(pp, 10d);

		characterName.setId("loadout-character-name");
		characterName.setAlignment(Pos.BASELINE_CENTER);
		AnchorPane.setTopAnchor(characterName, 20d);
		AnchorPane.setLeftAnchor(characterName, 10d);
		AnchorPane.setRightAnchor(characterName, 100d);

		mainPane.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(profilePane, Priority.ALWAYS);
		mainPane.getChildren().addAll(profilePane, rightPane);

		root.getChildren().addAll(characterName, mainPane, pp);

		this.getChildren().add(root);

		setLoadoutModel(model);

		selectedCharacter.addListener((v, o, n) -> {
			characterName.setText(n.profileProperty()
				.get().rootCharacter.name);
			profilePane.setProfile(n);
		});

		loadoutName.setTooltip(new Tooltip("Enter a name for this loadout"));
		done.setTooltip(new Tooltip(
			"Save this loadout and return to the loadouts menu"));
	}

	public void setLoadoutModel(LoadoutModel model) {
		this.model = model;

		loadoutName.setText(model.name.get());
		model.name.bind(loadoutName.textProperty());
		rebindTotalCost(model);

		rightPane.getChildren().clear();
		final Separator spacer1 = new Separator(Orientation.VERTICAL);
		final Separator spacer2 = new Separator(Orientation.VERTICAL);
		spacer1.setMinHeight(0);
		spacer2.setMinHeight(0);
		VBox.setVgrow(spacer1, Priority.ALWAYS);
		VBox.setVgrow(spacer2, Priority.ALWAYS);
		rightPane.getChildren().addAll(spacer1, loadoutName);

		final VBox indicatorsPane = new VBox(4);
		indicatorsPane.setMaxWidth(Double.MAX_VALUE);
		indicatorsPane.setFillWidth(true);
		for (int i = 0; i < 4; i++) {
			indicatorsPane.getChildren().add(
				new CharacterIndicatorPane(
					model.usedProfiles.get(i), selectedCharacter));
			model.usedProfiles.get(i).ifPresent(p -> {
				if (selectedCharacter.get() == null) selectedCharacter.set(p);
			});
		}
		final RollerScrollPane scrollPane =
			new RollerScrollPane(indicatorsPane, false);
		scrollPane.setId("character-indicators");
		scrollPane.setMaxHeight(178 * 4 + 48);
		scrollPane.setMaxWidth(Double.MAX_VALUE);
		scrollPane.setMinHeight(100);
		scrollPane.layout();
		scrollPane.setScrollPos(scrollPane.getScrollMin());

		rightPane.getChildren().addAll(scrollPane, done, spacer2);
	}

	private static LoadoutModel emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		final List<CharacterProfile> profiles = new ArrayList<>();
		return new LoadoutModel(new Loadout("<new loadout>", profiles));
	}
}

