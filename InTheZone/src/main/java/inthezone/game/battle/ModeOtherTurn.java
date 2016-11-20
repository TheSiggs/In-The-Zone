package inthezone.game.battle;

import isogame.engine.MapPoint;
import java.util.Optional;

public class ModeOtherTurn extends Mode {
	private final BattleView view;

	public ModeOtherTurn(BattleView view) {
		this.view = view;
		view.getStage().clearAllHighlighting();
		view.selectCharacter(Optional.empty());
	}

	@Override public boolean isInteractive() {return false;}

	@Override public void handleSelection(MapPoint p) {
		return;
	}

	@Override public void handleMouseOver(MapPoint p) {
		return;
	}

	@Override public void handleMouseOut() {
		return;
	}
}

