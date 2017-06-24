package inthezone.game.battle;


import java.util.Optional;

import inthezone.battle.Character;
import isogame.engine.SelectionInfo;

public class ModeSelect extends Mode {
	public ModeSelect(final BattleView view) {
		super(view);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		Optional<Character> s = view.getSelectedCharacter();
		if (s.isPresent() && !s.get().isDead()) {
			return (new ModeMove(view, s.get())).setupMode();
		} else {
			return this;
		}
	}

	@Override public void handleSelection(final SelectionInfo selection) {
		final Optional<Character> oc = selection.spritePriority()
			.flatMap(p -> view.sprites.getCharacterAt(p));

		if (oc.isPresent() && oc.get().player == view.player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}
}

