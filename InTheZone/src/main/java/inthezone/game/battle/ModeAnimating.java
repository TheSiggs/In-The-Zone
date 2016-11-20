package inthezone.game.battle;

import isogame.engine.MapPoint;

public class ModeAnimating extends Mode {
	public ModeAnimation(BattleView view) {
		view.canvas.getStage().clearAllHighlighting();
	}

	@Overide private void handleSelection(MapPoint p) {
		return;
	}

	@Overide private void handleMouseOver(MapPoint p) {
		return;
	}

	@Overide private void handleMouseOut() {
		return;
	}
}

