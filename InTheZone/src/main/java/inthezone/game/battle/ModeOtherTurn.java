package inthezone.game.battle;

import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.List;
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

	@Override public Mode animationDone() {
		return this;
	}
}

