package inthezone.game.battle;


import java.util.Optional;

import inthezone.battle.Character;
import isogame.engine.SelectionInfo;

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
		final Optional<Character> oc = selection.spritePriority()
			.flatMap(p -> view.sprites.getCharacterAt(p));

		if (oc.isPresent() && oc.get().player == view.player) {
			view.outOfTurnSelect(Optional.of(oc.get()));
		} else {
			view.outOfTurnSelect(Optional.empty());
		}
	}

	@Override public Mode animationDone() {
		return this;
	}
}

