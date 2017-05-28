package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class LoadoutView extends DialogScreen<Void> {
	private LoadoutModel model;
	private final TextField loadoutName = new TextField("<Loadout name (click to edit)>");
	private final Button done = new Button("Done");
	private final IntegerProperty totalCost = new SimpleIntegerProperty(0);
	private final BooleanProperty isLegitimate = new SimpleBooleanProperty(true);
	private final Label costLabel = new Label("Cost: ");
	private final VBox rightPane = new VBox(4);
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

		// The done button
		done.setOnAction(event -> {
			onDone.accept(null);
		});

		rightPane.setMaxWidth(280);
		this.getChildren().add(rightPane);

		setLoadoutModel(model);
	}

	public void setLoadoutModel(LoadoutModel model) {
		this.model = model;

		loadoutName.setText(model.name.get());
		model.name.bind(loadoutName.textProperty());

		rightPane.getChildren().clear();
		rightPane.getChildren().add(loadoutName);
		for (int i = 0; i < 4; i++) {
			rightPane.getChildren().add(
				new CharacterIndicatorPane(model.usedProfiles.get(i)));
		}
		rightPane.getChildren().add(done);
	}

	private static LoadoutModel emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		final List<CharacterProfile> profiles = new ArrayList<>();
		return new LoadoutModel(new Loadout("<new loadout>", profiles));
	}
}

