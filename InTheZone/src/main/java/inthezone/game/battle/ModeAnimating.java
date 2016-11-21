package inthezone.game.battle;

import isogame.engine.MapPoint;

public class ModeAnimating extends Mode {
	private final BattleView view;
	public ModeAnimating(BattleView view) {
		this.view = view;
	}

	@Override public void setupMode() {
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

