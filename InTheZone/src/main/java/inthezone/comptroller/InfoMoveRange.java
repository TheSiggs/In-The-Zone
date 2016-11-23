package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Get the locations the character can move to.
 * */
public class InfoMoveRange extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;

	public InfoMoveRange(Character subject) {
		this.subject = subject;
	}

	@Override public void completeAction(Battle battle) {
		Set<MapPoint> r = new HashSet<>();
		int w = battle.battleState.terrain.terrain.w;
		int h = battle.battleState.terrain.terrain.h;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				MapPoint p = new MapPoint(x, y);
				List<MapPoint> path = battle.battleState.findPath(
					subject.getPos(), p, subject.player);
				if (battle.battleState.canMove(path)) r.add(p);
			}
		}

		complete.complete(r);
	}
}

