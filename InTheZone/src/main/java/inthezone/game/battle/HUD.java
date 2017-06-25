package inthezone.game.battle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.StandardSprites;
import inthezone.game.battle.TurnClock;

public class HUD extends AnchorPane {
	private final FlowPane characterInfoBoxes = new FlowPane();
	private final Button endTurnButton = new Button("End turn");
	private final Button resignButton = new Button("Resign");

	private final VBox roundCounterAndClock = new VBox(10);
	private final RoundCounter roundCounter = new RoundCounter();
	private final TurnClock clock = new TurnClock(new Duration(3 * 60 * 1000));
	final Tooltip clockTooltip = new Tooltip();

	private final MultiTargetAssistant multiTargetAssistant;
	private final MessageLine messageLine = new MessageLine();
	private final VBox assistanceLine = new VBox();

	private final FlowPane actionButtons = new FlowPane();

	private final FlowPane abilitiesMenu = new FlowPane();
	private final CommandButton pushItem;
	private final CommandButton potionItem;

	private final BooleanProperty disableUI = new SimpleBooleanProperty(false);

	private final BattleView view;

	public final Map<Integer, CharacterInfoBox> characters = new HashMap<>();

	private final StandardSprites sprites;

	public HUD(final BattleView view, final StandardSprites sprites) {
		this.setMinSize(0, 0);
		this.getStylesheets().add("HUD.css");

		this.sprites = sprites;
		this.view = view;
		this.multiTargetAssistant = new MultiTargetAssistant(view);

		endTurnButton.setTooltip(new Tooltip("End your turn"));
		resignButton.setTooltip(new Tooltip("Resign from the game"));
		Tooltip.install(clock, clockTooltip);

		pushItem = new CommandButton(sprites.pushIcon,
			"Push a character (1 AP)");
		pushItem.setButtonAction(event -> view.usePush());
		potionItem = new CommandButton(sprites.potionIcon,
			"Use a healing potion (1 AP)");
		potionItem.setButtonAction(event -> view.useItem());

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

		this.getChildren().addAll(
			assistanceLine, characterInfoBoxes, actionButtons,
			endTurnButton, resignButton, roundCounterAndClock
		);
	}

	public void notifyRound() {
		clock.reset();
		roundCounter.increment();
		if (view.isMyTurn.get()) notifyTurn();
		else notifyOtherTurn();
	}

	public void notifyTurn() {
		clock.clockAnimator.setOnFinished(event -> view.sendEndTurn());
		clockTooltip.textProperty().bind(
			new SimpleStringProperty("You have")
			.concat(clock.remainingTime).concat("s remaining to complete your turn"));
		clock.clockAnimator.play();
	}

	public void notifyOtherTurn() {
		clock.clockAnimator.setOnFinished(null);
		clockTooltip.textProperty().bind(
			new SimpleStringProperty("Your turn in ")
			.concat(clock.remainingTime).concat("s"));
		clock.reset();
		clock.clockAnimator.play();
	}

	public void notifyFatigue() {
		roundCounter.setFatigue();
	}

	public void writeMessage(final String message) {
		messageLine.writeMessage(message);
	}

	public void selectCharacter(final Optional<Character> oc) {
		final int id = oc.map(c -> c.id).orElse(-1);
		oc.ifPresent(c -> updateAbilities(c, c.hasMana()));

		for (CharacterInfoBox box : characters.values())
			box.setSelected(box.id == id);
	}

	public void modalStart() {
		disableUI.set(true);
	}

	public void modalEnd() {
		disableUI.set(false);
	}

	/**
	 * Switch to battle end mode.
	 * @param outcome The outcome, or nothing if a player resigned.
	 * @param resigned True if this player resigned.
	 * */
	public void doEndMode(final BattleOutcome outcome) {
		clock.reset();
		view.modalDialog.doCancel();
		disableUI.set(true);
		assistanceLine.getChildren().add(new EndManager(view, outcome));
	}

	public void doReconnectMode(final boolean thisClientReconnecting) {
		clock.clockAnimator.pause();
		view.modalDialog.doCancel();
		disableUI.set(true);
		assistanceLine.getChildren().add(
			new ReconnectManager(thisClientReconnecting));
	}

	public void endReconnectMode() {
		disableUI.set(false);
		clock.clockAnimator.play();
		assistanceLine.getChildren().clear();
	}

	public void updateAbilities(final Character c, final boolean mana) {
		final Ability basicAbility =
			mana ? c.basicAbility.getMana() : c.basicAbility;

		final CommandButton attackItem = new CommandButton(sprites.attackIcon,
			(new AbilityDescription(basicAbility.info)).toString());
		attackItem.setButtonAction(event -> view.useAbility(basicAbility));

		actionButtons.getChildren().clear();
		actionButtons.getChildren().addAll(attackItem, pushItem, potionItem);

		final boolean notMyTurn = !view.isMyTurn.get();

		attackItem.cannotUseThis.set(c.isAbilityBlocked(basicAbility) ||
			c.getAP() < 1 || notMyTurn);
		potionItem.cannotUseThis.set(c.isDead() || c.isStunned() ||
			c.getAP() < 1 || notMyTurn ||
			view.remainingPotions.get() <= 0);
		pushItem.cannotUseThis.set(c.isDead() || c.isStunned() ||
			c.getAP() < 1 || notMyTurn);

		for (Ability a : c.abilities) {
			final Ability ability = mana ? a.getMana() : a;

			final CommandButton i = new CommandButton(ability.info.media.icon,
				(new AbilityDescription(ability.info)).toString());
			i.cannotUseThis.set(notMyTurn ||
				c.isAbilityBlocked(ability) ||
				ability.info.ap > c.getAP() ||
				ability.info.mp > c.getMP() ||
				c.isAbilityBlocked(a));
			i.setButtonAction(event -> view.useAbility(ability));

			actionButtons.getChildren().add(i);
		}
	}

	public void init(final Collection<Character> characters) {
		for (Character c : characters) {
			final CharacterInfoBox box = new CharacterInfoBox(c, sprites);
			box.setOnMouseClicked(event -> {
				if (!disableUI.get()) view.selectCharacterById(c.id);
			});
			this.characters.put(c.id, box);
			characterInfoBoxes.getChildren().add(box);
		}
	}
}

