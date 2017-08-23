package inthezone.game.battle;

import isogame.engine.KeyBinding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;

public abstract class HUD extends AnchorPane {
	protected final MessageLine messageLine = new MessageLine();

	protected final FlowPane actionButtons = new FlowPane();
	protected final CommandButton pushItem;
	protected final CommandButton potionItem;

	protected final RoundCounter roundCounter = new RoundCounter();

	protected final BooleanProperty disableUI = new SimpleBooleanProperty(false);

	protected final BattleView view;
	protected final Map<Integer, CharacterInfoBox> characters = new HashMap<>();
	protected final StandardSprites sprites;

	protected HUD(final BattleView view, final StandardSprites sprites) {
		this.view = view;
		this.sprites = sprites;

		pushItem = new CommandButton(sprites.pushIcon,
			"Push a character (1 AP)");
		pushItem.setButtonAction(() -> view.usePush());
		potionItem = new CommandButton(sprites.potionIcon,
			"Use a healing potion (1 AP)");
		potionItem.setButtonAction(() -> view.useItem());
	}

	public final void notifyRound() {
		roundCounter.increment();
		if (view.isMyTurn.get()) notifyTurn();
		else notifyOtherTurn();
	}

	protected void notifyTurn() {
	}

	protected void notifyOtherTurn() {
	}

	public final void notifyFatigue() {
		roundCounter.setFatigue();
	}

	public final void writeMessage(final String message) {
		messageLine.writeMessage(message);
	}

	public final void selectCharacter(final Optional<CharacterFrozen> oc) {
		final int id = oc.map(c -> c.getId()).orElse(-1);
		oc.ifPresent(c -> updateAbilities(c, c.hasMana()));

		for (final CharacterInfoBox box : characters.values())
			box.setSelected(box.id == id);
	}

	public abstract void handleKey(final KeyBinding binding);

	public void modalStart() { disableUI.set(true); }
	public void modalEnd() { disableUI.set(false); }

	/**
	 * Switch to battle end mode.
	 * */
	public abstract void doEndMode(final BattleOutcome outcome);
	public abstract void doReconnectMode(final boolean thisClientReconnecting);
	public abstract void endReconnectMode();

	public final void updateAbilities(final CharacterFrozen c, final boolean mana) {
		final Ability basicAbility =
			mana ? c.getBasicAbility().getMana() : c.getBasicAbility();

		final CommandButton attackItem = new CommandButton(sprites.attackIcon,
			(new AbilityDescription(basicAbility.info)).toString());
		attackItem.setButtonAction(() -> view.useAbility(basicAbility));

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

		for (final Ability a : c.getAbilities()) {
			final Ability ability = mana ? a.getMana() : a;

			final CommandButton i = new CommandButton(ability.info.media.icon,
				(new AbilityDescription(ability.info)).toString());
			i.cannotUseThis.set(notMyTurn ||
				c.isAbilityBlocked(ability) ||
				ability.info.ap > c.getAP() ||
				ability.info.mp > c.getMP() ||
				c.isAbilityBlocked(a));
			i.setButtonAction(() -> view.useAbility(ability));

			actionButtons.getChildren().add(i);
		}
	}

	public void init(final Collection<CharacterFrozen> characters) {
		for (final CharacterFrozen c : characters) {
			if (c.getPlayer() != view.player && view.player != Player.PLAYER_OBSERVER)
				continue;

			final CharacterInfoBox box = new CharacterInfoBox(c, sprites);
			box.setOnMouseClicked(event -> {
				if (!disableUI.get()) view.selectCharacterById(c.getId());
			});
			this.characters.put(c.getId(), box);
		}
	}
}

