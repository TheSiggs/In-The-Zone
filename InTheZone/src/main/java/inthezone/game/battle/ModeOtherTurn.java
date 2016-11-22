package inthezone.game.battle;

import isogame.engine.MapPoint;
import java.util.Optional;

public class ModeOtherTurn extends Mode {
	public ModeOtherTurn(BattleView view) {
		super(view);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		view.selectCharacter(Optional.empty());
		return this;
	}

	@Override public boolean isInteractive() {return false;}

	@Override public Mode animationDone() {
		return this;
	}
}

