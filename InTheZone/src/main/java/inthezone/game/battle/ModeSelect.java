package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Optional;

public class ModeSelect extends Mode {
	public ModeSelect(BattleView view) {
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

	@Override public void handleSelection(MapPoint p) {
		Optional<Character> oc = view.sprites.getCharacterAt(p);

		if (oc.isPresent() && oc.get().player == view.player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}
}

