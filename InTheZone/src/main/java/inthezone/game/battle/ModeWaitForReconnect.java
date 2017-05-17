package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Map;

public class ModeWaitForReconnect extends Mode {
	private Mode previous;

	public ModeWaitForReconnect(BattleView view, Mode previous) {
		super(view);
		this.previous = previous;
	}

	public Mode getPrevious() { return previous; }

	@Override public boolean isInteractive() {return false;}
	@Override public boolean canCancel() {return false;}

	@Override public ModeWaitForReconnect updateSelectedCharacter(Character selected) {
		return new ModeWaitForReconnect(view, previous.updateSelectedCharacter(selected));
	}

	@Override public Mode retarget(Map<MapPoint, MapPoint> retargeting) {
		return new ModeWaitForReconnect(view, previous.retarget(retargeting));
	}

	@Override public Mode animationDone() {
		previous = previous.animationDone();
		return this;
	}
}

