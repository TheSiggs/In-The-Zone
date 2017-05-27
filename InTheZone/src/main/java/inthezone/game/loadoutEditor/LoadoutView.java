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
import java.util.ArrayList;
import java.util.List;

public class LoadoutView extends DialogScreen<Void> {
	private final LoadoutModel model;
	private final TextField loadoutName = new TextField("<loadout name>");
	private final Button done = new Button("Done");
	private final IntegerProperty totalCost = new SimpleIntegerProperty(0);
	private final BooleanProperty isLegitimate = new SimpleBooleanProperty(true);
	private final Label costLabel = new Label("Cost: ");
	private final ClientConfig config;

	private void rebindTotalCost(LoadoutModel l) {
		if (l != null) totalCost.bind(l.profiles.stream()
			.map(pr -> (NumberExpression) pr.costProperty())
			.reduce(new SimpleIntegerProperty(0), (x, y) -> x.add(y)));
	}

	public LoadoutView(ClientConfig config, GameDataFactory gameData) {
		this(config, gameData, emptyLoadout(config, gameData));
	}

	public LoadoutView(
		ClientConfig config, GameDataFactory gameData, LoadoutModel model
	) {
		this.config = config;
		this.model = model;

		this.getStylesheets().add("/GUI.css");
		this.getStyleClass().add("gui-pane");

		done.getStyleClass().add("gui-button");

		// The done button
		done.setOnAction(event -> {
			onDone.accept(null);
		});

		this.getChildren().add(done);
	}

	private static LoadoutModel emptyLoadout(
		ClientConfig config, GameDataFactory gameData
	) {
		final List<CharacterProfile> profiles = new ArrayList<>();
		return new LoadoutModel(new Loadout("<new loadout>", profiles));
	}
}

