package inthezone.game.battle;

import inthezone.battle.Character;
import java.util.Optional;

public class ModeSelect extends Mode {
	private final BattleView view;

	public ModeSelect(BattleView view) {
		this.view = view;
	}

	@Override private void handleSelection(MapPoint p) {
		Optional<Character> = view.getCharacterAt(p);

		if (oc.isPresent() && oc.get().player == player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override private void handleMouseOver(MapPoint p) {
		return;
	}

	@Override private void handleMouseOut() {
		return;
	}
}

