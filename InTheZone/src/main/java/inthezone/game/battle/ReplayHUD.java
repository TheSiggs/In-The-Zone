package inthezone.game.battle;

import java.util.Collection;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import inthezone.battle.BattleOutcome;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;

public class ReplayHUD extends HUD {
	@Override public void doEndMode(final BattleOutcome outcome) {
		System.err.println("End mode");

		view.modalDialog.doCancel();
		disableUI.set(true);
	}

	@Override public void doReconnectMode(final boolean thisClientReconnecting) {
		System.err.println("Reconnect mode");

		view.modalDialog.doCancel();
		disableUI.set(true);
	}

	@Override public void endReconnectMode() {
		disableUI.set(false);
	}

	private final HBox characterInfoBoxes1 = new HBox();
	private final HBox characterInfoBoxes2 = new HBox();
	private final Button nextButton = new Button("Next");
	private final Button closeButton = new Button("Close");

	private final VBox roundCounterAndClock = new VBox(10);

	private final VBox assistanceLine = new VBox();

	public ReplayHUD(
		final BattleView view,
		final StandardSprites sprites,
		final PlaybackGenerator playback
	) {
		super(view, sprites);

		this.setMinSize(0, 0);
		this.getStylesheets().add("HUD.css");

		nextButton.setTooltip(new Tooltip("Next action"));
		closeButton.setTooltip(new Tooltip("End the replay"));

		nextButton.setOnAction(event -> playback.nextCommand());
		closeButton.setOnAction(event -> {
			playback.close();
			view.handleEndBattle(Optional.empty());
		});

		nextButton.getStyleClass().add("gui-button");
		closeButton.getStyleClass().add("gui-button");

		nextButton.disableProperty().bind(disableUI);

		actionButtons.visibleProperty().bind(view.isCharacterSelected
			.and(view.cannotCancel.not()).and(disableUI.not()));

		assistanceLine.setAlignment(Pos.CENTER);
		assistanceLine.setFillWidth(false);
		assistanceLine.getChildren().addAll(messageLine);

		actionButtons.setAlignment(Pos.CENTER);
		actionButtons.getChildren().addAll(pushItem, potionItem);
		actionButtons.setMaxHeight(actionButtons.getPrefHeight());

		roundCounterAndClock.getChildren().addAll(
			roundCounter, nextButton, closeButton);

		final Group ci1 = new Group(characterInfoBoxes1);
		final Group ci2 = new Group(characterInfoBoxes2);

		AnchorPane.setTopAnchor(ci1, 0d);
		AnchorPane.setLeftAnchor(ci1, 0d);

		AnchorPane.setBottomAnchor(ci2, 0d);
		AnchorPane.setRightAnchor(ci2, 0d);

		AnchorPane.setTopAnchor(roundCounterAndClock, 0d);
		AnchorPane.setRightAnchor(roundCounterAndClock, 0d);

		AnchorPane.setBottomAnchor(actionButtons, 0d);
		AnchorPane.setLeftAnchor(actionButtons, 0d);
		AnchorPane.setRightAnchor(actionButtons, 0d);

		AnchorPane.setBottomAnchor(assistanceLine, 30d);
		AnchorPane.setLeftAnchor(assistanceLine, 0d);
		AnchorPane.setRightAnchor(assistanceLine, 0d);

		AnchorPane.setTopAnchor(view.canvas, 0d);
		AnchorPane.setBottomAnchor(view.canvas, 0d);
		AnchorPane.setLeftAnchor(view.canvas, 0d);
		AnchorPane.setRightAnchor(view.canvas, 0d);

		this.getChildren().addAll(view.canvas,
			ci1, ci2, actionButtons, roundCounterAndClock, assistanceLine);
	}

	@Override public void init(final Collection<CharacterFrozen> characters) {
		super.init(characters);

		for (final CharacterFrozen c : characters) {
			if (c.getPlayer() == Player.PLAYER_A) {
				characterInfoBoxes1.getChildren().add(this.characters.get(c.getId()));
			} else if (c.getPlayer() == Player.PLAYER_B) {
				characterInfoBoxes2.getChildren().add(this.characters.get(c.getId()));
			}
		}
	}
}

