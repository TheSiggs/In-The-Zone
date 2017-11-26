package inthezone.game.battle;

import inthezone.battle.CharacterFrozen;
import isogame.engine.MapPoint;
import java.util.Map;

/**
 * Waiting for the other player to reconnect.
 * */
public class ModeWaitForReconnect extends Mode {
	private Mode previous;

	public ModeWaitForReconnect(final BattleView view, final Mode previous) {
		super(view);
		this.previous = previous;
	}

	public Mode getPrevious() { return previous; }

	@Override public boolean isInteractive() {return false;}
	@Override public boolean canCancel() {return false;}

	@Override public ModeWaitForReconnect updateSelectedCharacter(
		final CharacterFrozen selected
	) {
		return new ModeWaitForReconnect(view,
			previous.updateSelectedCharacter(selected));
	}

	@Override public Mode retarget(final Map<MapPoint, MapPoint> retargeting) {
		return new ModeWaitForReconnect(view, previous.retarget(retargeting));
	}

	@Override public Mode animationDone() {
		previous = previous.animationDone();
		return this;
	}
}

