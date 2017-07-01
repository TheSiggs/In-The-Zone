package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Map;

public class ModeAnimating extends Mode {
	private final Mode previous;

	public ModeAnimating(final BattleView view) {
		this(view, new ModeSelect(view));
	}

	public ModeAnimating(final BattleView view, final Mode previous) {
		super(view);
		this.previous = previous;
	}

	@Override public boolean isInteractive() {return false;}
	@Override public boolean canCancel() {return false;}

	@Override public ModeAnimating updateSelectedCharacter(
		final Character selected
	) {
		return new ModeAnimating(view, previous.updateSelectedCharacter(selected));
	}

	@Override public Mode retarget(final Map<MapPoint, MapPoint> retargeting) {
		return new ModeAnimating(view, previous.retarget(retargeting));
	}

	@Override public Mode animationDone() {
		System.err.println("Is my turn? " + view.isMyTurn.getValue());

		if (view.isMyTurn.getValue()) {
			return previous;
		} else {
			return new ModeOtherTurn(view);
		}
	}
}

