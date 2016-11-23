package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.List;

/**
 * Get the path the character will follow to a specific point.
 * */
public class InfoPath extends InfoRequest<List<MapPoint>> {
	private final Character subject;
	private final MapPoint target;

	public InfoPath(Character subject, MapPoint target) {
		this.subject = subject;
		this.target = target;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(battle.battleState.findValidPath(
			subject.getPos(), target, subject.player));
	}
}

