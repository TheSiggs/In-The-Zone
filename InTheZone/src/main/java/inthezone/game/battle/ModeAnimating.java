package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.List;
import java.util.Optional;

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

	@Override public Mode updateSelectedCharacter(Character selected) {
		return new ModeAnimating(view, previous.updateSelectedCharacter(selected));
	}

	@Override public void updateAffected(List<Targetable> affected) {
		previous.updateAffected(affected);
	}

	@Override public Mode animationDone() {
		if (view.isMyTurn.getValue()) {
			return previous;
		} else {
			return new ModeOtherTurn(view);
		}
	}
}

