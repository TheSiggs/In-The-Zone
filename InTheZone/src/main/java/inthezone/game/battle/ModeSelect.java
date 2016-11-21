package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Optional;

public class ModeSelect extends Mode {
	private final BattleView view;

	public ModeSelect(BattleView view) {
		this.view = view;
	}

	@Override public void setupMode() {
		view.getStage().clearAllHighlighting();
	}

	@Override public void handleSelection(MapPoint p) {
		Optional<Character> oc = view.getCharacterAt(p);

		if (oc.isPresent() && oc.get().player == view.player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		return;
	}

	@Override public void handleMouseOut() {
		return;
	}
}

