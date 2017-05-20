package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.StandardSprites;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HUD extends AnchorPane {
	private final FlowPane characterInfoBoxes = new FlowPane();
	private final Button endTurnButton = new Button("End turn");
	private final Button resignButton = new Button("Resign");
	private final RoundCounter roundCounter = new RoundCounter();

	private final MultiTargetAssistant multiTargetAssistant;
	private final MessageLine messageLine = new MessageLine();
	private final VBox assistanceLine = new VBox();

	private final FlowPane actionButtons = new FlowPane();

	private final FlowPane abilitiesMenu = new FlowPane();
	private final CommandButton pushItem =
		new CommandButton("Push", "Push a character (1 AP)");
	private final CommandButton potionItem =
		new CommandButton("Use potion", "Use a healing potion (1 AP)");

	private final BooleanProperty disableUI = new SimpleBooleanProperty(false);

	private final BattleView view;

	public final Map<Integer, CharacterInfoBox> characters = new HashMap<>();

	private StandardSprites sprites;

	public HUD(BattleView view, StandardSprites sprites) {
		super();
		this.setMinSize(0, 0);

		this.sprites = sprites;

		this.view = view;
		this.multiTargetAssistant = new MultiTargetAssistant(view);

		endTurnButton.setTooltip(new Tooltip("End your turn"));
		resignButton.setTooltip(new Tooltip("Resign from the game"));

		pushItem.setButtonAction(event -> view.usePush());
		potionItem.setButtonAction(event -> view.useItem());

		endTurnButton.setOnAction(event -> {view.sendEndTurn();});
		resignButton.setOnAction(event -> view.sendResign());

		endTurnButton.getStyleClass().add("gui_button");
		resignButton.getStyleClass().add("gui_button");

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

		AnchorPane.setTopAnchor(characterInfoBoxes, 0d);
		AnchorPane.setLeftAnchor(characterInfoBoxes, 0d);

		AnchorPane.setBottomAnchor(endTurnButton, 0d);
		AnchorPane.setLeftAnchor(endTurnButton, 0d);

		AnchorPane.setBottomAnchor(resignButton, 0d);
		AnchorPane.setRightAnchor(resignButton, 0d);

		AnchorPane.setTopAnchor(roundCounter, 0d);
		AnchorPane.setRightAnchor(roundCounter, 0d);

		AnchorPane.setBottomAnchor(actionButtons, 0d);
		AnchorPane.setLeftAnchor(actionButtons, 0d);
		AnchorPane.setRightAnchor(actionButtons, 0d);

		AnchorPane.setBottomAnchor(assistanceLine, 30d);
		AnchorPane.setLeftAnchor(assistanceLine, 0d);
		AnchorPane.setRightAnchor(assistanceLine, 0d);

		this.getChildren().addAll(
			assistanceLine, characterInfoBoxes, actionButtons,
			endTurnButton, resignButton, roundCounter
		);
	}

	public void notifyRound() {
		roundCounter.increment();
	}

	public void notifyFatigue() {
		roundCounter.setFatigue();
	}

	public void writeMessage(String message) {
		messageLine.writeMessage(message);
	}

	public void selectCharacter(Optional<Character> oc) {
		final int id = oc.map(c -> c.id).orElse(-1);
		oc.ifPresent(c -> updateAbilities(c, c.hasMana()));

		for (CharacterInfoBox box : characters.values())
			box.setSelected(box.id == id);
	}

	/**
	 * Switch to battle end mode.
	 * @param outcome The outcome, or nothing if a player resigned.
	 * @param resigned True if this player resigned.
	 * */
	public void doEndMode(BattleOutcome outcome) {
		disableUI.set(true);
		assistanceLine.getChildren().add(new EndManager(view, outcome));
	}

	public void doReconnectMode(boolean thisClientReconnecting) {
		disableUI.set(true);
		assistanceLine.getChildren().add(new ReconnectManager(thisClientReconnecting));
	}

	public void endReconnectMode() {
		disableUI.set(false);
		assistanceLine.getChildren().clear();
	}

	public void updateAbilities(Character c, boolean mana) {
		final Ability basicAbility = mana ? c.basicAbility.getMana() : c.basicAbility;

		final CommandButton attackItem = new CommandButton("Attack",
			(new AbilityDescription(basicAbility.info)).toString());
		attackItem.setButtonAction(event -> view.useAbility(basicAbility));

		actionButtons.getChildren().clear();
		actionButtons.getChildren().addAll(attackItem, pushItem, potionItem);

		final boolean notMyTurn = !view.isMyTurn.get();

		attackItem.cannotUseThis.set(c.isStunned() || c.getAP() < 1 || notMyTurn);
		potionItem.cannotUseThis.set(c.isStunned() || c.getAP() < 1 || notMyTurn);
		pushItem.cannotUseThis.set(c.isStunned() || c.getAP() < 1 || notMyTurn);

		for (Ability a : c.abilities) {
			final Ability ability = mana ? a.getMana() : a;

			final CommandButton i = new CommandButton(ability.info.name,
				(new AbilityDescription(ability.info)).toString());
			i.cannotUseThis.set(notMyTurn ||
				ability.info.ap > c.getAP() ||
				ability.info.mp > c.getMP() ||
				c.isAbilityBlocked(a));
			i.setButtonAction(event -> view.useAbility(ability));

			actionButtons.getChildren().add(i);
		}
	}

	public void init(Collection<Character> characters) {
		for (Character c : characters) {
			CharacterInfoBox box = new CharacterInfoBox(c, sprites);
			box.setOnMouseClicked(event -> {
				view.selectCharacterById(c.id);
			});
			this.characters.put(c.id, box);
			characterInfoBoxes.getChildren().add(box);
		}
	}
}

