package inthezone.game.battle;

import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import inthezone.game.battle.TurnClock;

public class StandardHUD extends HUD {
	private final FlowPane characterInfoBoxes = new FlowPane();
	private final Button endTurnButton = new Button("End turn");
	private final Button resignButton = new Button("Resign");

	private final MultiTargetAssistant multiTargetAssistant;

	private final VBox roundCounterAndClock = new VBox(10);
	private final TurnClock clock = new TurnClock(new Duration(3 * 60 * 1000));
	final Tooltip clockTooltip = new Tooltip();

	private final VBox assistanceLine = new VBox();

	public StandardHUD(final BattleView view, final StandardSprites sprites) {
		super(view, sprites);

		this.setMinSize(0, 0);
		this.getStylesheets().add("HUD.css");

		this.multiTargetAssistant = new MultiTargetAssistant(view);

		endTurnButton.setTooltip(new Tooltip("End your turn"));
		resignButton.setTooltip(new Tooltip("Resign from the game"));
		Tooltip.install(clock, clockTooltip);

		endTurnButton.setOnAction(event -> view.sendEndTurn());
		resignButton.setOnAction(event -> view.sendResign());

		endTurnButton.getStyleClass().add("gui-button");
		resignButton.getStyleClass().add("gui-button");

		endTurnButton.disableProperty().bind(view.isMyTurn.not()
			.or(view.cannotCancel).or(disableUI));
		resignButton.disableProperty().bind(view.isMyTurn.not().or(disableUI));

		actionButtons.visibleProperty().bind(view.isCharacterSelected
			.and(view.cannotCancel.not()).and(disableUI.not()));

		characterInfoBoxes.setPrefWrapLength(1000);

		assistanceLine.setAlignment(Pos.CENTER);
		assistanceLine.setFillWidth(false);
		assistanceLine.getChildren().addAll(messageLine, multiTargetAssistant);

		actionButtons.setAlignment(Pos.CENTER);
		actionButtons.getChildren().addAll(pushItem, potionItem);
		actionButtons.setMaxHeight(actionButtons.getPrefHeight());

		clock.setPrefWidth(100d);
		clock.setPrefHeight(100d);
		roundCounterAndClock.getChildren().addAll(roundCounter, clock);

		AnchorPane.setTopAnchor(characterInfoBoxes, 0d);
		AnchorPane.setLeftAnchor(characterInfoBoxes, 0d);

		AnchorPane.setBottomAnchor(endTurnButton, 0d);
		AnchorPane.setLeftAnchor(endTurnButton, 0d);

		AnchorPane.setBottomAnchor(resignButton, 0d);
		AnchorPane.setRightAnchor(resignButton, 0d);

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

		this.getChildren().addAll(
			view.canvas,
			assistanceLine, characterInfoBoxes, actionButtons,
			endTurnButton, resignButton, roundCounterAndClock
		);
	}

	@Override protected void notifyTurn() {
		clock.reset();
		clock.clockAnimator.setOnFinished(event -> view.sendEndTurn());
		clockTooltip.textProperty().bind(
			new SimpleStringProperty("You have")
			.concat(clock.remainingTime).concat("s remaining to complete your turn"));
		clock.clockAnimator.play();
	}

	@Override protected void notifyOtherTurn() {
		clock.reset();
		clock.clockAnimator.setOnFinished(null);
		clockTooltip.textProperty().bind(
			new SimpleStringProperty("Your turn in ")
			.concat(clock.remainingTime).concat("s"));
		clock.reset();
		clock.clockAnimator.play();
	}

	@Override public void doEndMode(final BattleOutcome outcome) {
		clock.reset();
		view.modalDialog.doCancel();
		disableUI.set(true);
		assistanceLine.getChildren().add(new EndManager(view, outcome));
	}

	@Override public void doReconnectMode(final boolean thisClientReconnecting) {
		clock.clockAnimator.pause();
		view.modalDialog.doCancel();
		disableUI.set(true);
		assistanceLine.getChildren().add(
			new ReconnectManager(thisClientReconnecting));
	}

	@Override public void endReconnectMode() {
		disableUI.set(false);
		clock.clockAnimator.play();
		assistanceLine.getChildren().clear();
	}

	@Override public void init(final Collection<Character> characters) {
		super.init(characters);
		characterInfoBoxes.getChildren().addAll(this.characters.values());
	}
}

