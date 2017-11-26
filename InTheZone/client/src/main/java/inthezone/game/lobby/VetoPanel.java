package inthezone.game.lobby;

import isogame.engine.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import inthezone.battle.data.GameDataFactory;

/**
 * Panel for vetoing maps that the player doesn't want to play.
 * */
public class VetoPanel extends StackPane {
	private final VBox inner = new VBox();
	private final Button resetButton = new Button("Reset");
	private final Button joinButton = new Button("Join queue");
	private final Button cancelButton = new Button("Cancel");
	private final HBox buttonBar = new HBox();
	private final Label title = new Label(
		"Click on any maps you don't want to play to veto them");
	private final ScrollPane vetoWrapper;
	private final FlowPane vetoButtonsPanel = new FlowPane();

	private final BooleanProperty allVetoed = new SimpleBooleanProperty(false);

	public VetoPanel(
		final GameDataFactory gameData,
		final Runnable onCancel,
		final Consumer<List<String>> onQueue
	) {
		vetoWrapper = new ScrollPane(vetoButtonsPanel);

		title.setMaxWidth(Double.MAX_VALUE);

		final Separator s1 = new Separator();
		final Separator s2 = new Separator();
		s1.setMaxWidth(Double.MAX_VALUE); s2.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(s1, Priority.ALWAYS); HBox.setHgrow(s2, Priority.ALWAYS);
		buttonBar.getChildren().addAll(resetButton, s1, joinButton, s2, cancelButton);

		this.getStylesheets().add("scrollpanel.css");
		buttonBar.getStyleClass().add("buttons");

		vetoButtonsPanel.prefWidthProperty().bind(
			vetoWrapper.widthProperty().subtract(new SimpleIntegerProperty(12)));

		initVetoButtons(gameData);

		cancelButton.setOnAction(event -> onCancel.run());
		joinButton.setOnAction(event -> 
			onQueue.accept(vetoButtons.stream()
				.filter(b -> b.isSelected()).map(b -> b.stage.name)
				.collect(Collectors.toList())));
		resetButton.setOnAction(event -> {
			for (final VetoButton b : vetoButtons) b.setSelected(false);
		});
		cancelButton.getStyleClass().add("gui-button");
		joinButton.getStyleClass().add("gui-button");
		joinButton.disableProperty().bind(allVetoed);
		resetButton.getStyleClass().add("gui-button");

		this.setId("veto-panel");
		inner.setId("veto-panel-inner");
		title.getStyleClass().add("title");

		vetoButtonsPanel.getStyleClass().add("veto-buttons");
		vetoButtonsPanel.setVgap(10);
		vetoButtonsPanel.setHgap(10);
		vetoButtonsPanel.setAlignment(Pos.TOP_CENTER);

		inner.setFillWidth(true);
		inner.getChildren().addAll(title, vetoWrapper, buttonBar);
		this.getChildren().add(inner);
	}

	private int nVetoed = 0;
	private final List<VetoButton> vetoButtons = new ArrayList<>();

	private void initVetoButtons(final GameDataFactory gameData) {
		for (final Stage s : gameData.getStages()) {
			final VetoButton b = new VetoButton(gameData, s);
			vetoButtons.add(b);
			vetoButtonsPanel.getChildren().addAll(b);
			b.selectedProperty().addListener((o, s0, s1) -> {
				if (s1 && !s0) nVetoed += 1;
				else if (!s1 && s0) nVetoed -= 1;
				if (nVetoed >= vetoButtons.size()) {
					allVetoed.setValue(true);
				} else {
					allVetoed.setValue(false);
				}
			});
		}
	}
}

