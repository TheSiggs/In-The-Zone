package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.LineOfSight;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Get the valid targets of a teleport action.
 * */
public class InfoTeleportRange extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;
	private final int range;

	public InfoTeleportRange(Character subject, int range) {
		this.subject = subject;
		this.range = range;
	}

	@Override public void completeAction(Battle battle) {
		Collection<MapPoint> diamond = LineOfSight.getDiamond(subject.getPos(), range);
		complete.complete(diamond.stream()
			.filter(p -> battle.battleState.isSpaceFree(p))
			.collect(Collectors.toList()));
	}
}

