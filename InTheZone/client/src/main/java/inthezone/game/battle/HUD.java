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

/**
 * Common HUD components.
 * */
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

	/**
	 * @param view a reference back to the BattleView
	 * @param sprites the standard sprites (icons etc.)
	 * */
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

	/**
	 * Notify a new round starting.
	 * */
	public final void notifyRound() {
		roundCounter.increment();
		if (view.isMyTurn.get()) notifyTurn();
		else notifyOtherTurn();
	}

	/**
	 * Notify the player's turn starting.
	 * */
	protected void notifyTurn() {}

	/**
	 * Notify the other player's turn starting.
	 * */
	protected void notifyOtherTurn() {}

	/**
	 * Notify fatigue damage.
	 * */
	public final void notifyFatigue() {
		roundCounter.setFatigue();
	}

	/**
	 * Write a message to the HUD.
	 * @param message the message to write
	 * */
	public final void writeMessage(final String message) {
		messageLine.writeMessage(message);
	}

	/**
	 * Select a character
	 * @param oc the character to select, or Optional.empty() to clear selection
	 * */
	public final void selectCharacter(final Optional<CharacterFrozen> oc) {
		final int id = oc.map(c -> c.getId()).orElse(-1);
		oc.ifPresent(c -> updateAbilities(c, c.hasMana()));

		for (final CharacterInfoBox box : characters.values())
			box.setSelected(box.id == id);
	}

	/**
	 * Handle a key binding.
	 * */
	public abstract void handleKey(final KeyBinding binding);

	/**
	 * A modal dialog starts.
	 * */
	public void modalStart() { disableUI.set(true); }

	/**
	 * A modal dialog ends.
	 * */
	public void modalEnd() { disableUI.set(false); }

	/**
	 * Switch to battle end mode.
	 * @param outcome the outcome of the battle.
	 * */
	public abstract void doEndMode(final BattleOutcome outcome);

	/**
	 * Go into reconnect mode.
	 * */
	public abstract void doReconnectMode(final boolean thisClientReconnecting);

	/**
	 * End reconnect mode.
	 * */
	public abstract void endReconnectMode();

	/**
	 * Update the abilities list for a character
	 * @param c a new copy of the character to update
	 * @param mana true if this character is on a mana zone, otherwise false
	 * */
	public final void updateAbilities(
		final CharacterFrozen c, final boolean mana
	) {
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

	/**
	 * Initialise the HUD
	 * @param character all the characters that appear in the HUD.
	 * */
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

