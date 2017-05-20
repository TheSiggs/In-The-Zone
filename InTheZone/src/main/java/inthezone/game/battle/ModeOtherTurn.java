package inthezone.game.battle;

import inthezone.battle.Targetable;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Optional;

public class ModeOtherTurn extends Mode {
	public ModeOtherTurn(BattleView view) {
		super(view);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		return this;
	}

	@Override public boolean isInteractive() {return false;}

	@Override public void handleSelection(MapPoint p) {
		Optional<Character> oc = view.sprites.getCharacterAt(p);

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

