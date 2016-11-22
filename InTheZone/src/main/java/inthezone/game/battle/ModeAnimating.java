package inthezone.game.battle;

import isogame.engine.MapPoint;

public class ModeAnimating extends Mode {
	private final Mode previous;

	public ModeAnimating(BattleView view) {
		this(view, new ModeSelect(view));
	}

	public ModeAnimating(BattleView view, Mode previous) {
		super(view);
		this.previous = previous;
	}

	@Override public boolean isInteractive() {return false;}

	@Override public Mode animationDone() {
		if (view.isMyTurn.getValue()) {
			return previous;
		} else {
			return new ModeOtherTurn(view);
		}
	}
}

