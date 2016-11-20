package inthezone.game.battle;

import isogame.engine.MapPoint;

public class ModeAnimating extends Mode {
	public ModeAnimating(BattleView view) {
		view.getStage().clearAllHighlighting();
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

