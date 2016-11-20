package inthezone.game.battle;

import isogame.engine.MapPoint;
import java.util.Optional;

public class ModeOtherTurn extends Mode {
	private final BattleView view;

	public ModeOtherTurn(BattleView view) {
		this.view = view;
		view.canvas.getStage().clearAllHighlighting();
		view.selectCharacter(Optional.empty());
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

