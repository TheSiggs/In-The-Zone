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
	private final int range;

	public InfoPath(Character subject, MapPoint target) {
		this.subject = subject;
		this.target = target;
		this.range = subject.getMP();
	}

	public InfoPath(Character subject, MapPoint target, int range) {
		this.subject = subject;
		this.target = target;
		this.range = range;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(battle.battleState.findValidPath(
			subject.getPos(), target, subject.player, range));
	}
}

