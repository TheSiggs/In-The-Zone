package inthezone.game.battle;


import java.util.Optional;

import inthezone.battle.CharacterFrozen;
import isogame.engine.SelectionInfo;

/**
 * A character is selected.
 * */
public class ModeSelect extends Mode {
	public ModeSelect(final BattleView view) {
		super(view);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		final Optional<CharacterFrozen> s = view.getSelectedCharacter();
		if (s.isPresent() && !s.get().isDead()) {
			return (new ModeMove(view, s.get())).setupMode();
		} else {
			return this;
		}
	}

	@Override public void handleSelection(final SelectionInfo selection) {
		final Optional<CharacterFrozen> oc = selection.spritePriority()
			.flatMap(p -> view.sprites.getCharacterAt(p));

		if (oc.isPresent() && oc.get().getPlayer() == view.player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}
}

