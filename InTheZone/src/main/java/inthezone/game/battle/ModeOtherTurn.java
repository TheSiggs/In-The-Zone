package inthezone.game.battle;

import java.util.Optional;

import inthezone.battle.CharacterFrozen;
import inthezone.battle.data.Player;
import isogame.engine.SelectionInfo;

/**
 * It is the other player's turn.
 * */
public class ModeOtherTurn extends Mode {
	public ModeOtherTurn(final BattleView view) {
		super(view);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		return this;
	}

	@Override public boolean isInteractive() {return false;}

	@Override public void handleSelection(final SelectionInfo selection) {
		final Optional<CharacterFrozen> oc = selection.spritePriority()
			.flatMap(p -> view.sprites.getCharacterAt(p));

		if (oc.isPresent() &&
			(view.player == Player.PLAYER_OBSERVER ||
				oc.get().getPlayer() == view.player)
		) {
			view.outOfTurnSelect(Optional.of(oc.get()));
		} else {
			view.outOfTurnSelect(Optional.empty());
		}
	}

	@Override public Mode animationDone() {
		return this;
	}
}

