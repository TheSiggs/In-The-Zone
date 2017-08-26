package inthezone.game.battle;

import isogame.engine.KeyBinding;

import java.util.Collection;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import inthezone.battle.BattleOutcome;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.data.StandardSprites;
import inthezone.game.ClientConfig;
import inthezone.game.InTheZoneKeyBinding;
import inthezone.game.battle.TurnClock;
import inthezone.game.guiComponents.KeyboardOptions;

public class StandardHUD extends HUD {
	private final HBox characterInfoBoxes = new HBox();
	private final Button endTurnButton = new Button("End turn");

	private final MenuBar optionsButton = new MenuBar();
	private final Menu optionsMenu = new Menu("Menu");
	private final MenuItem resignButton = new MenuItem("Resign");
	private final MenuItem keyboardButton = new MenuItem("Keyboard options");

	private final MultiTargetAssistant multiTargetAssistant;

	private final VBox roundCounterAndClock = new VBox(10);
	private final TurnClock clock = new TurnClock(new Duration(3 * 60 * 1000));
	final Tooltip clockTooltip = new Tooltip();

	private final VBox assistanceLine = new VBox();

	public StandardHUD(
		final BattleView view,
		final StandardSprites sprites,
		final ClientConfig config
	) {
		super(view, sprites);

		this.setMinSize(0, 0);
		this.getStylesheets().add("HUD.css");

		this.multiTargetAssistant = new MultiTargetAssistant(view);

		endTurnButton.setTooltip(new Tooltip("End your turn"));
		Tooltip.install(clock, clockTooltip);

		endTurnButton.setMaxWidth(Double.MAX_VALUE);

		endTurnButton.setOnAction(event -> view.sendEndTurn());
		keyboardButton.setOnAction(event -> {
			final KeyboardOptions dialog = new KeyboardOptions(config);
			view.modalDialog.showDialog(dialog, r -> {
				if (r == KeyboardOptions.doneButton) {
					view.canvas.keyBindings.loadBindings(dialog.resultTable);
					config.getKeyBindingTable().loadBindings(dialog.resultTable);
					config.writeConfig();
				}
			});
		});
		resignButton.setOnAction(event -> view.sendResign());

		optionsButton.getStyleClass().add("hud-menu-button");
		endTurnButton.getStyleClass().add("gui-button");

		endTurnButton.disableProperty().bind(view.isMyTurn.not()
			.or(view.cannotCancel).or(disableUI));
		optionsButton.disableProperty().bind(disableUI);
		resignButton.disableProperty().bind(view.isMyTurn.not().or(disableUI));

		actionButtons.visibleProperty().bind(view.isCharacterSelected
			.and(view.cannotCancel.not()).and(disableUI.not()));

		assistanceLine.setAlignment(Pos.CENTER);
		assistanceLine.setFillWidth(false);
		assistanceLine.getChildren().addAll(messageLine, multiTargetAssistant);

		actionButtons.setAlignment(Pos.CENTER);
		actionButtons.getChildren().addAll(pushItem, potionItem);
		actionButtons.setMaxHeight(actionButtons.getPrefHeight());

		clock.setPrefWidth(100d);
		clock.setPrefHeight(100d);
		roundCounterAndClock.getChildren().addAll(
			roundCounter, clock, endTurnButton);

		optionsButton.getMenus().add(optionsMenu);
		optionsMenu.getItems().addAll(keyboardButton, resignButton);

		final Group characterInfoBoxesWrapper =
			new Group(characterInfoBoxes);

		AnchorPane.setTopAnchor(characterInfoBoxesWrapper, 0d);
		AnchorPane.setLeftAnchor(characterInfoBoxesWrapper, 0d);

		AnchorPane.setBottomAnchor(optionsButton, 0d);
		AnchorPane.setRightAnchor(optionsButton, 0d);

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
			assistanceLine, characterInfoBoxesWrapper, actionButtons,
			optionsButton, roundCounterAndClock
		);
	}

	@Override public void handleKey(final KeyBinding binding) {
		final ObservableList<Node> boxes = characterInfoBoxes.getChildren();
		final ObservableList<Node> actions = actionButtons.getChildren();
		if (binding == InTheZoneKeyBinding.next) {
			view.selectCharacterById(
				((CharacterInfoBox) boxes.get((
					getSelectedCharacterIndex() + 1) % 4)).id);
		} else if (binding == InTheZoneKeyBinding.prev) {
			view.selectCharacterById(
				((CharacterInfoBox) boxes.get((
					getSelectedCharacterIndex() + 4 - 1) % 4)).id);
		} else if (binding == InTheZoneKeyBinding.character1) {
			view.selectCharacterById(((CharacterInfoBox) boxes.get(0)).id);
		} else if (binding == InTheZoneKeyBinding.character2) {
			view.selectCharacterById(((CharacterInfoBox) boxes.get(1)).id);
		} else if (binding == InTheZoneKeyBinding.character3) {
			view.selectCharacterById(((CharacterInfoBox) boxes.get(2)).id);
		} else if (binding == InTheZoneKeyBinding.character4) {
			view.selectCharacterById(((CharacterInfoBox) boxes.get(3)).id);
		} else if (binding == InTheZoneKeyBinding.clearSelected) {
			view.selectCharacter(Optional.empty());
		} else if (binding == InTheZoneKeyBinding.attack) {
			if (actions.size() > 0) ((CommandButton) actions.get(0)).doCommand();
		} else if (binding == InTheZoneKeyBinding.push) {
			if (actions.size() > 1) ((CommandButton) actions.get(1)).doCommand();
		} else if (binding == InTheZoneKeyBinding.potion) {
			if (actions.size() > 2) ((CommandButton) actions.get(2)).doCommand();
		} else if (binding == InTheZoneKeyBinding.special) {
			if (actions.size() > 3) ((CommandButton) actions.get(3)).doCommand();
		} else if (binding == InTheZoneKeyBinding.a1) {
			if (actions.size() > 4) ((CommandButton) actions.get(4)).doCommand();
		} else if (binding == InTheZoneKeyBinding.a2) {
			if (actions.size() > 5) ((CommandButton) actions.get(5)).doCommand();
		} else if (binding == InTheZoneKeyBinding.a3) {
			if (actions.size() > 6) ((CommandButton) actions.get(6)).doCommand();
		} else if (binding == InTheZoneKeyBinding.a4) {
			if (actions.size() > 7) ((CommandButton) actions.get(7)).doCommand();
		} else if (binding == InTheZoneKeyBinding.a5) {
			if (actions.size() > 8) ((CommandButton) actions.get(8)).doCommand();
		} else if (binding == InTheZoneKeyBinding.a6) {
			if (actions.size() > 9) ((CommandButton) actions.get(9)).doCommand();
		} else if (binding == InTheZoneKeyBinding.endTurn) {
			view.sendEndTurn();
		}
	}

	private int getSelectedCharacterIndex() {
		final ObservableList<Node> boxes = characterInfoBoxes.getChildren();
		for (int i = 0; i < boxes.size(); i++) {
			if (((CharacterInfoBox) boxes.get(i)).isSelected()) return i;
		}
		return -1;
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

	@Override public void init(final Collection<CharacterFrozen> characters) {
		super.init(characters);
		characterInfoBoxes.getChildren().addAll(this.characters.values());
	}
}

